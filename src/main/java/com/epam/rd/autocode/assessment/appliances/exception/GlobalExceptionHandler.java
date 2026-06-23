package com.epam.rd.autocode.assessment.appliances.exception;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@AllArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    private String msg(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        log.warn("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/not-found";
    }

    @ExceptionHandler(InvalidOrderStateException.class)
    public String handleInvalidOrderState(InvalidOrderStateException ex, Model model) {
        log.warn("Invalid order state: {}", ex.getMessage());
        model.addAttribute("errorMessage", msg("error.invalid.order.state"));
        return "error/not-found";
    }

    @ExceptionHandler(EmailAlreadyInUseException.class)
    public String handleEmailAlreadyInUse(EmailAlreadyInUseException ex, Model model) {
        log.warn("Registration rejected: {}", ex.getMessage());
        model.addAttribute("errorMessage", msg("error.registration.failed"));
        return "error/not-found";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException ex, Model model) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        model.addAttribute("errorMessage", msg("error.data.integrity"));
        return "error/not-found";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, Model model) {
        log.warn("Invalid identifier: {}", ex.getMessage());
        model.addAttribute("errorMessage", msg("error.invalid.id"));
        return "error/not-found";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFound(NoResourceFoundException ex) {
        // expected/benign — e.g. browser auto-requesting /favicon.ico when none is configured
        log.debug("Static resource not found: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unexpected error", ex);
        model.addAttribute("errorMessage", msg("error.unexpected"));
        return "error/not-found";
    }
}
