package com.master.config;

import com.master.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Component
@SuppressWarnings("unchecked")
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private UserDetailsService customUserDetailsService;
    @Value("${mfa.enabled}")
    private Boolean isMfaEnable;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        Map<String, String> detailsMap = (Map<String, String>) authentication.getDetails();
        String grantType = detailsMap.get("grant_type");
        String totp = null;
        String tenantId = detailsMap.get("tenantId");
        if (Boolean.TRUE.equals(isMfaEnable) && Objects.equals(grantType, SecurityConstant.GRANT_TYPE_PASSWORD)) {
            totp = detailsMap.get("totp");
        }
        UserDetails userDetails = ((UserServiceImpl) customUserDetailsService).loadUserByUsername(username, password, totp, grantType, tenantId);
        return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
