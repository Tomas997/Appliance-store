package com.epam.rd.autocode.assessment.appliances.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

record LoginAttemptKey(String credentialKey, String ip) {

    static LoginAttemptKey of(Authentication authentication) {
        String username = authentication.getName() == null ? "" : authentication.getName().toLowerCase();
        String ip = "unknown";
        if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
            ip = details.getRemoteAddress();
        }
        return new LoginAttemptKey(username + "|" + ip, ip);
    }
}
