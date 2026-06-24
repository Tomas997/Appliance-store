package com.epam.rd.autocode.assessment.appliances.controller.api;

import com.epam.rd.autocode.assessment.appliances.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthRestControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthRestController authRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authRestController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login: правильні дані — повинен повернути 200 і JWT-токен")
    void login_validCredentials_shouldReturnToken() throws Exception {
        User principal = new User("client@store.com", "encoded",
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        UsernamePasswordAuthenticationToken authenticated =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(authenticated);
        when(jwtService.generateToken(principal)).thenReturn("jwt-token-value");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@store.com\",\"password\":\"Secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-value"));

        ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertThatCredentialsMatch(captor.getValue());
    }

    private void assertThatCredentialsMatch(UsernamePasswordAuthenticationToken token) {
        org.assertj.core.api.Assertions.assertThat(token.getName()).isEqualTo("client@store.com");
        org.assertj.core.api.Assertions.assertThat(token.getCredentials()).isEqualTo("Secret123");
    }

    @Test
    @DisplayName("POST /api/auth/login: неправильний пароль — повинен повернути 401")
    void login_invalidCredentials_shouldReturnUnauthorized() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"client@store.com\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("POST /api/auth/login: відсутній email — повинен повернути 400")
    void login_missingEmail_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"Secret123\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationManager, jwtService);
    }
}
