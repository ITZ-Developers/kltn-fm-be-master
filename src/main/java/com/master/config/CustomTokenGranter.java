package com.master.config;

import com.master.service.impl.UserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.token.AbstractTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Objects;

public class CustomTokenGranter extends AbstractTokenGranter {
    private UserServiceImpl userService;
    private AuthenticationManager authenticationManager;

    protected CustomTokenGranter(AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
    }

    public CustomTokenGranter(AuthenticationManager authenticationManager,AuthorizationServerTokenServices tokenServices, ClientDetailsService clientDetailsService, OAuth2RequestFactory requestFactory, String grantType, UserServiceImpl userService) {
        super(tokenServices, clientDetailsService, requestFactory, grantType);
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected OAuth2Authentication getOAuth2Authentication(ClientDetails client, TokenRequest tokenRequest) {
        return super.getOAuth2Authentication(client, tokenRequest);
    }

    protected OAuth2AccessToken getAccessToken(ClientDetails client, TokenRequest tokenRequest) {
        String tenant = tokenRequest.getRequestParameters().get("tenantId");
        String username = tokenRequest.getRequestParameters().get("username");
        String password = tokenRequest.getRequestParameters().get("password");
        String totp = tokenRequest.getRequestParameters().get("totp");
        String grantType = tokenRequest.getGrantType();
        try {
            if (List.of(
                    SecurityConstant.GRANT_TYPE_PASSWORD,
                    SecurityConstant.GRANT_TYPE_CUSTOMER,
                    SecurityConstant.GRANT_TYPE_EMPLOYEE
            ).contains(grantType)) {
                throw new InvalidTokenException("[General] Invalid grant type: " + tokenRequest.getGrantType());
            }
            if (Objects.equals(grantType, SecurityConstant.GRANT_TYPE_EMPLOYEE) && StringUtils.isBlank(tenant)) {
                throw new InvalidTokenException("tenantId cannot be null");
            }
            return userService.getAccessTokenForMultipleTenancies(client, tokenRequest, username, password, tenant, totp, this.getTokenServices());
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            throw new InvalidTokenException("account or tenant invalid");
        }
    }
}
