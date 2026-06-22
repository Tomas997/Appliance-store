package com.epam.rd.autocode.assessment.appliances.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationDeniedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityEventListener {

    private final LoginAttemptService loginAttemptService;

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        LoginAttemptKey key = LoginAttemptKey.of(event.getAuthentication());
        loginAttemptService.onSuccess(key.credentialKey());
        log.info("Успішний логін: {}", event.getAuthentication().getName());
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        if (event.getException() instanceof LockedException) {
            log.warn("Спроба логіну під час блокування: {}", event.getAuthentication().getName());
            return;
        }
        LoginAttemptKey key = LoginAttemptKey.of(event.getAuthentication());
        loginAttemptService.onFailure(key.credentialKey(), key.ip());
        log.warn("Невдала спроба логіну: {} — {}",
                event.getAuthentication().getName(), event.getException().getMessage());
    }

    @EventListener
    public void onAuthorizationDenied(AuthorizationDeniedEvent<?> event) {
        log.warn("Відмова в доступі для {}: {}",
                event.getAuthentication().get().getName(), event.getAuthorizationDecision());
    }
}
