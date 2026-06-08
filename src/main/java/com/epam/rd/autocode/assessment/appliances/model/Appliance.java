package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Appliance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Enumerated(EnumType.STRING)
    private Category category;
    private String model;
    @ManyToOne
    private Manufacturer manufacturer;
    @Enumerated(EnumType.STRING)
    private PowerType powerType;
    private String characteristic;
    private String description;
    private Integer power;
    private BigDecimal price;
}
