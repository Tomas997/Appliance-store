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
import org.modelmapper.ModelMapper;
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
    private ApplianceRepository applianceRepository;

    @Mock
    private ApplianceInOrderRepository applianceInOrderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Orders buildOrder(Long id, boolean approved) {
        Employee employee = new Employee();
        employee.setName("Олег");
        Client client = new Client();
        client.setName("Марія");

        Orders order = new Orders();
        order.setEmployee(employee);
        order.setClient(client);
        order.setApproved(approved);
        order.setOrderRowSet(new HashSet<>());
        return order;
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO для всіх замовлень")
    void findAll_shouldReturnMappedDtoList() {
        Orders order = buildOrder(1L, false);
        OrderResponseDTO dto = new OrderResponseDTO();

        when(ordersRepository.findAll()).thenReturn(List.of(order));
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(dto);

        List<OrderResponseDTO> result = orderService.findAll();

        assertThat(result).containsExactly(dto);
        assertThat(dto.getEmployeeName()).isEqualTo("Олег");
        assertThat(dto.getClientName()).isEqualTo("Марія");
        assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        when(ordersRepository.findAll()).thenReturn(List.of());

        List<OrderResponseDTO> result = orderService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder(1L, false);
        OrderResponseDTO dto = new OrderResponseDTO();
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(order, OrderResponseDTO.class)).thenReturn(dto);

        Page<OrderResponseDTO> result = orderService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("saveOrder: повинен знайти співробітника і клієнта, а потім зберегти замовлення")
    void saveOrder_shouldResolveEmployeeAndClientAndSave() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(1L);
        dto.setClientId(2L);

        Employee employee = new Employee();
        Client client = new Client();
        Orders order = new Orders();

        when(modelMapper.map(dto, Orders.class)).thenReturn(order);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(clientRepository.findById(2L)).thenReturn(Optional.of(client));

        orderService.saveOrder(dto);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployee()).isSameAs(employee);
        assertThat(captor.getValue().getClient()).isSameAs(client);
    }

    @Test
    @DisplayName("saveOrder: якщо співробітника не знайдено — кинути ResourceNotFoundException")
    void saveOrder_whenEmployeeNotFound_shouldThrow() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(99L);
        dto.setClientId(1L);

        Orders order = new Orders();
        when(modelMapper.map(dto, Orders.class)).thenReturn(order);
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.saveOrder(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("saveOrder: якщо клієнта не знайдено — кинути ResourceNotFoundException")
    void saveOrder_whenClientNotFound_shouldThrow() {
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(1L);
        dto.setClientId(99L);

        Orders order = new Orders();
        Employee employee = new Employee();
        when(modelMapper.map(dto, Orders.class)).thenReturn(order);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
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

        OrderRequestDTO dto = new OrderRequestDTO();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(modelMapper.map(order, OrderRequestDTO.class)).thenReturn(dto);

        OrderRequestDTO result = orderService.findById(1L);

        assertThat(result).isSameAs(dto);
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
    @DisplayName("updateOrder: повинен оновити співробітника, клієнта і статус, потім зберегти")
    void updateOrder_shouldUpdateFieldsAndSave() {
        Orders existing = new Orders();
        Employee employee = new Employee();
        Client client = new Client();

        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(5L);
        dto.setClientId(6L);
        dto.setApproved(true);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findById(5L)).thenReturn(Optional.of(employee));
        when(clientRepository.findById(6L)).thenReturn(Optional.of(client));

        orderService.updateOrder(1L, dto);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());

        Orders saved = captor.getValue();
        assertThat(saved.getEmployee()).isSameAs(employee);
        assertThat(saved.getClient()).isSameAs(client);
        assertThat(saved.getApproved()).isTrue();
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
    @DisplayName("deleteOrderById: повинен делегувати видалення в репозиторій")
    void deleteOrderById_shouldDelegateToRepository() {
        orderService.deleteOrderById(8L);

        verify(ordersRepository).deleteById(8L);
        verifyNoMoreInteractions(ordersRepository);
    }

    @Test
    @DisplayName("approveOrder: повинен встановити approved і зберегти замовлення")
    void approveOrder_shouldSetApprovedAndSave() {
        Orders order = new Orders();
        order.setApproved(false);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.approveOrder(1L, true);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getApproved()).isTrue();
    }

    @Test
    @DisplayName("approveOrder: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void approveOrder_whenNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.approveOrder(99L, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("addRowToOrder: повинен створити рядок замовлення з обчисленою сумою і зберегти")
    void addRowToOrder_shouldCreateRowWithComputedAmountAndSave() {
        Orders order = new Orders();
        order.setApproved(false);
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
    @DisplayName("addRowToOrder: якщо замовлення підтверджено — кинути InvalidOrderStateException")
    void addRowToOrder_whenOrderApproved_shouldThrow() {
        Orders order = new Orders();
        order.setApproved(true);

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
        Orders order = new Orders();
        order.setApproved(false);

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

        Orders order = new Orders();
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
