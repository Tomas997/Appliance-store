package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.OrderStatus;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private DeliveryController deliveryController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deliveryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /deliveries: повинен повернути сторінку зі списком замовлень для доставки та атрибутами пагінації")
    void getDeliveries_shouldReturnDeliveriesViewWithPaginationAttributes() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(1L, "Employee", "Client", "Deliverer",
                OrderStatus.PENDING_DELIVERY, "note", BigDecimal.TEN);
        Page<OrderResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(orderService.findPendingForDelivery(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/deliveries"))
                .andExpect(status().isOk())
                .andExpect(view().name("delivery/deliveries"))
                .andExpect(model().attributeExists("orders", "currentPage", "totalPages", "pageSize"))
                .andExpect(model().attribute("orders", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1));
    }

    @Test
    @DisplayName("GET /deliveries?sort=status,desc: сортування з query-параметра має передаватись у сервіс")
    void getDeliveries_withCustomSort_shouldBindFromRequest() throws Exception {
        when(orderService.findPendingForDelivery(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/deliveries").param("sort", "status,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(orderService).findPendingForDelivery(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("status").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("PATCH /deliveries/{id}/confirm: повинен підтвердити доставку з іменем користувача та перенаправити на список")
    void confirmDelivery_shouldConfirmWithUsernameAndRedirect() throws Exception {
        UsernamePasswordAuthenticationToken principal =
                new UsernamePasswordAuthenticationToken("deliverer@store.com", null);

        mockMvc.perform(patch("/deliveries/{id}/confirm", 1L).principal(principal))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/deliveries"));

        verify(orderService).acceptByDeliverer(1L, "deliverer@store.com");
    }
}
