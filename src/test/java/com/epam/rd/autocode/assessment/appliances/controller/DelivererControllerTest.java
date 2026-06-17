package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.DelivererResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.service.DelivererService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DelivererControllerTest {

    @Mock
    private DelivererService delivererService;

    @InjectMocks
    private DelivererController delivererController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(delivererController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /deliverers: повинен повернути сторінку зі списком доставників та атрибутами пагінації")
    void getDeliverers_shouldReturnDeliverersViewWithPaginationAttributes() throws Exception {
        DelivererResponseDTO dto = new DelivererResponseDTO(1L, "Andrii", "andrii@kpi.ua");
        Page<DelivererResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(delivererService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/deliverers"))
                .andExpect(status().isOk())
                .andExpect(view().name("deliverer/deliverers"))
                .andExpect(model().attributeExists("deliverers", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("deliverers", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /deliverers?sort=name,desc: сортування з query-параметра має передаватись у сервіс")
    void getDeliverers_withCustomSort_shouldBindFromRequest() throws Exception {
        when(delivererService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/deliverers").param("sort", "name,desc"))
                .andExpect(model().attribute("sortField", "name"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(delivererService).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /deliverers/add: повинен повернути форму для додавання нового доставника")
    void getAddDelivererForm_shouldReturnNewDelivererView() throws Exception {
        mockMvc.perform(get("/deliverers/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("deliverer/newDeliverer"))
                .andExpect(model().attributeExists("deliverer"));
    }

    @Test
    @DisplayName("POST /deliverers/add: валідні дані — повинен зберегти доставника та перенаправити на список")
    void postAddDeliverer_validData_shouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/deliverers/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Andrii")
                        .param("email", "andrii@kpi.ua")
                        .param("password", "Admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/deliverers"));

        verify(delivererService).saveDeliverer(any());
    }

    @Test
    @DisplayName("POST /deliverers/add: невалідні дані — повинен повернутись на форму без виклику сервісу")
    void postAddDeliverer_invalidData_shouldReturnFormWithoutSaving() throws Exception {
        mockMvc.perform(post("/deliverers/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("deliverer/newDeliverer"));

        verifyNoInteractions(delivererService);
    }

    @Test
    @DisplayName("GET /deliverers/{id}/edit: повинен повернути форму редагування з даними доставника")
    void getEditDelivererForm_shouldReturnEditViewWithDelivererData() throws Exception {
        DelivererUpdateDTO dto = new DelivererUpdateDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        when(delivererService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/deliverers/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("deliverer/editDeliverer"))
                .andExpect(model().attribute("deliverer", dto))
                .andExpect(model().attribute("delivererId", 1L));
    }

    @Test
    @DisplayName("PUT /deliverers/{id}/edit: валідні дані — повинен оновити доставника та перенаправити на список")
    void putEditDeliverer_validData_shouldUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/deliverers/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Andrii")
                        .param("email", "andrii@kpi.ua"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/deliverers"));

        verify(delivererService).updateDeliverer(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /deliverers/{id}/edit: невалідні дані — повинен повернутись на форму з delivererId без виклику сервісу")
    void putEditDeliverer_invalidData_shouldReturnFormWithDelivererId() throws Exception {
        mockMvc.perform(put("/deliverers/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("deliverer/editDeliverer"))
                .andExpect(model().attribute("delivererId", 1L));

        verifyNoInteractions(delivererService);
    }

    @Test
    @DisplayName("DELETE /deliverers/{id}: повинен видалити доставника та перенаправити на список")
    void deleteDeliverer_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/deliverers/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/deliverers"));

        verify(delivererService).deleteDelivererById(1L);
    }
}
