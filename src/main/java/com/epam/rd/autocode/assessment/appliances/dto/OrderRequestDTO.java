package com.epam.rd.autocode.assessment.appliances.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequestDTO {
    private Long employeeId;

    @NotNull(message = "{order.client.is.mandatory}")
    private Long clientId;
}
