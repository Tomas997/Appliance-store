package com.epam.rd.autocode.assessment.appliances.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class EmailAlreadyInUseException extends DataIntegrityViolationException {
    public EmailAlreadyInUseException(String email) {
        super("Email already in use: " + email);
    }
}
