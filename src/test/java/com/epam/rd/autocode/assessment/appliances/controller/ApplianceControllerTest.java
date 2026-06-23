package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApplianceControllerTest {

    @Mock
    private ApplianceService applianceService;

    @Mock
    private ManufacturerService manufacturerService;

    @InjectMocks
    private ApplianceController applianceController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applianceController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /appliances: повинен повернути сторінку зі списком приладів та атрибутами пагінації")
    void getAppliances_shouldReturnAppliancesViewWithPaginationAttributes() throws Exception {
        ApplianceResponseDTO dto = new ApplianceResponseDTO(1L, "Fridge", Category.BIG, "X100",
                "Samsung", PowerType.AC220, "Cooling", "A nice fridge", 200, BigDecimal.valueOf(999.99));
        Page<ApplianceResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(applianceService.search(any(), any())).thenReturn(page);

        mockMvc.perform(get("/appliances"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/appliances"))
                .andExpect(model().attributeExists("appliances", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("appliances", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /appliances?sort=name,desc: сортування з query-параметра має передаватись у сервіс")
    void getAppliances_withCustomSort_shouldBindFromRequest() throws Exception {
        when(applianceService.search(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/appliances").param("sort", "name,desc"))
                .andExpect(model().attribute("sortField", "name"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(applianceService).search(isNull(), captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /appliances?q=fridge: повинен передати пошуковий запит у сервіс і в модель")
    void getAppliances_withSearchQuery_shouldPassQueryToServiceAndModel() throws Exception {
        when(applianceService.search(eq("fridge"), any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/appliances").param("q", "fridge"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("q", "fridge"));

        verify(applianceService).search(eq("fridge"), any());
    }

    @Test
    @DisplayName("GET /appliances/add: повинен повернути форму для додавання нового прилада")
    void getAddApplianceForm_shouldReturnNewApplianceView() throws Exception {
        when(manufacturerService.findAll()).thenReturn(List.of(new ManufacturerResponseDTO(1L, "Samsung")));

        mockMvc.perform(get("/appliances/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/newAppliance"))
                .andExpect(model().attributeExists("appliance", "categories", "powerTypes", "manufacturers"));
    }

    @Test
    @DisplayName("POST /appliances/add: валідні дані — повинен зберегти прилад та перенаправити на список")
    void postAddAppliance_validData_shouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/appliances/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Fridge")
                        .param("category", "BIG")
                        .param("model", "X100")
                        .param("manufacturerId", "1")
                        .param("powerType", "AC220")
                        .param("characteristic", "Cooling")
                        .param("description", "A nice fridge")
                        .param("power", "200")
                        .param("price", "999.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliances"));

        verify(applianceService).saveAppliance(any());
    }

    @Test
    @DisplayName("POST /appliances/add: невалідні дані — повинен повернутись на форму без виклику сервісу")
    void postAddAppliance_invalidData_shouldReturnFormWithoutSaving() throws Exception {
        when(manufacturerService.findAll()).thenReturn(List.of());

        mockMvc.perform(post("/appliances/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("model", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/newAppliance"));

        verify(applianceService, never()).saveAppliance(any());
    }

    @Test
    @DisplayName("GET /appliances/{id}/edit: повинен повернути форму редагування з даними прилада")
    void getEditApplianceForm_shouldReturnEditViewWithApplianceData() throws Exception {
        ApplianceRequestDTO dto = new ApplianceRequestDTO();
        dto.setName("Fridge");
        dto.setCategory(Category.BIG);
        dto.setModel("X100");
        dto.setManufacturerId(1L);
        dto.setPowerType(PowerType.AC220);
        when(applianceService.findById(1L)).thenReturn(dto);
        when(manufacturerService.findAll()).thenReturn(List.of(new ManufacturerResponseDTO(1L, "Samsung")));

        mockMvc.perform(get("/appliances/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/editAppliance"))
                .andExpect(model().attribute("appliance", dto))
                .andExpect(model().attribute("applianceId", 1L));
    }

    @Test
    @DisplayName("PUT /appliances/{id}/edit: валідні дані — повинен оновити прилад та перенаправити на список")
    void putEditAppliance_validData_shouldUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/appliances/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Fridge")
                        .param("category", "BIG")
                        .param("model", "X100")
                        .param("manufacturerId", "1")
                        .param("powerType", "AC220")
                        .param("power", "200")
                        .param("price", "999.99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliances"));

        verify(applianceService).updateAppliance(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /appliances/{id}/edit: невалідні дані — повинен повернутись на форму з applianceId без виклику сервісу")
    void putEditAppliance_invalidData_shouldReturnFormWithApplianceId() throws Exception {
        when(manufacturerService.findAll()).thenReturn(List.of());

        mockMvc.perform(put("/appliances/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("model", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("appliance/editAppliance"))
                .andExpect(model().attribute("applianceId", 1L));

        verify(applianceService, never()).updateAppliance(any(), any());
    }

    @Test
    @DisplayName("DELETE /appliances/{id}: повинен видалити прилад та перенаправити на список")
    void deleteAppliance_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/appliances/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliances"));

        verify(applianceService).deleteApplianceById(1L);
    }
}
