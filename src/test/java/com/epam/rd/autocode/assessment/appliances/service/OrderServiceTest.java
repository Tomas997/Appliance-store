package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.exception.InvalidOrderStateException;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.*;
import com.epam.rd.autocode.assessment.appliances.repository.*;
import com.epam.rd.autocode.assessment.appliances.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrdersRepository ordersRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private DelivererRepository delivererRepository;

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private ApplianceInOrderRepository applianceInOrderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Orders buildOrder() {
        Employee employee = new Employee();
        employee.setName("Олег");
        Client client = new Client();
        client.setName("Марія");

        Orders order = new Orders();
        order.setEmployee(employee);
        order.setClient(client);
        order.setStatus(OrderStatus.PENDING_EMPLOYEE);
        order.setOrderRowSet(new HashSet<>());
        return order;
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO з іменами співробітника і клієнта")
    void findAll_shouldReturnMappedDtoList() {
        Orders order = buildOrder();

        when(ordersRepository.findAll()).thenReturn(List.of(order));

        List<OrderResponseDTO> result = orderService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmployeeName()).isEqualTo("Олег");
        assertThat(result.get(0).getClientName()).isEqualTo("Марія");
        assertThat(result.get(0).getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(0).getStatus()).isEqualTo(OrderStatus.PENDING_EMPLOYEE);
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        when(ordersRepository.findAll()).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByStatusNot(OrderStatus.DRAFT, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getClientName()).isEqualTo("Марія");
    }

    @Test
    @DisplayName("saveOrder: повинен знайти співробітника і клієнта, а потім зберегти замовлення")
    void saveOrder_shouldResolveEmployeeAndClientAndSave() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(1L);
        dto.setClientId(2L);

        Employee employee = new Employee();
        Client client = new Client();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client));

        orderService.saveOrder(dto);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployee()).isSameAs(employee);
        assertThat(captor.getValue().getClient()).isSameAs(client);
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING_EMPLOYEE);
    }

    @Test
    @DisplayName("saveOrder: якщо співробітника не знайдено — кинути ResourceNotFoundException")
    void saveOrder_whenEmployeeNotFound_shouldThrow() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(99L);
        dto.setClientId(1L);

        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.saveOrder(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("saveOrder: якщо клієнта не знайдено — кинути ResourceNotFoundException")
    void saveOrder_whenClientNotFound_shouldThrow() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setClientId(99L);

        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.saveOrder(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findById: повинен повернути OrderRequestDTO з employeeId і clientId якщо замовлення знайдено")
    void findById_whenFound_shouldReturnDTOWithIds() {
        Employee employee = new Employee();
        employee.setId(10L);
        Client client = new Client();
        client.setId(20L);

        Orders order = new Orders();
        order.setEmployee(employee);
        order.setClient(client);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderRequestDTO result = orderService.findById(1L);

        assertThat(result.getEmployeeId()).isEqualTo(10L);
        assertThat(result.getClientId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("findById: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void findById_whenNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateOrder: повинен оновити співробітника і клієнта, потім зберегти")
    void updateOrder_shouldUpdateFieldsAndSave() {
        Orders existing = buildOrder();
        Employee newEmployee = new Employee();
        Client newClient = new Client();

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(5L);
        dto.setClientId(6L);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(newEmployee));

        orderService.updateOrder(1L, dto);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());

        assertThat(captor.getValue().getEmployee()).isSameAs(newEmployee);
    }

    @Test
    @DisplayName("updateOrder: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void updateOrder_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(99L, new OrderRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteOrderById: повинен делегувати видалення якщо замовлення не доставляється")
    void deleteOrderById_shouldDelegateToRepository() {
        Orders order = buildOrder();
        when(ordersRepository.findById(8L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(8L);

        verify(ordersRepository).deleteById(8L);
    }

    @Test
    @DisplayName("deleteOrderById: якщо замовлення доставляється — кинути InvalidOrderStateException")
    void deleteOrderById_whenDelivering_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERING);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.deleteOrderById(1L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("approveByEmployee: повинен встановити нотатку і статус PENDING_CLIENT")
    void approveByEmployee_shouldSetNoteAndStatus() {
        Orders order = buildOrder();
        Employee employee = new Employee();
        employee.setName("Іван");

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("ivan@store.com")).thenReturn(Optional.of(employee));

        orderService.approveByEmployee(1L, "Alternative suggested", "ivan@store.com");

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());

        Orders saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING_DELIVERY);
        assertThat(saved.getEmployeeNote()).isNull();
    }

    @Test
    @DisplayName("approveByEmployee: якщо статус не PENDING_EMPLOYEE — кинути InvalidOrderStateException")
    void approveByEmployee_whenWrongStatus_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_CLIENT);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.approveByEmployee(1L, null, "ivan@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("addRowToOrder: повинен створити рядок замовлення з обчисленою сумою і зберегти")
    void addRowToOrder_shouldCreateRowWithComputedAmountAndSave() {
        Orders order = buildOrder();
        Appliance appliance = new Appliance();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(applianceRepository.findById(2L)).thenReturn(Optional.of(appliance));

        orderService.addRowToOrder(1L, 2L, 3L, new BigDecimal("100.00"));

        ArgumentCaptor<OrderRow> captor = ArgumentCaptor.forClass(OrderRow.class);
        verify(applianceInOrderRepository).save(captor.capture());

        OrderRow saved = captor.getValue();
        assertThat(saved.getAppliance()).isSameAs(appliance);
        assertThat(saved.getNumber()).isEqualTo(3L);
        assertThat(saved.getAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        assertThat(saved.getOrder()).isSameAs(order);
    }

    @Test
    @DisplayName("addRowToOrder: якщо статус не PENDING_EMPLOYEE — кинути InvalidOrderStateException")
    void addRowToOrder_whenNotPendingEmployee_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_CLIENT);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.addRowToOrder(1L, 2L, 1L, BigDecimal.TEN))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("addRowToOrder: якщо замовлення не знайдено — кинути ResourceNotFoundException")
    void addRowToOrder_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addRowToOrder(99L, 1L, 1L, BigDecimal.ONE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("addRowToOrder: якщо прилад не знайдено — кинути ResourceNotFoundException")
    void addRowToOrder_whenApplianceNotFound_shouldThrow() {
        Orders order = buildOrder();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(applianceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.addRowToOrder(1L, 99L, 1L, BigDecimal.ONE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteRowFromOrder: повинен делегувати видалення рядка в репозиторій")
    void deleteRowFromOrder_shouldDelegateToRepository() {
        orderService.deleteRowFromOrder(5L);

        verify(applianceInOrderRepository).deleteById(5L);
        verifyNoMoreInteractions(applianceInOrderRepository);
    }

    @Test
    @DisplayName("getOrderRows: повинен повернути набір рядків замовлення")
    void getOrderRows_shouldReturnOrderRowSet() {
        OrderRow row1 = new OrderRow();
        OrderRow row2 = new OrderRow();
        Set<OrderRow> rows = new HashSet<>(Set.of(row1, row2));

        Orders order = buildOrder();
        order.setOrderRowSet(rows);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        Set<OrderRow> result = orderService.getOrderRows(1L);

        assertThat(result).containsExactlyInAnyOrder(row1, row2);
    }

    @Test
    @DisplayName("getOrderRows: якщо замовлення не знайдено — кинути ResourceNotFoundException")
    void getOrderRows_whenNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderRows(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
