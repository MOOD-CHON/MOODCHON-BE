package com.example.moodchon.auth;

import com.example.moodchon.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

public class CustomOidcUser implements OidcUser, UserPrincipal {

    private final Long userId;
    private final String role;
    private final OidcUser oidcUser;

    public CustomOidcUser(User user, OidcUser oidcUser) {
        this.userId = user.getId();
        this.role = user.getRole().name();
        this.oidcUser = oidcUser;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public Map<String, Object> getClaims() {
        return oidcUser.getClaims();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUser.getUserInfo();
    }

    @Override
    public OidcIdToken getIdToken() {
        return oidcUser.getIdToken();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oidcUser.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}