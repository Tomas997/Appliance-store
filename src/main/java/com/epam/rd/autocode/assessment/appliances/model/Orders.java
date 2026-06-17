package com.epam.rd.autocode.assessment.appliances.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @ManyToOne
    private Employee employee;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Client client;

    @ManyToOne
    private Deliverer deliverer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderRow> orderRowSet = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING_EMPLOYEE;

    private String employeeNote;
}
