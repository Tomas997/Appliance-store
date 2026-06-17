package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrdersControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ClientService clientService;

    @Mock
    private ApplianceService applianceService;

    @InjectMocks
    private OrdersController ordersController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ordersController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /orders: повинен повернути сторінку зі списком замовлень та атрибутами пагінації")
    void getOrders_shouldReturnOrdersViewWithPaginationAttributes() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(1L, "Employee", "Client", "Deliverer",
                OrderStatus.DRAFT, "note", BigDecimal.TEN);
        Page<OrderResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(orderService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/orders"))
                .andExpect(model().attributeExists("orders", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("orders", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /orders?sort=status,desc: сортування з query-параметра має передаватись у сервіс")
    void getOrders_withCustomSort_shouldBindFromRequest() throws Exception {
        when(orderService.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/orders").param("sort", "status,desc"))
                .andExpect(model().attribute("sortField", "status"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("status").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /orders/add: повинен повернути форму для додавання нового замовлення")
    void getAddOrderForm_shouldReturnNewOrderView() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of());
        when(clientService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/orders/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("order/newOrder"))
                .andExpect(model().attributeExists("order", "employees", "clients"));
    }

    @Test
    @DisplayName("POST /orders/add: валідні дані — повинен зберегти замовлення та перенаправити на список")
    void postAddOrder_validData_shouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/orders/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("clientId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).saveOrder(any());
    }

    @Test
    @DisplayName("POST /orders/add: невалідні дані — повинен повернутись на форму без виклику сервісу")
    void postAddOrder_invalidData_shouldReturnFormWithoutSaving() throws Exception {
        when(employeeService.findAll()).thenReturn(List.of());
        when(clientService.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/orders/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("order/newOrder"));

        verify(orderService, never()).saveOrder(any());
    }

    @Test
    @DisplayName("GET /orders/{id}/edit: повинен повернути форму редагування з даними замовлення")
    void getEditOrderForm_shouldReturnEditViewWithOrderData() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO();
        requestDTO.setClientId(1L);
        OrderResponseDTO responseDTO = new OrderResponseDTO(1L, "Employee", "Client", "Deliverer",
                OrderStatus.DRAFT, "note", BigDecimal.TEN);
        when(orderService.findById(1L)).thenReturn(requestDTO);
        when(orderService.findResponseById(1L)).thenReturn(responseDTO);
        when(orderService.getOrderRows(1L)).thenReturn(Set.of());
        when(employeeService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/orders/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("order/editOrder"))
                .andExpect(model().attribute("order", requestDTO))
                .andExpect(model().attribute("orderInfo", responseDTO))
                .andExpect(model().attribute("orderId", 1L))
                .andExpect(model().attributeExists("rows", "employees"));
    }

    @Test
    @DisplayName("PUT /orders/{id}/edit: валідні дані — повинен оновити замовлення та перенаправити на список")
    void putEditOrder_validData_shouldUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/orders/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("clientId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).updateOrder(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /orders/{id}/edit: відсутній clientId — контролер не валідує DTO (немає @Valid), тож оновлення все одно відбувається")
    void putEditOrder_missingClientId_shouldStillUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/orders/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).updateOrder(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /orders/{id}: повинен видалити замовлення та перенаправити на список")
    void deleteOrder_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/orders/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).deleteOrderById(1L);
    }

    @Test
    @DisplayName("PATCH /orders/{id}/employee-approve: повинен викликати сервіс з іменем користувача та перенаправити на список")
    void approveByEmployee_shouldCallServiceWithUsernameAndRedirect() throws Exception {
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("employee@store.com", null);

        mockMvc.perform(patch("/orders/{id}/employee-approve", 1L)
                        .principal(principal)
                        .param("note", "ok"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).approveByEmployee(1L, "ok", "employee@store.com");
    }

    @Test
    @DisplayName("PATCH /orders/{id}/employee-approve: без параметра note — повинен викликати сервіс з null")
    void approveByEmployee_withoutNote_shouldCallServiceWithNullNote() throws Exception {
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("employee@store.com", null);

        mockMvc.perform(patch("/orders/{id}/employee-approve", 1L)
                        .principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).approveByEmployee(eq(1L), isNull(), eq("employee@store.com"));
    }

    @Test
    @DisplayName("PATCH /orders/{id}/employee-revision: повинен викликати сервіс з іменем користувача та перенаправити на список")
    void requestRevision_shouldCallServiceWithUsernameAndRedirect() throws Exception {
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("employee@store.com", null);

        mockMvc.perform(patch("/orders/{id}/employee-revision", 1L)
                        .principal(principal)
                        .param("note", "please fix"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders"));

        verify(orderService).requestRevision(1L, "please fix", "employee@store.com");
    }

    @Test
    @DisplayName("GET /orders/{id}/choice-appliance: повинен повернути сторінку вибору товару")
    void choiceAppliance_shouldReturnChoiceApplianceView() throws Exception {
        ApplianceResponseDTO appliance = new ApplianceResponseDTO();
        appliance.setId(1L);
        when(applianceService.findAll()).thenReturn(List.of(appliance));

        mockMvc.perform(get("/orders/{id}/choice-appliance", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("order/choiceAppliance"))
                .andExpect(model().attribute("ordersId", 1L))
                .andExpect(model().attribute("appliances", List.of(appliance)));
    }

    @Test
    @DisplayName("POST /orders/add-into-order: повинен додати рядок до замовлення та перенаправити на редагування")
    void addIntoOrder_shouldAddRowAndRedirectToEdit() throws Exception {
        mockMvc.perform(post("/orders/add-into-order")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("ordersId", "1")
                        .param("applianceId", "2")
                        .param("numbers", "3")
                        .param("price", "99.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1/edit"));

        verify(orderService).addRowToOrder(1L, 2L, 3L, new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("DELETE /orders/{rowId}/row: повинен видалити рядок та перенаправити на редагування замовлення")
    void deleteRow_shouldDeleteRowAndRedirectToEdit() throws Exception {
        mockMvc.perform(delete("/orders/{rowId}/row", 5L)
                        .param("orderId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1/edit"));

        verify(orderService).deleteRowFromOrder(5L);
    }
}
