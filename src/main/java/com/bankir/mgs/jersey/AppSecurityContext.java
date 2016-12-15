package com.bankir.mgs.jersey;

import com.bankir.mgs.User;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public class AppSecurityContext implements SecurityContext {

    private final User user;
    private String scheme;

    public AppSecurityContext(User user, String scheme) {
        this.user = user;
        this.scheme = scheme;
    }

    public Principal getUserPrincipal() {
        return user;
    }

    public boolean isUserInRole(String role) {
        return this.user.userWithRole(role);
    }

    public boolean isSecure() {
        return "https".equals(scheme);
    }

    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }


}
