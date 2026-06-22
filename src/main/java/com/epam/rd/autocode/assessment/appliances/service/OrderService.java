package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderRowPriceChangeDTO;
import com.epam.rd.autocode.assessment.appliances.model.OrderRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface OrderService {
    List<OrderResponseDTO> findAll();
    Page<OrderResponseDTO> findAll(Pageable pageable);
    Page<OrderResponseDTO> findCancelled(Pageable pageable);
    Page<OrderResponseDTO> findByClientEmail(String email, Pageable pageable);
    Page<OrderResponseDTO> findCancelledByClientEmail(String email, Pageable pageable);
    Page<OrderResponseDTO> findPendingForDelivery(Pageable pageable);
    Page<OrderResponseDTO> findDelivering(Pageable pageable);
    Page<OrderResponseDTO> findDelivered(Pageable pageable);
    void saveOrder(OrderRequestDTO dto);
    Long createClientOrder(String clientEmail);
    OrderRequestDTO findById(Long id);
    OrderResponseDTO findResponseById(Long id);
    void updateOrder(Long id, OrderRequestDTO dto);
    void deleteOrderById(Long id);
    void approveByEmployee(Long id, String note, String employeeEmail);
    void acceptByDeliverer(Long id, String delivererEmail);
    void markAsDelivered(Long id, String delivererEmail);
    List<OrderRowPriceChangeDTO> submitForReview(Long id);
    void requestRevision(Long id, String note, String employeeEmail);
    void cancelOrder(Long id, String reason, String cancelledByEmail);
    void addRowToOrder(Long orderId, Long applianceId, Long number);
    void deleteRowFromOrder(Long rowId);
    Set<OrderRow> getOrderRows(Long orderId);
}
