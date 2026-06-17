package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@Entity
@NoArgsConstructor
public class Deliverer extends User {

    public Deliverer(Long id, String name, String email, String password) {
        super(id, name, email, password);
    }
}
