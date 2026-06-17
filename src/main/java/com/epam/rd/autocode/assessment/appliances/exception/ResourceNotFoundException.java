package com.epam.rd.autocode.assessment.appliances.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String entity, Long id) {
        super(entity + " not found with id: " + id);
    }

    public ResourceNotFoundException(String entity, String key) {
        super(entity + " not found: " + key);
    }
}