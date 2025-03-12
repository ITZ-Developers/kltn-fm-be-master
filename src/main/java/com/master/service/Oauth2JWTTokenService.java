package com.master.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.master.config.CustomTokenEnhancer;
import com.master.config.SecurityConstant;
import com.master.constant.MasterConstant;
import com.master.dto.auth.OauthClientDetailsDto;
import com.master.dto.auth.RequestInfoWrapperDto;
import com.master.exception.BadRequestException;
import com.master.model.Account;
import com.master.redis.RedisService;
import com.master.repository.AccountRepository;
import com.master.repository.PermissionRepository;
import com.master.service.impl.UserServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class Oauth2JWTTokenService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DefaultTokenServices tokenServices;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserServiceImpl userService;
    @Value("${mfa.enabled}")
    private Boolean isMfaEnable;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtAccessTokenConverter accessTokenConverter;
    @Value("${security.customer.client.id}")
    private String customerClientId;
    @Autowired
    private RedisService redisService;
    @Autowired
    private PermissionRepository permissionRepository;

//    public OAuth2AccessToken generateAccessToken(UserDetails userPrincipal, OauthClientDetailsDto clientDetails, String grantType, RequestInfoWrapperDto requestInfoWrapperDto, Account account) {
//        try {
//            OAuth2Authentication authentication = convertAuthentication(userPrincipal, clientDetails, grantType, requestInfoWrapperDto, account);
//            TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
//            tokenEnhancerChain.setTokenEnhancers(Arrays.asList(new CustomTokenEnhancer(jdbcTemplate, objectMapper, isMfaEnable, redisService), accessTokenConverter));
//            tokenServices.setTokenEnhancer(tokenEnhancerChain);
//            tokenServices.setReuseRefreshToken(false);
//            tokenServices.setSupportRefreshToken(true);
//            tokenServices.setAccessTokenValiditySeconds(clientDetails.getAccessTokenValidInSeconds());
//            tokenServices.setRefreshTokenValiditySeconds(clientDetails.getRefreshTokenValidInSeconds());
//            return tokenServices.createAccessToken(authentication);
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return null;
//        }
//    }
//
//    private OAuth2Authentication convertAuthentication(UserDetails userDetails, OauthClientDetailsDto clientDetails, String grantType, RequestInfoWrapperDto requestInfoWrapperDto, Account account) {
//        Map<String, String> requestParameters = new HashMap<>();
//        requestParameters.put("grant_type", grantType);
//        if (requestInfoWrapperDto != null) {
//            requestParameters.put("tenantId", requestInfoWrapperDto.getTenantId());
//            requestParameters.put("restaurantId", String.valueOf(requestInfoWrapperDto.getRestaurantId()));
//            requestParameters.put("restaurantName", requestInfoWrapperDto.getRestaurantName());
//            requestParameters.put("restaurantLogo", requestInfoWrapperDto.getRestaurantLogo());
//            requestParameters.put("posId", requestInfoWrapperDto.getPosId());
//            requestParameters.put("deviceType", String.valueOf(requestInfoWrapperDto.getDeviceType()));
//            requestParameters.put("loginType", requestInfoWrapperDto.getLoginType());
//            requestParameters.put("posStatus", String.valueOf(requestInfoWrapperDto.getPosStatus()));
//            requestParameters.put("viewStatus", String.valueOf(requestInfoWrapperDto.getViewStatus()));
//            requestParameters.put("posTimeLastUsed", requestInfoWrapperDto.getPosTimeLastUsed());
//        }
//        if (account != null) {
//            requestParameters.put("username", StringUtils.isNotBlank(account.getUsername()) ? account.getUsername() : userDetails.getUsername());
//            if (StringUtils.isNotBlank(account.getFullName())) {
//                requestParameters.put("fullName", account.getFullName());
//            }
//            if (account.getId() != null) {
//                requestParameters.put("employeeId", account.getId().toString());
//            }
//        }
//        Set<String> scope = new HashSet<>();
//        String[] scopeArray = clientDetails.getScope().split(",");
//        Collections.addAll(scope, scopeArray);
//        Set<String> responseTypes = new HashSet<>();
//        responseTypes.add("code");
//        Map<String, Serializable> extensionProperties = new HashMap<>();
//        OAuth2Request request = new OAuth2Request(requestParameters, clientDetails.getClientId(), userDetails.getAuthorities(), true, scope, null,
//                null, responseTypes, extensionProperties);
//        return new OAuth2Authentication(request, new UsernamePasswordAuthenticationToken(userDetails, "N/A", userDetails.getAuthorities()));
//    }
//
//    public OauthClientDetailsDto getOauthClientDetails(String clientId){
//        try {
//            String query = "SELECT client_id, scope, authorized_grant_types, access_token_validity, refresh_token_validity " +
//                    "FROM oauth_client_details WHERE client_id = '" + clientId + "' LIMIT 1";
//            OauthClientDetailsDto oauthClientDetailsDto = jdbcTemplate.queryForObject(query,
//                    (resultSet, rowNum) -> new OauthClientDetailsDto(resultSet.getString("client_id"),
//                            resultSet.getString("scope"), resultSet.getString("authorized_grant_types"),
//                            resultSet.getInt("access_token_validity"), resultSet.getInt("refresh_token_validity")));
//            return oauthClientDetailsDto;
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            return null;
//        }
//    }
//
//    public OAuth2AccessToken getAccessTokenByGrantType(String username, String grantType, String clientId, RequestInfoWrapperDto requestInfoWrapperDto, LoginEmployeeForm form) {
//        OauthClientDetailsDto clientDetails = getOauthClientDetails(clientId);
//        if (clientDetails == null) {
//            throw new BadRequestException("[General] Not found clientId");
//        }
//        if (!clientDetails.getAuthorizedGrantTypes().contains(grantType)) {
//            throw new BadRequestException("[General] Client not contain this grant type");
//        }
//        Set<GrantedAuthority> grantedAuthorities;
//        List<String> pCodes;
//        Account user = null;
//        if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_EMPLOYEE)) {
//            user = new Account();
//            user.setPassword(form.getSecretKey());
//            user.setId(form.getEmployeeId());
//            user.setUsername(username);
//            user.setFullName(form.getFullName());
//            user.setStatus(MasterConstant.STATUS_ACTIVE);
//            user.setKind(MasterConstant.USER_KIND_EMPLOYEE);
//            pCodes = permissionRepository.findPermissionCodesByGroupKindAndIdIn(MasterConstant.GROUP_KIND_EMPLOYEE, true, form.getPermissionIds());
//        } else if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_VIEW)) {
//            if (clientId.equals(customerClientId)) {
//                pCodes = List.of("VIEW_V_T");
//            } else {
//                pCodes = devicePermissionRepository.findAllPermissionCodeByTenantIdAndPosIdAndType(
//                        requestInfoWrapperDto.getTenantId(),
//                        requestInfoWrapperDto.getPosId(),
//                        requestInfoWrapperDto.getDeviceType()
//                );
//            }
//        } else {
//            pCodes = permissionRepository.findPermissionCodesByUsername(username, true);
//        }
//        grantedAuthorities = pCodes.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).collect(Collectors.toSet());
//        boolean enabled = true;
//        String password = user != null && StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : requestInfoWrapperDto.getPosId();
//        UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, password, enabled, enabled, enabled, enabled, grantedAuthorities);
//        return generateAccessToken(userDetails, clientDetails, grantType, requestInfoWrapperDto, user);
//    }
}
