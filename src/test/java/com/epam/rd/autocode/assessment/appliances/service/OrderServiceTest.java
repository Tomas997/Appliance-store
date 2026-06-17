package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderRowPriceChangeDTO;
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

        when(ordersRepository.findByStatusNotIn(List.of(OrderStatus.DRAFT, OrderStatus.CANCELLED), pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getClientName()).isEqualTo("Марія");
    }

    @Test
    @DisplayName("findCancelled: повинен повернути сторінку скасованих замовлень")
    void findCancelled_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        order.setStatus(OrderStatus.CANCELLED);
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByStatus(OrderStatus.CANCELLED, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findCancelled(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("findByClientEmail: повинен передати email і Pageable в репозиторій і повернути сторінку DTO")
    void findByClientEmail_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByClient_EmailAndStatusNot("maria@store.com", OrderStatus.CANCELLED, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findByClientEmail("maria@store.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getClientName()).isEqualTo("Марія");
    }

    @Test
    @DisplayName("findCancelledByClientEmail: повинен повернути сторінку скасованих замовлень клієнта")
    void findCancelledByClientEmail_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        order.setStatus(OrderStatus.CANCELLED);
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByClient_EmailAndStatus("maria@store.com", OrderStatus.CANCELLED, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findCancelledByClientEmail("maria@store.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("findPendingForDelivery: повинен повернути сторінку замовлень зі статусом PENDING_DELIVERY")
    void findPendingForDelivery_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_DELIVERY);
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByStatus(OrderStatus.PENDING_DELIVERY, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findPendingForDelivery(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.PENDING_DELIVERY);
    }

    @Test
    @DisplayName("createClientOrder: повинен створити чернетку замовлення для клієнта і повернути id")
    void createClientOrder_shouldCreateDraftAndReturnId() {
        Client client = new Client();
        when(clientRepository.findByEmail("maria@store.com")).thenReturn(Optional.of(client));
        when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> {
            Orders order = invocation.getArgument(0);
            order.setId(42L);
            return order;
        });

        Long id = orderService.createClientOrder("maria@store.com");

        assertThat(id).isEqualTo(42L);
        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getClient()).isSameAs(client);
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    @DisplayName("createClientOrder: якщо клієнта не знайдено — кинути ResourceNotFoundException")
    void createClientOrder_whenClientNotFound_shouldThrow() {
        when(clientRepository.findByEmail("unknown@store.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createClientOrder("unknown@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown@store.com");
    }

    @Test
    @DisplayName("findResponseById: повинен повернути OrderResponseDTO якщо замовлення знайдено")
    void findResponseById_whenFound_shouldReturnDto() {
        Orders order = buildOrder();
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDTO result = orderService.findResponseById(1L);

        assertThat(result.getClientName()).isEqualTo("Марія");
        assertThat(result.getEmployeeName()).isEqualTo("Олег");
    }

    @Test
    @DisplayName("findResponseById: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void findResponseById_whenNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findResponseById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("acceptByDeliverer: повинен встановити доставника і статус DELIVERING")
    void acceptByDeliverer_shouldSetDelivererAndStatus() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_DELIVERY);
        Deliverer deliverer = new Deliverer();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(delivererRepository.findByEmail("driver@store.com")).thenReturn(Optional.of(deliverer));

        orderService.acceptByDeliverer(1L, "driver@store.com");

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getDeliverer()).isSameAs(deliverer);
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    @DisplayName("acceptByDeliverer: якщо статус не PENDING_DELIVERY — кинути InvalidOrderStateException")
    void acceptByDeliverer_whenWrongStatus_shouldThrow() {
        Orders order = buildOrder();
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.acceptByDeliverer(1L, "driver@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("acceptByDeliverer: якщо доставника не знайдено — кинути ResourceNotFoundException")
    void acceptByDeliverer_whenDelivererNotFound_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_DELIVERY);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(delivererRepository.findByEmail("unknown@store.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.acceptByDeliverer(1L, "unknown@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("unknown@store.com");
    }

    @Test
    @DisplayName("markAsDelivered: повинен перевести замовлення в статус DELIVERED")
    void markAsDelivered_shouldSetDeliveredStatus() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERING);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.markAsDelivered(1L, "driver@store.com");

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("markAsDelivered: якщо статус не DELIVERING — кинути InvalidOrderStateException")
    void markAsDelivered_whenWrongStatus_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_DELIVERY);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.markAsDelivered(1L, "driver@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("markAsDelivered: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void markAsDelivered_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.markAsDelivered(99L, "driver@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findDelivering: повинен повернути сторінку замовлень зі статусом DELIVERING")
    void findDelivering_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERING);
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByStatus(OrderStatus.DELIVERING, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findDelivering(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    @DisplayName("findDelivered: повинен повернути сторінку замовлень зі статусом DELIVERED")
    void findDelivered_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERED);
        Page<Orders> page = new PageImpl<>(List.of(order), pageable, 1);

        when(ordersRepository.findByStatus(OrderStatus.DELIVERED, pageable)).thenReturn(page);

        Page<OrderResponseDTO> result = orderService.findDelivered(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("requestRevision: повинен встановити нотатку і зберегти без зміни статусу")
    void requestRevision_shouldSetNoteAndSave() {
        Orders order = buildOrder();
        Employee employee = new Employee();

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));
        when(employeeRepository.findByEmail("ivan@store.com")).thenReturn(Optional.of(employee));

        orderService.requestRevision(1L, "Please clarify quantity", "ivan@store.com");

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployeeNote()).isEqualTo("Please clarify quantity");
        assertThat(captor.getValue().getEmployee()).isSameAs(employee);
    }

    @Test
    @DisplayName("requestRevision: якщо статус не PENDING_EMPLOYEE — кинути InvalidOrderStateException")
    void requestRevision_whenWrongStatus_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.PENDING_CLIENT);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestRevision(1L, "note", "ivan@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("requestRevision: якщо нотатка порожня — кинути InvalidOrderStateException")
    void requestRevision_whenNoteBlank_shouldThrow() {
        Orders order = buildOrder();
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.requestRevision(1L, "   ", "ivan@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("submitForReview: повинен перевести замовлення в PENDING_EMPLOYEE і очистити нотатку")
    void submitForReview_shouldSetStatusAndClearNote() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DRAFT);
        order.setEmployeeNote("old note");
        Appliance appliance = new Appliance();
        appliance.setPrice(new BigDecimal("50.00"));
        OrderRow row = new OrderRow();
        row.setAppliance(appliance);
        row.setNumber(2L);
        row.setAmount(new BigDecimal("100.00"));
        order.getOrderRowSet().add(row);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        List<OrderRowPriceChangeDTO> priceChanges = orderService.submitForReview(1L);

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING_EMPLOYEE);
        assertThat(captor.getValue().getEmployeeNote()).isNull();
        assertThat(priceChanges).isEmpty();
        assertThat(row.getAmount()).isEqualByComparingTo("100.00");
    }

    @Test
    @DisplayName("submitForReview: якщо ціна приладу змінилась — повинен оновити суму рядка і повернути зміну")
    void submitForReview_whenPriceChanged_shouldUpdateAmountAndReturnChange() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DRAFT);
        Appliance appliance = new Appliance();
        appliance.setName("Кондиціонер");
        appliance.setPrice(new BigDecimal("80.00"));
        OrderRow row = new OrderRow();
        row.setAppliance(appliance);
        row.setNumber(2L);
        row.setAmount(new BigDecimal("100.00"));
        order.getOrderRowSet().add(row);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        List<OrderRowPriceChangeDTO> priceChanges = orderService.submitForReview(1L);

        assertThat(priceChanges).hasSize(1);
        OrderRowPriceChangeDTO change = priceChanges.get(0);
        assertThat(change.getApplianceName()).isEqualTo("Кондиціонер");
        assertThat(change.getOldAmount()).isEqualByComparingTo("100.00");
        assertThat(change.getNewAmount()).isEqualByComparingTo("160.00");
        assertThat(row.getAmount()).isEqualByComparingTo("160.00");

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus())
                .as("замовлення не повинно бути відправлене на першому виклику зі зміненою ціною")
                .isEqualTo(OrderStatus.DRAFT);
    }

    @Test
    @DisplayName("submitForReview: повторний виклик після оновлення ціни — повинен відправити замовлення без попереджень")
    void submitForReview_whenCalledAgainAfterPriceUpdated_shouldSubmit() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DRAFT);
        Appliance appliance = new Appliance();
        appliance.setPrice(new BigDecimal("80.00"));
        OrderRow row = new OrderRow();
        row.setAppliance(appliance);
        row.setNumber(2L);
        row.setAmount(new BigDecimal("160.00")); // вже синхронізовано з поточною ціною
        order.getOrderRowSet().add(row);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        List<OrderRowPriceChangeDTO> priceChanges = orderService.submitForReview(1L);

        assertThat(priceChanges).isEmpty();
        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(OrderStatus.PENDING_EMPLOYEE);
    }

    @Test
    @DisplayName("submitForReview: якщо статус не DRAFT/PENDING_EMPLOYEE — кинути InvalidOrderStateException")
    void submitForReview_whenWrongStatus_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERING);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.submitForReview(1L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("submitForReview: якщо замовлення без рядків — кинути InvalidOrderStateException")
    void submitForReview_whenEmptyRows_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DRAFT);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.submitForReview(1L))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("cancelOrder: повинен перевести замовлення в статус CANCELLED і записати аудит")
    void cancelOrder_shouldSetCancelledStatus() {
        Orders order = buildOrder();
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(1L, "Client changed mind", "maria@store.com");

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        Orders saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(saved.getCancelReason()).isEqualTo("Client changed mind");
        assertThat(saved.getCancelledBy()).isEqualTo("maria@store.com");
        assertThat(saved.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("cancelOrder: якщо замовлення доставляється — кинути InvalidOrderStateException")
    void cancelOrder_whenDelivering_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERING);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "reason", "maria@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("cancelOrder: якщо замовлення вже доставлене — кинути InvalidOrderStateException")
    void cancelOrder_whenDelivered_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERED);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "reason", "maria@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("cancelOrder: якщо замовлення вже скасоване — кинути InvalidOrderStateException")
    void cancelOrder_whenAlreadyCancelled_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.CANCELLED);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "reason", "maria@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("cancelOrder: якщо причина порожня — кинути InvalidOrderStateException")
    void cancelOrder_whenReasonBlank_shouldThrow() {
        Orders order = buildOrder();
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(1L, "   ", "maria@store.com"))
                .isInstanceOf(InvalidOrderStateException.class);
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
    @DisplayName("updateOrder: якщо статус PENDING_DELIVERY — кинути InvalidOrderStateException")
    void updateOrder_whenPendingDelivery_shouldThrow() {
        Orders existing = buildOrder();
        existing.setStatus(OrderStatus.PENDING_DELIVERY);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.updateOrder(1L, new OrderRequestDTO()))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("updateOrder: якщо статус DELIVERING — кинути InvalidOrderStateException")
    void updateOrder_whenDelivering_shouldThrow() {
        Orders existing = buildOrder();
        existing.setStatus(OrderStatus.DELIVERING);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.updateOrder(1L, new OrderRequestDTO()))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("updateOrder: якщо статус DELIVERED — кинути InvalidOrderStateException")
    void updateOrder_whenDelivered_shouldThrow() {
        Orders existing = buildOrder();
        existing.setStatus(OrderStatus.DELIVERED);
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> orderService.updateOrder(1L, new OrderRequestDTO()))
                .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    @DisplayName("updateOrder: якщо employeeId не вказано — повинен очистити співробітника")
    void updateOrder_whenEmployeeIdNull_shouldClearEmployee() {
        Orders existing = buildOrder();
        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));

        orderService.updateOrder(1L, new OrderRequestDTO());

        ArgumentCaptor<Orders> captor = ArgumentCaptor.forClass(Orders.class);
        verify(ordersRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployee()).isNull();
    }

    @Test
    @DisplayName("updateOrder: якщо нового співробітника не знайдено — кинути ResourceNotFoundException")
    void updateOrder_whenNewEmployeeNotFound_shouldThrow() {
        Orders existing = buildOrder();
        OrderRequestDTO dto = new OrderRequestDTO();
        dto.setEmployeeId(99L);

        when(ordersRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrder(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("deleteOrderById: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void deleteOrderById_whenNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteOrderById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("approveByEmployee: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void approveByEmployee_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.approveByEmployee(99L, null, "ivan@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("acceptByDeliverer: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void acceptByDeliverer_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.acceptByDeliverer(99L, "driver@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("requestRevision: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void requestRevision_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.requestRevision(99L, "note", "ivan@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("submitForReview: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void submitForReview_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.submitForReview(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("cancelOrder: повинен кинути ResourceNotFoundException якщо замовлення не знайдено")
    void cancelOrder_whenOrderNotFound_shouldThrow() {
        when(ordersRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(99L, "reason", "maria@store.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
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
    @DisplayName("deleteOrderById: якщо замовлення доставлене — кинути InvalidOrderStateException")
    void deleteOrderById_whenDelivered_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.DELIVERED);
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
    @DisplayName("deleteRowFromOrder: повинен делегувати видалення рядка в репозиторій якщо замовлення редагується")
    void deleteRowFromOrder_shouldDelegateToRepository() {
        Orders order = buildOrder();
        OrderRow row = new OrderRow();
        row.setOrder(order);
        when(applianceInOrderRepository.findById(5L)).thenReturn(Optional.of(row));

        orderService.deleteRowFromOrder(5L);

        verify(applianceInOrderRepository).deleteById(5L);
    }

    @Test
    @DisplayName("deleteRowFromOrder: якщо замовлення не в DRAFT/PENDING_EMPLOYEE — кинути InvalidOrderStateException")
    void deleteRowFromOrder_whenOrderLocked_shouldThrow() {
        Orders order = buildOrder();
        order.setStatus(OrderStatus.CANCELLED);
        OrderRow row = new OrderRow();
        row.setOrder(order);
        when(applianceInOrderRepository.findById(5L)).thenReturn(Optional.of(row));

        assertThatThrownBy(() -> orderService.deleteRowFromOrder(5L))
                .isInstanceOf(InvalidOrderStateException.class);

        verify(applianceInOrderRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("deleteRowFromOrder: якщо рядок не знайдено — кинути ResourceNotFoundException")
    void deleteRowFromOrder_whenRowNotFound_shouldThrow() {
        when(applianceInOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.deleteRowFromOrder(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
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
