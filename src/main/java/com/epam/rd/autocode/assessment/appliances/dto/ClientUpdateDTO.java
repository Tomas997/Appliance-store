package com.epam.rd.autocode.assessment.appliances.dto;

import com.epam.rd.autocode.assessment.appliances.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ClientUpdateDTO {
    @NotBlank(message = "{user.name.is.mandatory}")
    private String name;

    @NotBlank(message = "{user.email.is.blank}")
    @Email(message = "{user.email.is.correctly}")
    private String email;

    @StrongPassword
    private String password;

    @NotBlank(message = "{client.card.is.mandatory}")
    private String card;
}
