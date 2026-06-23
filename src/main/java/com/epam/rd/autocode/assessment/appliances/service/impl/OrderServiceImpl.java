package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.aspect.Loggable;
import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderRowPriceChangeDTO;
import com.epam.rd.autocode.assessment.appliances.exception.InvalidOrderStateException;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.repository.*;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {
    private final OrdersRepository ordersRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final DelivererRepository delivererRepository;
    private final ApplianceRepository applianceRepository;
    private final OrderRowRepository orderRowRepository;

    @Override
    public List<OrderResponseDTO> findAll() {
        return toDtoList(ordersRepository.findAll());
    }

    @Override
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return toDtoPage(ordersRepository.findByStatusNotIn(List.of(OrderStatus.DRAFT, OrderStatus.CANCELLED), pageable));
    }

    @Override
    public Page<OrderResponseDTO> findCancelled(Pageable pageable) {
        return toDtoPage(ordersRepository.findByStatus(OrderStatus.CANCELLED, pageable));
    }

    @Override
    public Page<OrderResponseDTO> findByClientEmail(String email, Pageable pageable) {
        return toDtoPage(ordersRepository.findByClient_EmailAndStatusNot(email, OrderStatus.CANCELLED, pageable));
    }

    @Override
    public Page<OrderResponseDTO> findCancelledByClientEmail(String email, Pageable pageable) {
        return toDtoPage(ordersRepository.findByClient_EmailAndStatus(email, OrderStatus.CANCELLED, pageable));
    }

    @Override
    public Page<OrderResponseDTO> findPendingForDelivery(Pageable pageable) {
        return toDtoPage(ordersRepository.findByStatus(OrderStatus.PENDING_DELIVERY, pageable));
    }

    @Override
    public Page<OrderResponseDTO> findDelivering(Pageable pageable) {
        return toDtoPage(ordersRepository.findByStatus(OrderStatus.DELIVERING, pageable));
    }

    @Override
    public Page<OrderResponseDTO> findDelivered(Pageable pageable) {
        return toDtoPage(ordersRepository.findByStatus(OrderStatus.DELIVERED, pageable));
    }

    @Override
    @Loggable
    @Transactional
    public void saveOrder(OrderRequestDTO dto) {
        ordersRepository.save(toEntity(dto));
    }

    @Override
    @Loggable
    @Transactional
    public Long createClientOrder(String clientEmail) {
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientEmail));
        Orders order = new Orders();
        order.setClient(client);
        order.setStatus(OrderStatus.DRAFT);
        return ordersRepository.save(order).getId();
    }

    @Override
    public OrderResponseDTO findResponseById(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        Map<Long, BigDecimal> totals = totalAmountsByOrderId(List.of(order));
        return toDto(order, totals.getOrDefault(id, BigDecimal.ZERO));
    }

    @Override
    public OrderRequestDTO findById(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(order.getEmployee() != null ? order.getEmployee().getId() : null);
        dto.setClientId(order.getClient() != null ? order.getClient().getId() : null);
        dto.setEmployeeNote(order.getEmployeeNote());
        return dto;
    }

    @Override
    @Loggable
    @Transactional
    public void updateOrder(Long id, OrderRequestDTO dto) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() == OrderStatus.PENDING_DELIVERY || order.getStatus() == OrderStatus.DELIVERING
                || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot edit an order that is pending delivery, being delivered, or already delivered");
        }
        // employee is assigned automatically from whoever approves/requests revision
        // (see approveByEmployee/requestRevision) — not manually editable here.
        ordersRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    public void deleteOrderById(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() == OrderStatus.DELIVERING || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot delete an order that is being delivered or already delivered");
        }
        ordersRepository.deleteById(id);
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public void approveByEmployee(Long id, String note, String employeeEmail) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.PENDING_EMPLOYEE) {
            throw new InvalidOrderStateException("Order is not pending employee review");
        }
        employeeRepository.findByEmail(employeeEmail).ifPresent(order::setEmployee);
        order.setEmployeeNote(null);
        order.setStatus(OrderStatus.PENDING_DELIVERY);
        ordersRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasRole('DELIVERER')")
    public void acceptByDeliverer(Long id, String delivererEmail) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.PENDING_DELIVERY) {
            throw new InvalidOrderStateException("Order is not pending delivery");
        }
        Deliverer deliverer = delivererRepository.findByEmail(delivererEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverer", delivererEmail));
        order.setDeliverer(deliverer);
        order.setStatus(OrderStatus.DELIVERING);
        ordersRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasRole('DELIVERER')")
    public void markAsDelivered(Long id, String delivererEmail) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.DELIVERING) {
            throw new InvalidOrderStateException("Order is not being delivered");
        }
        order.setStatus(OrderStatus.DELIVERED);
        ordersRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public void requestRevision(Long id, String note, String employeeEmail) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.PENDING_EMPLOYEE) {
            throw new InvalidOrderStateException("Order is not pending employee review");
        }
        if (note == null || note.isBlank()) {
            throw new InvalidOrderStateException("A note is required when requesting revision");
        }
        employeeRepository.findByEmail(employeeEmail).ifPresent(order::setEmployee);
        order.setEmployeeNote(note);
        ordersRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasRole('CLIENT')")
    public List<OrderRowPriceChangeDTO> submitForReview(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING_EMPLOYEE) {
            throw new InvalidOrderStateException("Order cannot be submitted in its current state");
        }
        if (order.getOrderRowSet().isEmpty()) {
            throw new InvalidOrderStateException("Cannot submit an empty order — add at least one item");
        }
        List<OrderRowPriceChangeDTO> priceChanges = new ArrayList<>();
        for (OrderRow row : order.getOrderRowSet()) {
            BigDecimal currentAmount = row.getAppliance().getPrice().multiply(BigDecimal.valueOf(row.getNumber()));
            if (currentAmount.compareTo(row.getAmount()) != 0) {
                priceChanges.add(new OrderRowPriceChangeDTO(row.getAppliance().getName(), row.getAmount(), currentAmount));
                row.setAmount(currentAmount);
            }
        }
        if (!priceChanges.isEmpty()) {
            // prices changed since the items were added — persist the updated amounts but
            // don't submit yet, so the client can see the new prices and confirm by submitting again
            ordersRepository.save(order);
            return priceChanges;
        }
        order.setStatus(OrderStatus.PENDING_EMPLOYEE);
        order.setEmployeeNote(null); // clear note to signal client addressed the revision
        ordersRepository.save(order);
        return priceChanges;
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE','CLIENT')")
    public void cancelOrder(Long id, String reason, String cancelledByEmail) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() == OrderStatus.DELIVERING || order.getStatus() == OrderStatus.DELIVERED) {
            throw new InvalidOrderStateException("Cannot cancel an order that is being delivered or already delivered");
        }
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Order is already cancelled");
        }
        if (reason == null || reason.isBlank()) {
            throw new InvalidOrderStateException("A reason is required when cancelling an order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(reason);
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelledBy(cancelledByEmail);
        ordersRepository.save(order);
    }

    @Override
    @Loggable
    @Transactional
    public void addRowToOrder(Long orderId, Long applianceId, Long number) {
        if (number == null || number < 1) {
            throw new InvalidOrderStateException("Number must be at least 1");
        }
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING_EMPLOYEE) {
            throw new InvalidOrderStateException("Cannot modify order in current state");
        }
        Appliance appliance = applianceRepository.findById(applianceId)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", applianceId));
        OrderRow row = new OrderRow();
        row.setAppliance(appliance);
        row.setNumber(number);
        row.setAmount(appliance.getPrice().multiply(BigDecimal.valueOf(number)));
        row.setOrder(order);
        orderRowRepository.save(row);
    }

    @Override
    @Loggable
    @Transactional
    public void deleteRowFromOrder(Long rowId) {
        OrderRow row = orderRowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("OrderRow", rowId));
        Orders order = row.getOrder();
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING_EMPLOYEE) {
            throw new InvalidOrderStateException("Cannot modify order in current state");
        }
        orderRowRepository.deleteById(rowId);
    }

    @Override
    public Set<OrderRow> getOrderRows(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return new HashSet<>(order.getOrderRowSet());
    }

    private List<OrderResponseDTO> toDtoList(List<Orders> orders) {
        Map<Long, BigDecimal> totals = totalAmountsByOrderId(orders);
        return orders.stream()
                .map(order -> toDto(order, totals.getOrDefault(order.getId(), BigDecimal.ZERO)))
                .toList();
    }

    private Page<OrderResponseDTO> toDtoPage(Page<Orders> page) {
        Map<Long, BigDecimal> totals = totalAmountsByOrderId(page.getContent());
        return page.map(order -> toDto(order, totals.getOrDefault(order.getId(), BigDecimal.ZERO)));
    }

    private Map<Long, BigDecimal> totalAmountsByOrderId(List<Orders> orders) {
        if (orders.isEmpty()) {
            return Map.of();
        }
        List<Long> orderIds = orders.stream().map(Orders::getId).toList();
        return orderRowRepository.sumAmountsByOrderIds(orderIds).stream()
                .collect(Collectors.toMap(
                        OrderRowRepository.OrderAmountProjection::getOrderId,
                        OrderRowRepository.OrderAmountProjection::getTotalAmount));
    }

    private OrderResponseDTO toDto(Orders order, BigDecimal totalAmount) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setClientName(order.getClient() != null ? order.getClient().getName() : "—");
        dto.setEmployeeName(order.getEmployee() != null ? order.getEmployee().getName() : "—");
        dto.setDelivererName(order.getDeliverer() != null ? order.getDeliverer().getName() : "—");
        dto.setStatus(order.getStatus());
        dto.setEmployeeNote(order.getEmployeeNote());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setCancelReason(order.getCancelReason());
        dto.setCancelledAt(order.getCancelledAt());
        dto.setCancelledBy(order.getCancelledBy());
        dto.setTotalAmount(totalAmount);
        return dto;
    }

    private Orders toEntity(OrderRequestDTO dto) {
        Orders order = new Orders();
        if (dto.getEmployeeId() != null) {
            order.setEmployee(employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", dto.getEmployeeId())));
        }
        order.setClient(clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", dto.getClientId())));
        order.setStatus(OrderStatus.PENDING_EMPLOYEE);
        order.setEmployeeNote(dto.getEmployeeNote());
        return order;
    }
}
