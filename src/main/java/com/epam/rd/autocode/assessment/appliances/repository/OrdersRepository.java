package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByClient_Email(String email, Pageable pageable);
    Page<Orders> findByStatus(OrderStatus status, Pageable pageable);
    Page<Orders> findByStatusNot(OrderStatus status, Pageable pageable);
}
