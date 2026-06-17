package com.epam.rd.autocode.assessment.appliances.dto;

import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDTO {
    private Long id;
    private String employeeName;
    private String clientName;
    private String delivererName;
    private OrderStatus status;
    private String employeeNote;
    private BigDecimal totalAmount;
}
