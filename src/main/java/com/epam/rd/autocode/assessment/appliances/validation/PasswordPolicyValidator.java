package com.epam.rd.autocode.assessment.appliances.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<StrongPassword, String> {

    private static final String PATTERN = "^(?=.*[A-Z])(?=.*\\d).{8,}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        return value.matches(PATTERN);
    }
}