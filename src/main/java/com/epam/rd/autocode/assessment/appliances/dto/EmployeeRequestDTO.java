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
public class EmployeeRequestDTO {
    @NotBlank(message = "{user.name.is.mandatory}")
    private String name;

    @NotBlank(message = "{user.email.is.blank}")
    @Email(message = "{user.email.is.correctly}")
    private String email;

    @NotBlank(message = "{user.password.is.correctly}")
    @StrongPassword
    private String password;

    @NotBlank(message = "{employee.department.is.mandatory}")
    private String department;
}
