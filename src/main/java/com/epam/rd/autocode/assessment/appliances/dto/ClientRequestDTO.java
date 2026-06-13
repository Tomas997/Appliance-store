package com.epam.rd.autocode.assessment.appliances.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@NoArgsConstructor
public class ClientRequestDTO {
    @NotBlank(message = "{user.name.is.mandatory}")
    private String name;

    @NotBlank(message = "{user.email.is.blank}")
    @Email(message = "{user.email.is.correctly}")
    private String email;

    @Length(min = 8, message = "{user.password.too.short}")
    @NotBlank(message = "{user.password.is.correctly}")
    private String password;

    @NotBlank(message = "{client.card.is.mandatory}")
    private String card;

}
