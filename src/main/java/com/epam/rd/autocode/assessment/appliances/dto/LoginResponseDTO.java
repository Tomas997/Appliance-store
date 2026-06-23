package com.epam.rd.autocode.assessment.appliances.dto;

import lombok.Getter;

@Getter
public class LoginResponseDTO {
    private final String token;
    private final String tokenType = "Bearer";

    public LoginResponseDTO(String token) {
        this.token = token;
    }
}
