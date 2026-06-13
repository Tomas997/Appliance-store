package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.OrderRow;
import com.epam.rd.autocode.assessment.appliances.model.Orders;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceInOrderRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import com.epam.rd.autocode.assessment.appliances.repository.OrdersRepository;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ApplianceRepository applianceRepository;
    private final ApplianceInOrderRepository applianceInOrderRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<OrderResponseDTO> findAll() {
        return ordersRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<OrderResponseDTO> findAll(Pageable pageable) {
        return ordersRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public void saveOrder(OrderRequestDTO dto) {
        ordersRepository.save(toEntity(dto));
    }

    @Override
    public OrderRequestDTO findById(Long id) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        OrderRequestDTO dto = modelMapper.map(order, OrderRequestDTO.class);
        dto.setEmployeeId(order.getEmployee().getId());
        dto.setClientId(order.getClient().getId());
        return dto;
    }

    @Override
    public void updateOrder(Long id, OrderRequestDTO dto) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        order.setEmployee(employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getEmployeeId())));
        order.setClient(clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found: " + dto.getClientId())));
        order.setApproved(dto.getApproved());
        ordersRepository.save(order);
    }

    @Override
    public void deleteOrderById(Long id) {
        ordersRepository.deleteById(id);
    }

    @Override
    public void approveOrder(Long id, boolean approved) {
        Orders order = ordersRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        order.setApproved(approved);
        ordersRepository.save(order);
    }

    @Override
    public void addRowToOrder(Long orderId, Long applianceId, Long number, BigDecimal price) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        Appliance appliance = applianceRepository.findById(applianceId)
                .orElseThrow(() -> new RuntimeException("Appliance not found: " + applianceId));
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
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        return order.getOrderRowSet();
    }

    private OrderResponseDTO toDto(Orders order) {
        OrderResponseDTO dto = modelMapper.map(order, OrderResponseDTO.class);
        dto.setEmployeeName(order.getEmployee().getName());
        dto.setClientName(order.getClient().getName());
        BigDecimal total = order.getOrderRowSet().stream()
                .map(OrderRow::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalAmount(total);
        return dto;
    }

    private Orders toEntity(OrderRequestDTO dto) {
        Orders order = modelMapper.map(dto, Orders.class);
        order.setEmployee(employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found: " + dto.getEmployeeId())));
        order.setClient(clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found: " + dto.getClientId())));
        return order;
    }
}
