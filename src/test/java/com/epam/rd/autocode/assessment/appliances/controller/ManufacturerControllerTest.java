package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
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
class ManufacturerControllerTest {

    @Mock
    private ManufacturerService manufacturerService;

    @InjectMocks
    private ManufacturerController manufacturerController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(manufacturerController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /manufacturers: повинен повернути сторінку зі списком виробників та атрибутами пагінації")
    void getManufacturers_shouldReturnManufacturersViewWithPaginationAttributes() throws Exception {
        ManufacturerResponseDTO dto = new ManufacturerResponseDTO(1L, "Samsung");
        Page<ManufacturerResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(manufacturerService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/manufacturers"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacture/manufacturers"))
                .andExpect(model().attributeExists("manufacturers", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("manufacturers", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /manufacturers?sort=name,desc: сортування з query-параметра має передаватись у сервіс")
    void getManufacturers_withCustomSort_shouldBindFromRequest() throws Exception {
        when(manufacturerService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/manufacturers").param("sort", "name,desc"))
                .andExpect(model().attribute("sortField", "name"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(manufacturerService).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /manufacturers/add: повинен повернути форму для додавання нового виробника")
    void getAddManufacturerForm_shouldReturnNewManufacturerView() throws Exception {
        mockMvc.perform(get("/manufacturers/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacture/newManufacturer"))
                .andExpect(model().attributeExists("manufacturer"));
    }

    @Test
    @DisplayName("POST /manufacturers/add: валідні дані — повинен зберегти виробника та перенаправити на список")
    void postAddManufacturer_validData_shouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/manufacturers/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Samsung"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerService).saveManufacturer(any());
    }

    @Test
    @DisplayName("POST /manufacturers/add: невалідні дані — повинен повернутись на форму без виклику сервісу")
    void postAddManufacturer_invalidData_shouldReturnFormWithoutSaving() throws Exception {
        mockMvc.perform(post("/manufacturers/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacture/newManufacturer"));

        verifyNoInteractions(manufacturerService);
    }

    @Test
    @DisplayName("GET /manufacturers/{id}/edit: повинен повернути форму редагування з даними виробника")
    void getEditManufacturerForm_shouldReturnEditViewWithManufacturerData() throws Exception {
        ManufacturerRequestDTO dto = new ManufacturerRequestDTO();
        dto.setName("Samsung");
        when(manufacturerService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/manufacturers/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacture/editManufacturer"))
                .andExpect(model().attribute("manufacturer", dto))
                .andExpect(model().attribute("manufacturerId", 1L));
    }

    @Test
    @DisplayName("PUT /manufacturers/{id}/edit: валідні дані — повинен оновити виробника та перенаправити на список")
    void putEditManufacturer_validData_shouldUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/manufacturers/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Samsung"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerService).updateManufacturer(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /manufacturers/{id}/edit: невалідні дані — повинен повернутись на форму з manufacturerId без виклику сервісу")
    void putEditManufacturer_invalidData_shouldReturnFormWithManufacturerId() throws Exception {
        mockMvc.perform(put("/manufacturers/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacture/editManufacturer"))
                .andExpect(model().attribute("manufacturerId", 1L));

        verifyNoInteractions(manufacturerService);
    }

    @Test
    @DisplayName("DELETE /manufacturers/{id}: повинен видалити виробника та перенаправити на список")
    void deleteManufacturer_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/manufacturers/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturers"));

        verify(manufacturerService).deleteManufacturerById(1L);
    }
}
