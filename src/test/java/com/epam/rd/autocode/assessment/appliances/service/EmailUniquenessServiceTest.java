package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.exception.EmailAlreadyInUseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class EmailUniquenessServiceTest {

    private EmailUniquenessService emailUniquenessService;

    @BeforeEach
    void setUp() {
        emailUniquenessService = new EmailUniquenessService();
        ReflectionTestUtils.setField(emailUniquenessService, "adminEmail", "admin@store.com");
    }

    @Test
    @DisplayName("verifyNotAdminEmail: якщо email збігається з admin.email — кинути EmailAlreadyInUseException")
    void verifyNotAdminEmail_whenMatchesAdminEmail_shouldThrow() {
        assertThatThrownBy(() -> emailUniquenessService.verifyNotAdminEmail("admin@store.com"))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    @DisplayName("verifyNotAdminEmail: порівняння без урахування регістру — кинути EmailAlreadyInUseException")
    void verifyNotAdminEmail_whenMatchesAdminEmailDifferentCase_shouldThrow() {
        assertThatThrownBy(() -> emailUniquenessService.verifyNotAdminEmail("ADMIN@STORE.COM"))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    @DisplayName("verifyNotAdminEmail: якщо email не збігається з admin.email — не кидати виняток")
    void verifyNotAdminEmail_whenDifferentEmail_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> emailUniquenessService.verifyNotAdminEmail("client@test.com"));
    }

    @Test
    @DisplayName("verifyNotAdminEmail: якщо email null — не кидати виняток")
    void verifyNotAdminEmail_whenEmailNull_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> emailUniquenessService.verifyNotAdminEmail(null));
    }
}
