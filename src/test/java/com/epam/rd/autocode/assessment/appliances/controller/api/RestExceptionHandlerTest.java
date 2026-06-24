package com.epam.rd.autocode.assessment.appliances.controller.api;

import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    @DisplayName("handleAuthentication: повинен повернути 401 з генеричним повідомленням")
    void handleAuthentication_shouldReturnUnauthorized() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAuthentication(new BadCredentialsException("bad creds"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("message", "Invalid email or password");
    }

    @Test
    @DisplayName("handleAccessDenied: повинен повернути 403 з генеричним повідомленням")
    void handleAccessDenied_shouldReturnForbidden() {
        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).containsEntry("message", "Access denied");
    }

    @Test
    @DisplayName("handleNotFound: повинен повернути 404 з повідомленням винятку")
    void handleNotFound_shouldReturnNotFoundWithExceptionMessage() {
        ResponseEntity<Map<String, String>> response =
                handler.handleNotFound(new ResourceNotFoundException("Appliance", 42L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).contains("42");
    }

    @Test
    @DisplayName("handleValidation: повинен повернути 400 з мапою помилок по полях")
    void handleValidation_shouldReturnBadRequestWithFieldErrors() {
        MapBindingResult bindingResult = new MapBindingResult(Map.of(), "dto");
        bindingResult.addError(new FieldError("dto", "email", "must not be blank"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("email", "must not be blank");
    }

    @Test
    @DisplayName("handleMalformedBody: повинен повернути 400 з генеричним повідомленням")
    void handleMalformedBody_shouldReturnBadRequest() {
        ResponseEntity<Map<String, String>> response =
                handler.handleMalformedBody(new HttpMessageNotReadableException("bad json", (HttpInputMessage) null));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Malformed or missing request body");
    }

    @Test
    @DisplayName("handleGeneral: повинен повернути 500 з генеричним повідомленням")
    void handleGeneral_shouldReturnInternalServerError() {
        ResponseEntity<Map<String, String>> response = handler.handleGeneral(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "Unexpected error");
    }
}
