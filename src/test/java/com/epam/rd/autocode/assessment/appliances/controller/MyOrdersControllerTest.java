package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MyOrdersControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private ApplianceService applianceService;

    @InjectMocks
    private MyOrdersController myOrdersController;

    private MockMvc mockMvc;

    private final UsernamePasswordAuthenticationToken principal =
            new UsernamePasswordAuthenticationToken("client@store.com", null);

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(myOrdersController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /my-orders: повинен повернути сторінку зі списком замовлень клієнта та атрибутами пагінації")
    void getMyOrders_shouldReturnMyOrdersViewWithPaginationAttributes() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(1L, "Employee", "Client", "Deliverer",
                OrderStatus.DRAFT, "note", BigDecimal.TEN);
        Page<OrderResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(orderService.findByClientEmail(eq("client@store.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/my-orders").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("my-orders/myOrders"))
                .andExpect(model().attributeExists("orders", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("orders", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /my-orders?sort=status,desc: сортування з query-параметра має передаватись у сервіс")
    void getMyOrders_withCustomSort_shouldBindFromRequest() throws Exception {
        when(orderService.findByClientEmail(eq("client@store.com"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/my-orders").principal(principal).param("sort", "status,desc"))
                .andExpect(model().attribute("sortField", "status"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).findByClientEmail(eq("client@store.com"), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("status").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("POST /my-orders/create: повинен створити замовлення клієнта та перенаправити на редагування")
    void createOrder_shouldCreateOrderAndRedirectToEdit() throws Exception {
        when(orderService.createClientOrder("client@store.com")).thenReturn(7L);

        mockMvc.perform(post("/my-orders/create").principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-orders/7/edit"));

        verify(orderService).createClientOrder("client@store.com");
    }

    @Test
    @DisplayName("GET /my-orders/{id}/edit: повинен повернути форму редагування замовлення клієнта")
    void getEditMyOrderForm_shouldReturnEditViewWithOrderData() throws Exception {
        OrderResponseDTO responseDTO = new OrderResponseDTO(1L, "Employee", "Client", "Deliverer",
                OrderStatus.DRAFT, "note", BigDecimal.TEN);
        when(orderService.findResponseById(1L)).thenReturn(responseDTO);
        when(orderService.getOrderRows(1L)).thenReturn(Set.of());

        mockMvc.perform(get("/my-orders/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("my-orders/editMyOrder"))
                .andExpect(model().attribute("order", responseDTO))
                .andExpect(model().attribute("orderId", 1L))
                .andExpect(model().attributeExists("rows"));
    }

    @Test
    @DisplayName("GET /my-orders/{id}/choice-appliance: повинен повернути сторінку вибору товару")
    void choiceAppliance_shouldReturnChoiceApplianceView() throws Exception {
        ApplianceResponseDTO appliance = new ApplianceResponseDTO();
        appliance.setId(1L);
        when(applianceService.findAll()).thenReturn(List.of(appliance));

        mockMvc.perform(get("/my-orders/{id}/choice-appliance", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("my-orders/choiceAppliance"))
                .andExpect(model().attribute("ordersId", 1L))
                .andExpect(model().attribute("appliances", List.of(appliance)));
    }

    @Test
    @DisplayName("POST /my-orders/add-into-order: повинен додати рядок до замовлення та перенаправити на редагування")
    void addIntoOrder_shouldAddRowAndRedirectToEdit() throws Exception {
        mockMvc.perform(post("/my-orders/add-into-order")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("ordersId", "1")
                        .param("applianceId", "2")
                        .param("numbers", "3")
                        .param("price", "49.50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-orders/1/edit"));

        verify(orderService).addRowToOrder(1L, 2L, 3L, new BigDecimal("49.50"));
    }

    @Test
    @DisplayName("DELETE /my-orders/{rowId}/row: повинен видалити рядок та перенаправити на редагування замовлення")
    void deleteRow_shouldDeleteRowAndRedirectToEdit() throws Exception {
        mockMvc.perform(delete("/my-orders/{rowId}/row", 5L)
                        .param("orderId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-orders/1/edit"));

        verify(orderService).deleteRowFromOrder(5L);
    }

    @Test
    @DisplayName("PATCH /my-orders/{id}/submit: повинен надіслати замовлення на розгляд та перенаправити на список")
    void submitForReview_shouldSubmitAndRedirect() throws Exception {
        mockMvc.perform(patch("/my-orders/{id}/submit", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-orders"));

        verify(orderService).submitForReview(1L);
    }

    @Test
    @DisplayName("PATCH /my-orders/{id}/cancel: повинен скасувати замовлення та перенаправити на список")
    void cancelOrder_shouldCancelAndRedirect() throws Exception {
        mockMvc.perform(patch("/my-orders/{id}/cancel", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-orders"));

        verify(orderService).cancelOrder(1L);
    }

    @Test
    @DisplayName("DELETE /my-orders/{id}: повинен видалити замовлення та перенаправити на список")
    void deleteOrder_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/my-orders/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-orders"));

        verify(orderService).deleteOrderById(1L);
    }
}
