package com.epam.rd.autocode.assessment.appliances.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordPolicyValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "{user.password.weak}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}