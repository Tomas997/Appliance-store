package com.epam.rd.autocode.assessment.appliances.controller.api;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ApplianceRestControllerTest {

    @Mock
    private ApplianceService applianceService;

    @InjectMocks
    private ApplianceRestController applianceRestController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applianceRestController)
                .setControllerAdvice(new RestExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/appliances: повинен повернути сторінку приладів")
    void findAll_shouldReturnPageOfAppliances() throws Exception {
        ApplianceResponseDTO dto = new ApplianceResponseDTO(1L, "Claw", Category.BIG, "M-1",
                "Samsung", PowerType.ACCUMULATOR, "char", "desc", 600, new BigDecimal("1.01"));
        when(applianceService.search(eq(null), any())).thenReturn(new PageImpl<>(List.of(dto), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/appliances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Claw"));
    }

    @Test
    @DisplayName("GET /api/appliances/{id}: повинен повернути прилад за id")
    void findById_shouldReturnAppliance() throws Exception {
        ApplianceRequestDTO dto = new ApplianceRequestDTO();
        dto.setName("Claw");
        when(applianceService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/appliances/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Claw"));
    }

    @Test
    @DisplayName("GET /api/appliances/{id}: якщо не знайдено — повинен повернути 404")
    void findById_whenNotFound_shouldReturnNotFound() throws Exception {
        when(applianceService.findById(99L)).thenThrow(new ResourceNotFoundException("Appliance", 99L));

        mockMvc.perform(get("/api/appliances/{id}", 99L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/appliances: повинен створити прилад і повернути 201 з Location")
    void create_shouldReturnCreatedWithLocation() throws Exception {
        when(applianceService.saveAppliance(any())).thenReturn(42L);

        mockMvc.perform(post("/api/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Claw","category":"BIG","model":"M-1","manufacturerId":1,"powerType":"ACCUMULATOR"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/appliances/42")))
                .andExpect(jsonPath("$").value(42));
    }

    @Test
    @DisplayName("POST /api/appliances: невалідні дані — повинен повернути 400")
    void create_invalidData_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/appliances")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(applianceService);
    }

    @Test
    @DisplayName("PUT /api/appliances/{id}: повинен оновити прилад і повернути 204")
    void update_shouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/appliances/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Claw","category":"BIG","model":"M-1","manufacturerId":1,"powerType":"ACCUMULATOR"}
                                """))
                .andExpect(status().isNoContent());

        verify(applianceService).updateAppliance(eq(1L), any());
    }

    @Test
    @DisplayName("DELETE /api/appliances/{id}: повинен видалити прилад і повернути 204")
    void delete_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/appliances/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(applianceService).deleteApplianceById(1L);
    }
}
