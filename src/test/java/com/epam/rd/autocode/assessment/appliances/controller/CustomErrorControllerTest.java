package com.epam.rd.autocode.assessment.appliances.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CustomErrorControllerTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CustomErrorController customErrorController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(customErrorController).build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("GET /error: 403 без автентифікації — повинен перенаправити на /login?csrf=true")
    void handleError_whenForbiddenAndAnonymous_shouldRedirectToLoginWithCsrf() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken("key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?csrf=true"));
    }

    @Test
    @DisplayName("GET /error: 403 без жодної автентифікації в контексті — теж повинен перенаправити на /login?csrf=true")
    void handleError_whenForbiddenAndNoAuthentication_shouldRedirectToLoginWithCsrf() throws Exception {
        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?csrf=true"));
    }

    @Test
    @DisplayName("GET /error: 403 для автентифікованого користувача — повинен показати сторінку відмови в доступі")
    void handleError_whenForbiddenAndAuthenticated_shouldShowAccessDeniedPage() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("employee@store.com", "password",
                        List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE"))));
        when(messageSource.getMessage(eq("error.access.denied"), isNull(), any()))
                .thenReturn("You don't have permission to access this page.");

        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 403))
                .andExpect(status().isOk())
                .andExpect(view().name("error/not-found"))
                .andExpect(model().attribute("errorMessage", "You don't have permission to access this page."));
    }

    @Test
    @DisplayName("GET /error: без коду статусу (інша помилка) — повинен показати загальне повідомлення")
    void handleError_whenNoStatusAttribute_shouldShowUnexpectedErrorPage() throws Exception {
        when(messageSource.getMessage(eq("error.unexpected"), isNull(), any()))
                .thenReturn("An unexpected error occurred.");

        mockMvc.perform(get("/error"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/not-found"))
                .andExpect(model().attribute("errorMessage", "An unexpected error occurred."));
    }

    @Test
    @DisplayName("GET /error: 500 — повинен показати загальне повідомлення")
    void handleError_whenInternalServerError_shouldShowUnexpectedErrorPage() throws Exception {
        when(messageSource.getMessage(eq("error.unexpected"), isNull(), any()))
                .thenReturn("An unexpected error occurred.");

        mockMvc.perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isOk())
                .andExpect(view().name("error/not-found"))
                .andExpect(model().attribute("errorMessage", "An unexpected error occurred."));
    }
}
