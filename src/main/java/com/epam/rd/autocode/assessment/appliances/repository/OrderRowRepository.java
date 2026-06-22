package com.epam.rd.autocode.assessment.appliances.repository;

import com.epam.rd.autocode.assessment.appliances.model.OrderRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Repository
public interface OrderRowRepository extends JpaRepository<OrderRow,Long> {

    @Query("SELECT r.order.id AS orderId, COALESCE(SUM(r.amount), 0) AS totalAmount FROM OrderRow r " +
            "WHERE r.order.id IN :orderIds GROUP BY r.order.id")
    List<OrderAmountProjection> sumAmountsByOrderIds(@Param("orderIds") Collection<Long> orderIds);

    interface OrderAmountProjection {
        Long getOrderId();
        BigDecimal getTotalAmount();
    }
}
