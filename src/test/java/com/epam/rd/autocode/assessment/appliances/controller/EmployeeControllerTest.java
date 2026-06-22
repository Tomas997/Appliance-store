package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.EmployeeRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeResponseDTO;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
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
class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /employees: повинен повернути сторінку зі списком співробітників та атрибутами пагінації")
    void getEmployees_shouldReturnEmployeesViewWithPaginationAttributes() throws Exception {
        EmployeeResponseDTO dto = new EmployeeResponseDTO(1L, "Andrii", "andrii@kpi.ua", "Sales");
        Page<EmployeeResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(employeeService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employees"))
                .andExpect(model().attributeExists("employees", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("employees", page))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /employees?sort=name,desc: сортування з query-параметра має передаватись у сервіс")
    void getEmployees_withCustomSort_shouldBindFromRequest() throws Exception {
        when(employeeService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/employees").param("sort", "name,desc"))
                .andExpect(model().attribute("sortField", "name"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(employeeService).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("GET /employees/add: повинен повернути форму для додавання нового співробітника")
    void getAddEmployeeForm_shouldReturnNewEmployeeView() throws Exception {
        mockMvc.perform(get("/employees/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/newEmployee"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    @DisplayName("POST /employees/add: валідні дані — повинен зберегти співробітника та перенаправити на список")
    void postAddEmployee_validData_shouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Andrii")
                        .param("email", "andrii@kpi.ua")
                        .param("password", "Admin123")
                        .param("department", "Sales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).saveEmployee(any());
    }

    @Test
    @DisplayName("POST /employees/add: невалідні дані — повинен повернутись на форму без виклику сервісу")
    void postAddEmployee_invalidData_shouldReturnFormWithoutSaving() throws Exception {
        mockMvc.perform(post("/employees/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "")
                        .param("password", "")
                        .param("department", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/newEmployee"));

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("GET /employees/{id}/edit: повинен повернути форму редагування з даними співробітника")
    void getEditEmployeeForm_shouldReturnEditViewWithEmployeeData() throws Exception {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        dto.setPassword("Admin123");
        dto.setDepartment("Sales");
        when(employeeService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/employees/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/editEmployee"))
                .andExpect(model().attribute("employee", dto))
                .andExpect(model().attribute("employeeId", 1L));
    }

    @Test
    @DisplayName("PUT /employees/{id}/edit: валідні дані — повинен оновити співробітника та перенаправити на список")
    void putEditEmployee_validData_shouldUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/employees/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Andrii")
                        .param("email", "andrii@kpi.ua")
                        .param("password", "Admin123")
                        .param("department", "Sales"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).updateEmployee(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /employees/{id}/edit: невалідні дані — повинен повернутись на форму з employeeId без виклику сервісу")
    void putEditEmployee_invalidData_shouldReturnFormWithEmployeeId() throws Exception {
        mockMvc.perform(put("/employees/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "")
                        .param("password", "")
                        .param("department", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/editEmployee"))
                .andExpect(model().attribute("employeeId", 1L));

        verifyNoInteractions(employeeService);
    }

    @Test
    @DisplayName("DELETE /employees/{id}: повинен видалити співробітника та перенаправити на список")
    void deleteEmployee_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/employees/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).deleteEmployeeById(1L);
    }
}
