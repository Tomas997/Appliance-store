package com.epam.rd.autocode.assessment.appliances.dto;

import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ApplianceRequestDTO {
    @NotBlank(message = "{appliance.name.is.mandatory}")
    private String name;

    @NotNull(message = "{appliance.category.is.mandatory}")
    private Category category;

    @NotBlank(message = "{appliance.model.is.mandatory}")
    private String model;

    @NotNull(message = "{appliance.manufacturer.is.mandatory}")
    private Long manufacturerId;

    @NotNull(message = "{appliance.powerType.is.mandatory}")
    private PowerType powerType;

    private String characteristic;
    private String description;
    private Integer power;
    private BigDecimal price;
}
