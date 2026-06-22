package com.epam.rd.autocode.assessment.appliances.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginRateLimitAuthenticationProvider implements AuthenticationProvider {

    private final LoginAttemptService loginAttemptService;

    @Override
    public Authentication authenticate(Authentication authentication) {
        LoginAttemptKey key = LoginAttemptKey.of(authentication);
        if (loginAttemptService.isBlocked(key.credentialKey(), key.ip())) {
            throw new LockedException("Too many failed login attempts. Try again later.");
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
