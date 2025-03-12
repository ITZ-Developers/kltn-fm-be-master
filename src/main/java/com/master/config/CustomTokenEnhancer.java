package com.master.config;

import com.master.constant.MasterConstant;
import com.master.dto.auth.RequestInfoWrapperDto;
import com.master.jwt.MasterJwt;
import com.master.redis.RedisService;
import com.master.utils.GenerateUtils;
import com.master.utils.Md5Utils;
import com.master.utils.ZipUtils;
import com.master.dto.auth.AccountForTokenDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.*;

@Slf4j
public class CustomTokenEnhancer implements TokenEnhancer {
    private JdbcTemplate jdbcTemplate;
    private ObjectMapper objectMapper;
    private Boolean isMfaEnable;
    private RedisService redisService;

    public CustomTokenEnhancer(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, Boolean isMfaEnable, RedisService redisService) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.isMfaEnable = isMfaEnable;
        this.redisService = redisService;
    }

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        Map<String, Object> additionalInfo;
        String grantType = authentication.getOAuth2Request().getRequestParameters().get("grant_type");
        String username = authentication.getName();
        RequestInfoWrapperDto requestInfoWrapperDto = getRequestInfoFromOAuth2Request(authentication.getOAuth2Request());
        if (SecurityConstant.GRANT_TYPE_EMPLOYEE.equals(grantType)) {
            AccountForTokenDto accountForTokenDto = getAccountByUsernameAndTenantId(authentication.getOAuth2Request().getRequestParameters());
            additionalInfo = getAdditionalInfoTypePass(requestInfoWrapperDto, username, grantType, accountForTokenDto);
        } else {
            additionalInfo = getAdditionalInfoTypePass(requestInfoWrapperDto, username, grantType, null);
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    private Map<String, Object> getAdditionalInfoTypePass(RequestInfoWrapperDto requestInfoWrapperDto, String username, String grantType, AccountForTokenDto form) {
        Map<String, Object> additionalInfo = new HashMap<>();
        AccountForTokenDto a;
        if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_EMPLOYEE)) {
            a = form;
        } else {
            a = getAccountByUsername(username);
        }

        if (a != null) {
            Long accountId = a.getId();
            Long restaurantId = -1L;
            String kind = a.getKind() + "";//token kind
            Long deviceId = -1L;
            String posId = MasterJwt.EMPTY_STRING;
            if (StringUtils.isNotBlank(requestInfoWrapperDto.getPosId())) {
                posId = requestInfoWrapperDto.getPosId();
                additionalInfo.put("pos_id", posId);
                if (requestInfoWrapperDto.getDeviceType() != null) {
                    additionalInfo.put("device_type", requestInfoWrapperDto.getDeviceType());
                }
            }
            String permission = "<>";//empty string
            Integer userKind = a.getKind(); // kind user là admin hay là gì
            Integer tabletKind = -1;
            Long orderId = -1L;
            Boolean isSuperAdmin = a.getIsSuperAdmin();
            Boolean isMfa = a.getIsMfa();
            String tenantInfo = requestInfoWrapperDto.getTenantId() == null ? getTenantByAccountId(a.getId()) : getTenantInfoByTenantId(requestInfoWrapperDto.getTenantId());
            if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_PASSWORD)
                    || Objects.equals(grantType, SecurityConstant.GRANT_TYPE_EMPLOYEE)) {
                additionalInfo.put("user_id", accountId);
                additionalInfo.put("user_kind", a.getKind());
            }
            grantType = grantType == null ? SecurityConstant.GRANT_TYPE_PASSWORD : grantType;
            additionalInfo.put("grant_type", grantType);
            additionalInfo.put("tenant_info", tenantInfo);
            String fullName = a.getFullName();
            if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_EMPLOYEE)) {
                String urlServerProvider = getUrlOfServerProviderByTenantId(requestInfoWrapperDto.getTenantId());
                additionalInfo.put("url", urlServerProvider);
            } else if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_PASSWORD)) {
                additionalInfo.put("isMfa", isMfa);
                additionalInfo.put("isMfaEnable", isMfaEnable);
            }
            String tenantName = requestInfoWrapperDto.getTenantId();
            if (StringUtils.isNotBlank(tenantName)) {
                additionalInfo.put("tenant_name", tenantName);
            }
            if (StringUtils.isNoneBlank(requestInfoWrapperDto.getRestaurantName())) {
                additionalInfo.put("restaurant_name", requestInfoWrapperDto.getRestaurantName());
                additionalInfo.put("restaurant_logo", requestInfoWrapperDto.getRestaurantLogo());
                additionalInfo.put("view_status", requestInfoWrapperDto.getViewStatus());
                additionalInfo.put("pos_status", requestInfoWrapperDto.getPosStatus());
                additionalInfo.put("pos_time_last_used", requestInfoWrapperDto.getPosTimeLastUsed());
            }
            String DELIM = "|";
            String additionalInfoStr = ZipUtils.zipString(accountId + DELIM
                    + restaurantId + DELIM
                    + kind + DELIM
                    + permission + DELIM
                    + deviceId + DELIM
                    + posId + DELIM
                    + userKind + DELIM
                    + username + DELIM
                    + fullName + DELIM
                    + tabletKind + DELIM
                    + orderId + DELIM
                    + isSuperAdmin + DELIM
                    + tenantInfo);
            additionalInfo.put("additional_info", additionalInfoStr);
            String randomString = GenerateUtils.generateRandomString(6);
            Date date = new Date();
            String sessionId= Md5Utils.hash(username + date + randomString);
            additionalInfo.put("session_id", sessionId);
            // Set session for device
//            DeviceSessionRequest deviceSessionRequest = new DeviceSessionRequest();
//            deviceSessionRequest.setSession(sessionId);
//            deviceSessionRequest.setTime(DateUtils.formatDate(date));
//            String key = "";
//            if (StringUtils.isNotBlank(posId) && !posId.equals(MasterJwt.EMPTY_STRING)) {
//                if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_POS)) {
//                    key = redisService.getKeyString(RedisService.KEY_POS, tenantName, posId);
//                } else if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_VIEW)) {
//                    key = redisService.getKeyString(RedisService.KEY_VIEW, tenantName, posId);
//                } else {
//                    key = redisService.getKeyString(RedisService.KEY_EMPLOYEE, tenantName, posId);
//                    String loginType = requestInfoWrapperDto.getLoginType();
//                    if (StringUtils.isNoneBlank(loginType) && loginType.equals(MasterConstant.LOGIN_TYPE_POS)) {
//                        DeviceSessionRequest request = new DeviceSessionRequest();
//                        request.setEmployee(a.getId().toString());
//                        String keyPos = redisService.getKeyString(RedisService.KEY_POS, tenantName, posId);
//                        redisService.put(keyPos, request);
//                    }
//                }
//                redisService.put(key, deviceSessionRequest);
//            }
        }
        return additionalInfo;
    }

    public AccountForTokenDto getAccountByUsername(String username) {
        try {
            String query = "SELECT a.id, a.kind, a.username, a.email, a.full_name, a.is_super_admin, a.is_mfa " +
                    "FROM db_mst_account a JOIN db_mst_group g ON a.group_id = g.id  WHERE a.username = ? AND a.status = 1 limit 1";
            log.debug(query);
            List<AccountForTokenDto> dto = jdbcTemplate.query(query, new Object[]{username}, new BeanPropertyRowMapper<>(AccountForTokenDto.class));
            if (dto.size() > 0) return dto.get(0);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public AccountForTokenDto getAccountByUsernameAndTenantId(Map<String, String> map) {
        AccountForTokenDto accountForTokenDto = new AccountForTokenDto();
        accountForTokenDto.setUsername(map.get("username"));
        accountForTokenDto.setFullName(map.get("fullName"));
        accountForTokenDto.setKind(MasterConstant.USER_KIND_EMPLOYEE);
        accountForTokenDto.setIsSuperAdmin(false);
        accountForTokenDto.setId(Long.parseLong(map.get("employeeId")));
        return accountForTokenDto;
    }

    public String getTenantByAccountId(Long accountId) {
        try {
            String query = "select distinct coalesce(GROUP_CONCAT(CONCAT(d.name, \"&\", d.restaurant_id) SEPARATOR ':'), '') " +
                    "from db_mst_restaurant r " +
                    "join db_mst_db_config d on r.id = d.restaurant_id " +
                    "where r.customer_id = ? and r.status = 1 and r.expire_date > NOW()";
            log.debug(query);
            return jdbcTemplate.queryForObject(query, String.class, accountId) == null
                    ? "" : jdbcTemplate.queryForObject(query, String.class, accountId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getTenantInfoByTenantId(String tenantId) {
        try {
            String query = "select distinct coalesce(GROUP_CONCAT(CONCAT(d.name, \"&\", d.restaurant_id) SEPARATOR ':'), '') " +
                    "from db_mst_restaurant r " +
                    "join db_mst_db_config d on r.id = d.restaurant_id " +
                    "where r.tenant_id = ? and r.status = 1 and r.expire_date > NOW()";
            return jdbcTemplate.queryForObject(query, String.class, tenantId) == null
                    ? "" : jdbcTemplate.queryForObject(query, String.class, tenantId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getUrlOfServerProviderByTenantId(String tenantId) {
        try {
            String query = "SELECT dsp.url FROM db_mst_restaurant dr " +
                    "INNER JOIN db_mst_db_config ddc ON ddc.restaurant_id = dr.id " +
                    "INNER JOIN db_mst_server_provider dsp ON dsp.id  = ddc.server_provider_id " +
                    "WHERE dr.tenant_id = ? and dr.status = 1 and dr.expire_date > NOW()";
            return jdbcTemplate.queryForObject(query, String.class, tenantId) == null
                    ? "" : jdbcTemplate.queryForObject(query, String.class, tenantId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public RequestInfoWrapperDto getRequestInfoFromOAuth2Request(OAuth2Request oAuth2Request) {
        RequestInfoWrapperDto requestInfoWrapperDto = new RequestInfoWrapperDto();
        requestInfoWrapperDto.setTenantId(oAuth2Request.getRequestParameters().get("tenantId"));
        requestInfoWrapperDto.setRestaurantName(oAuth2Request.getRequestParameters().get("restaurantName"));
        requestInfoWrapperDto.setRestaurantLogo(oAuth2Request.getRequestParameters().get("restaurantLogo"));
        requestInfoWrapperDto.setPosId(oAuth2Request.getRequestParameters().get("posId"));
        requestInfoWrapperDto.setLoginType(oAuth2Request.getRequestParameters().get("loginType"));
        requestInfoWrapperDto.setPosTimeLastUsed(oAuth2Request.getRequestParameters().get("posTimeLastUsed"));
        Integer deviceType = null;
        Integer posStatus = null;
        Integer viewStatus = null;
        try {
            deviceType = Integer.parseInt(oAuth2Request.getRequestParameters().get("deviceType"));
            posStatus = Integer.parseInt(oAuth2Request.getRequestParameters().get("posStatus"));
            viewStatus = Integer.parseInt(oAuth2Request.getRequestParameters().get("viewStatus"));
        } catch (Exception ignored) {}
        requestInfoWrapperDto.setDeviceType(deviceType);
        requestInfoWrapperDto.setPosStatus(posStatus);
        requestInfoWrapperDto.setViewStatus(viewStatus);
        return requestInfoWrapperDto;
    }
}
