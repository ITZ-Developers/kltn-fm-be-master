package com.master.service.impl;

import com.master.config.SecurityConstant;
import com.master.constant.MasterConstant;
import com.master.dto.ErrorCode;
import com.master.exception.BadRequestException;
import com.master.feign.FeignConstant;
import com.master.model.Account;
import com.master.model.Permission;
import com.master.repository.AccountRepository;
import com.master.jwt.MasterJwt;
import com.master.repository.LocationRepository;
import com.master.repository.PermissionRepository;
import com.master.service.TotpManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
@Service(value = "userService")
@Slf4j
public class UserServiceImpl implements UserDetailsService {
    public ThreadLocal<String> tenantId = new InheritableThreadLocal<>();
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Value("${mfa.enabled}")
    private Boolean isMfaEnable;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private TotpManager totpManager;
    @Autowired
    private LocationRepository locationRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) {
        Account user = accountRepository.findFirstByUsername(userId).orElse(null);
        if (user == null) {
            log.error("Invalid username or password!");
            throw new UsernameNotFoundException("[General] Invalid username or password!");
        }
        boolean enabled = true;
        if (user.getStatus() != 1) {
            log.error("User had been locked!");
            enabled = false;
        }
        Set<GrantedAuthority> grantedAuthorities;
        if (Boolean.FALSE.equals(isMfaEnable) || Objects.equals(user.getKind(), MasterConstant.USER_KIND_EMPLOYEE)) {
            grantedAuthorities = getAccountPermission(user);
        } else {
            if (user.getIsMfa() != null && user.getIsMfa().equals(true)) {
                grantedAuthorities = getAccountPermission(user);
            } else {
                grantedAuthorities = new HashSet<>();
                List<String> basicRoles = new ArrayList<>(Arrays.asList("ACC_TOTP"));
                if (Boolean.TRUE.equals(user.getIsSuperAdmin())) {
                    basicRoles.add("ACC_D_MFA");
                }
                basicRoles.forEach(role -> grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
            }
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), enabled, true, true, true, grantedAuthorities);
    }

    public Set<GrantedAuthority> getAccountPermission(Account user) {
        List<String> roles = new ArrayList<>();
        List<Permission> permissions = user.getGroup().getPermissions();
        permissions.stream().filter(f -> f.getPermissionCode() != null).forEach(pName -> roles.add(pName.getPermissionCode()));
        return roles.stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())).collect(Collectors.toSet());
    }

    public MasterJwt getAddInfoFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                Map<String, Object> map = (Map<String, Object>) oauthDetails.getDecodedDetails();
                String encodedData = (String) map.get("additional_info");
                //idStr -> json
                if (encodedData != null && !encodedData.isEmpty()) {
                    return MasterJwt.decode(encodedData);
                }
                return null;
            }
        }
        return null;
    }

    public List<String> getAuthoritiesFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            return authorities.stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public String getCurrentToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication instanceof AnonymousAuthenticationToken) ? null : ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
    }

    public String getTenantInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                Map<String, Object> map = (Map<String, Object>) oauthDetails.getDecodedDetails();
                return (String) map.get("tenant_info");
            }
        }
        return null;
    }

    public OAuth2AccessToken getAccessTokenForMultipleTenancies(ClientDetails client, TokenRequest tokenRequest, String username, String password, String tenant, String totp, AuthorizationServerTokenServices tokenServices) throws GeneralSecurityException, IOException {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("grant_type", tokenRequest.getGrantType());
        requestParameters.put("tenantId", tokenRequest.getRequestParameters().get("tenantId"));

        String clientId = client.getClientId();
        boolean approved = true;
        Set<String> responseTypes = new HashSet<>();
        responseTypes.add("code");
        Map<String, Serializable> extensionProperties = new HashMap<>();

        UserDetails userDetails = loadUserByUsername(username, password, totp, tokenRequest.getGrantType(), tenant);
        OAuth2Request oAuth2Request = new OAuth2Request(requestParameters, clientId,
                userDetails.getAuthorities(), approved, client.getScope(),
                client.getResourceIds(), null, responseTypes, extensionProperties);
        org.springframework.security.core.userdetails.User userPrincipal = new org.springframework.security.core.userdetails.User(userDetails.getUsername(), userDetails.getPassword(), userDetails.isEnabled(), userDetails.isAccountNonExpired(), userDetails.isCredentialsNonExpired(), userDetails.isAccountNonLocked(), userDetails.getAuthorities());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userPrincipal, null, userDetails.getAuthorities());
        OAuth2Authentication auth = new OAuth2Authentication(oAuth2Request, authenticationToken);
        return tokenServices.createAccessToken(auth);
    }

    public UserDetails loadUserByUsername(String username, String password, String totp, String grantedType, String tenant) {
        Account user = accountRepository.findFirstByUsername(username).orElse(null);
        if (user == null
                || !Objects.equals(MasterConstant.STATUS_ACTIVE, user.getStatus())
                || !passwordEncoder.matches(password, user.getPassword())) {
            log.error("[General] Invalid username or password!");
            throw new BadRequestException("[General] Invalid username or password!", ErrorCode.GENERAL_ERROR_INVALID_USERNAME_OR_PASSWORD);
        }
        if (Objects.equals(user.getKind(), MasterConstant.USER_KIND_CUSTOMER)) {
//            String tenantInfo = restaurantRepository.findTenantInfoOfCustomer(user.getId());
//            if (tenantInfo == null || tenantInfo.isEmpty()) {
//                log.error("[General] Customer of Restaurant does not have Db Config!");
//                throw new BadRequestException("[General] Customer of Restaurant does not have Db Config!", ErrorCode.GENERAL_ERROR_RESTAURANT_DOES_NOT_HAVE_DB_CONFIG);
//            }
        }
        if (grantedType.equals(SecurityConstant.GRANT_TYPE_EMPLOYEE) && !Objects.equals(user.getKind(), MasterConstant.USER_KIND_EMPLOYEE)) {
            log.error("[General] Invalid login by employee");
            throw new BadRequestException("[General] Invalid login by employee", ErrorCode.GENERAL_ERROR_INVALID_LOGIN_BY_EMPLOYEE);
        }
        boolean enabled = true;
        Set<GrantedAuthority> grantedAuthorities = getAccountPermission(user);
        if (Boolean.TRUE.equals(isMfaEnable) && Objects.equals(grantedType, SecurityConstant.GRANT_TYPE_PASSWORD)) {
            checkMFA(user, totp);
            if (Boolean.FALSE.equals(user.getIsMfa())) {
                user.setIsMfa(true);
                accountRepository.save(user);
            }
        }
        return new org.springframework.security.core.userdetails.User(username, user.getPassword(), enabled, true, true, true, grantedAuthorities);
    }

    private void checkMFA(Account user, String totp) {
        if (totp == null) {
            throw new BadRequestException("TOTP is required", ErrorCode.GENERAL_ERROR_TOTP_REQUIRED);
        }
        if (user.getSecretKey() == null) {
            throw new BadRequestException("Account not setup TOTP", ErrorCode.GENERAL_ERROR_ACCOUNT_NOT_SET_UP_2FA);
        }
        boolean isVerified = totpManager.verifyCode(totp, user.getSecretKey());
        if (!isVerified) {
            throw new BadRequestException("Verify TOTP failed", ErrorCode.GENERAL_ERROR_VERIFY_TOTP_FAILED);
        }
    }

    public String getAttributeFromToken(String attribute) {
        Map<String, Object> map = getAttributeFromToken();
        if (map != null) {
            return String.valueOf(map.get(attribute));
        }
        return null;
    }

    public Map<String, Object> getAttributeFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            OAuth2AuthenticationDetails oauthDetails =
                    (OAuth2AuthenticationDetails) authentication.getDetails();
            if (oauthDetails != null) {
                return (Map<String, Object>) oauthDetails.getDecodedDetails();
            }
        }
        return null;
    }

    public String getBearerTokenHeader() {
        return FeignConstant.AUTH_BEARER_TOKEN + " " + getCurrentToken();
    }
}
