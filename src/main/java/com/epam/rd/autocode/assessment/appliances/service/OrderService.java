package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.OrderRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface OrderService {
    List<OrderResponseDTO> findAll();
    Page<OrderResponseDTO> findAll(Pageable pageable);
    void saveOrder(OrderRequestDTO dto);
    OrderRequestDTO findById(Long id);
    void updateOrder(Long id, OrderRequestDTO dto);
    void deleteOrderById(Long id);
    void approveOrder(Long id, boolean approved);
    void addRowToOrder(Long orderId, Long applianceId, Long number, BigDecimal price);
    void deleteRowFromOrder(Long rowId);
    Set<OrderRow> getOrderRows(Long orderId);
}
