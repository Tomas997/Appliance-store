package com.epam.rd.autocode.assessment.appliances.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private final LoginAttemptService service = new LoginAttemptService();

    @Test
    @DisplayName("isBlocked: для невідомого ключа повинен повернути false")
    void isBlocked_whenUnknownKey_shouldReturnFalse() {
        assertThat(service.isBlocked("admin@store.com|127.0.0.1", "127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("isBlocked: 4 невдалі спроби для credential+IP не повинні блокувати")
    void isBlocked_whenFourFailures_shouldNotBlock() {
        String key = "admin@store.com|127.0.0.1";
        for (int i = 0; i < 4; i++) {
            service.onFailure(key, "127.0.0.1");
        }
        assertThat(service.isBlocked(key, "127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("isBlocked: 5 невдалих спроб для credential+IP повинні заблокувати")
    void isBlocked_whenFiveFailures_shouldBlock() {
        String key = "admin@store.com|127.0.0.1";
        for (int i = 0; i < 5; i++) {
            service.onFailure(key, "127.0.0.1");
        }
        assertThat(service.isBlocked(key, "127.0.0.1")).isTrue();
    }

    @Test
    @DisplayName("isBlocked: блокування одного credential+IP не впливає на інший IP з тим самим email")
    void isBlocked_whenDifferentIp_shouldNotInheritBlock() {
        String key = "admin@store.com|127.0.0.1";
        for (int i = 0; i < 5; i++) {
            service.onFailure(key, "127.0.0.1");
        }
        assertThat(service.isBlocked("admin@store.com|10.0.0.5", "10.0.0.5")).isFalse();
    }

    @Test
    @DisplayName("onSuccess: повинен скинути лічильник credential, розблокувавши акаунт")
    void onSuccess_shouldResetCredentialCounterAndUnblock() {
        String key = "admin@store.com|127.0.0.1";
        for (int i = 0; i < 5; i++) {
            service.onFailure(key, "127.0.0.1");
        }
        assertThat(service.isBlocked(key, "127.0.0.1")).isTrue();

        service.onSuccess(key);

        assertThat(service.isBlocked(key, "127.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("isBlocked: 30 невдалих спроб з одного IP по різних акаунтах повинні заблокувати весь IP (password spraying)")
    void isBlocked_whenThirtyFailuresAcrossDifferentCredentialsFromSameIp_shouldBlockIp() {
        String ip = "203.0.113.7";
        for (int i = 0; i < 30; i++) {
            service.onFailure("user" + i + "@store.com|" + ip, ip);
        }
        assertThat(service.isBlocked("brandNewUser@store.com|" + ip, ip)).isTrue();
    }

    @Test
    @DisplayName("onSuccess: успіх одного credential на IP не розблоковує сам IP для інших акаунтів")
    void onSuccess_shouldNotResetIpLevelCounter() {
        String ip = "203.0.113.7";
        for (int i = 0; i < 30; i++) {
            service.onFailure("user" + i + "@store.com|" + ip, ip);
        }
        service.onSuccess("user0@store.com|" + ip);

        assertThat(service.isBlocked("anotherUser@store.com|" + ip, ip)).isTrue();
    }

    @Test
    @DisplayName("isBlocked: 29 невдалих спроб по IP не повинні блокувати (поріг саме 30)")
    void isBlocked_whenTwentyNineIpFailures_shouldNotBlock() {
        String ip = "198.51.100.9";
        for (int i = 0; i < 29; i++) {
            service.onFailure("user" + i + "@store.com|" + ip, ip);
        }
        assertThat(service.isBlocked("newUser@store.com|" + ip, ip)).isFalse();
    }
}
