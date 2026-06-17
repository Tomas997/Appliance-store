package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrdersRepository ordersRepository;
    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final DelivererRepository delivererRepository;
    private final ApplianceRepository applianceRepository;
    private final ApplianceInOrderRepository applianceInOrderRepository;

    @Override
    public List<OrderResponseDTO> findAll() {
        return ordersRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return ordersRepository.findByStatusNot(OrderStatus.DRAFT, pageable).map(this::toDto);
    }

    @Override
    public Page<OrderResponseDTO> findByClientEmail(String email, Pageable pageable) {
        return ordersRepository.findByClient_Email(email, pageable).map(this::toDto);
    }

    @Override
    public Page<OrderResponseDTO> findPendingForDelivery(Pageable pageable) {
        return ordersRepository.findByStatus(OrderStatus.PENDING_DELIVERY, pageable).map(this::toDto);
    }

    @Override
    public void saveOrder(OrderRequestDTO dto) {
        ordersRepository.save(toEntity(dto));
    }

    @Override
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
        return toDto(order);
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
    public void updateOrder(Long id, OrderRequestDTO dto) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() == OrderStatus.PENDING_DELIVERY || order.getStatus() == OrderStatus.DELIVERING) {
            throw new InvalidOrderStateException("Cannot edit an order that is pending delivery or being delivered");
        }
        if (dto.getEmployeeId() != null) {
            order.setEmployee(employeeRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Employee", dto.getEmployeeId())));
        } else {
            order.setEmployee(null);
        }
        ordersRepository.save(order);
    }

    @Override
    public void deleteOrderById(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() == OrderStatus.DELIVERING) {
            throw new InvalidOrderStateException("Cannot delete an order that is being delivered");
        }
        ordersRepository.deleteById(id);
    }

    @Override
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
    @PreAuthorize("hasRole('CLIENT')")
    public void submitForReview(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() != OrderStatus.DRAFT && order.getStatus() != OrderStatus.PENDING_EMPLOYEE) {
            throw new InvalidOrderStateException("Order cannot be submitted in its current state");
        }
        if (order.getOrderRowSet().isEmpty()) {
            throw new InvalidOrderStateException("Cannot submit an empty order — add at least one item");
        }
        order.setStatus(OrderStatus.PENDING_EMPLOYEE);
        order.setEmployeeNote(null); // clear note to signal client addressed the revision
        ordersRepository.save(order);
    }

    @Override
    public void cancelOrder(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
        if (order.getStatus() == OrderStatus.DELIVERING) {
            throw new InvalidOrderStateException("Cannot cancel an order that is already being delivered");
        }
        order.setStatus(OrderStatus.CANCELLED);
        ordersRepository.save(order);
    }

    @Override
    public void addRowToOrder(Long orderId, Long applianceId, Long number, BigDecimal price) {
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
        row.setAmount(price.multiply(BigDecimal.valueOf(number)));
        row.setOrder(order);
        applianceInOrderRepository.save(row);
    }

    @Override
    public void deleteRowFromOrder(Long rowId) {
        applianceInOrderRepository.deleteById(rowId);
    }

    @Override
    public Set<OrderRow> getOrderRows(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        return order.getOrderRowSet();
    }

    private OrderResponseDTO toDto(Orders order) {
        OrderResponseDTO dto = new OrderResponseDTO();
        dto.setId(order.getId());
        dto.setClientName(order.getClient() != null ? order.getClient().getName() : "—");
        dto.setEmployeeName(order.getEmployee() != null ? order.getEmployee().getName() : "—");
        dto.setDelivererName(order.getDeliverer() != null ? order.getDeliverer().getName() : "—");
        dto.setStatus(order.getStatus());
        dto.setEmployeeNote(order.getEmployeeNote());
        BigDecimal total = order.getOrderRowSet().stream()
                .map(OrderRow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(total);
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
