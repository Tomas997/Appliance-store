package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Працівник магазину (роль EMPLOYEE) — розглядає замовлення клієнтів (схвалення/доопрацювання).
@Setter
@Getter
@Entity
@NoArgsConstructor
public class Employee extends User {
    @NotBlank
    private String department; // відділ, у якому працює співробітник

    public Employee(Long id, String name, String email, String password, String department) {
        super(id, name, email, password);
        this.department = department;
    }
}
