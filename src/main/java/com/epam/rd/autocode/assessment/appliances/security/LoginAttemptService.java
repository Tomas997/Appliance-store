package com.epam.rd.autocode.assessment.appliances.security;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS_PER_CREDENTIAL = 5;
    private static final int MAX_ATTEMPTS_PER_IP = 30;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private record Attempts(int count, Instant windowStart, Instant lockedUntil) {
    }

    private final Map<String, Attempts> attemptsByCredential = new ConcurrentHashMap<>();
    private final Map<String, Attempts> attemptsByIp = new ConcurrentHashMap<>();

    public boolean isBlocked(String credentialKey, String ip) {
        return isLocked(attemptsByCredential.get(credentialKey)) || isLocked(attemptsByIp.get(ip));
    }

    public void onFailure(String credentialKey, String ip) {
        increment(attemptsByCredential, credentialKey, MAX_ATTEMPTS_PER_CREDENTIAL);
        increment(attemptsByIp, ip, MAX_ATTEMPTS_PER_IP);
    }

    public void onSuccess(String credentialKey) {
        attemptsByCredential.remove(credentialKey);
    }

    private boolean isLocked(Attempts attempts) {
        return attempts != null && attempts.lockedUntil() != null && Instant.now().isBefore(attempts.lockedUntil());
    }

    private void increment(Map<String, Attempts> attemptsMap, String key, int maxAttempts) {
        Instant now = Instant.now();
        attemptsMap.compute(key, (k, existing) -> {
            if (existing == null || now.isAfter(existing.windowStart().plus(WINDOW))) {
                return new Attempts(1, now, null);
            }
            int count = existing.count() + 1;
            Instant lockedUntil = count >= maxAttempts ? now.plus(LOCK_DURATION) : null;
            return new Attempts(count, existing.windowStart(), lockedUntil);
        });
    }
}
