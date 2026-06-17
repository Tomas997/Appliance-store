package com.epam.rd.autocode.assessment.appliances.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class OrderRowPriceChangeDTO {
    private String applianceName;
    private BigDecimal oldAmount;
    private BigDecimal newAmount;
}
