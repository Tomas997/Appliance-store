package com.epam.rd.autocode.assessment.appliances.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || value.isBlank()) return true;
        if (value.length() < 8) return false;
        boolean hasUppercase = value.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        return hasUppercase && hasDigit;
    }
}