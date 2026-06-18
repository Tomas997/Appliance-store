package com.epam.rd.autocode.assessment.appliances.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@AllArgsConstructor
public class CustomErrorController implements ErrorController {

    private final MessageSource messageSource;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusAttr = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int status = statusAttr != null ? (int) statusAttr : HttpStatus.INTERNAL_SERVER_ERROR.value();

        if (status == HttpStatus.FORBIDDEN.value()) {
            // CSRF token failures are thrown by CsrfFilter, which runs before
            // ExceptionTranslationFilter, so they never reach GlobalExceptionHandler
            // and land here as a plain 403 instead of redirecting to login.
            // Only treat it as a stale CSRF token when the user isn't authenticated —
            // an authenticated user hitting 403 is a real authorization denial (wrong
            // role for the resource), not an expired form, so don't bounce them to
            // login with a misleading "session expired" message.
            if (!isAuthenticated()) {
                return "redirect:/login?csrf=true";
            }
            model.addAttribute("errorMessage", messageSource.getMessage("error.access.denied", null, LocaleContextHolder.getLocale()));
            return "error/not-found";
        }

        log.warn("Unhandled error dispatch, status={}", status);
        model.addAttribute("errorMessage", messageSource.getMessage("error.unexpected", null, LocaleContextHolder.getLocale()));
        return "error/not-found";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
