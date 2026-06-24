package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.exception.EmailAlreadyInUseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailUniquenessService {

    @Value("${admin.email}")
    private String adminEmail;

    public void verifyNotAdminEmail(String email) {
        if (email != null && email.equalsIgnoreCase(adminEmail)) {
            throw new EmailAlreadyInUseException(email);
        }
    }
}
