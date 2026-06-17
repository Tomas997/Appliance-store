package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    Page<Orders> findByClient_EmailAndStatusNot(String email, OrderStatus status, Pageable pageable);
    Page<Orders> findByClient_EmailAndStatus(String email, OrderStatus status, Pageable pageable);
    Page<Orders> findByStatus(OrderStatus status, Pageable pageable);
    Page<Orders> findByStatusNotIn(Collection<OrderStatus> statuses, Pageable pageable);
}
