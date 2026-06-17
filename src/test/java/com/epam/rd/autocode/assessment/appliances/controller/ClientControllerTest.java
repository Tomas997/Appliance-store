package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
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
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /clients: повинен повернути сторінку зі списком клієнтів та атрибутами пагінації")
    void getClients_shouldReturnClientsViewWithPaginationAttributes() throws Exception {
        ClientResponseDTO dto = new ClientResponseDTO(1L, "Andrii", "andrii@kpi.ua", "1234-5678");
        Page<ClientResponseDTO> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id")),
                1
        );
        when(clientService.findAll(any())).thenReturn(page);

        mockMvc.perform(get("/clients"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/clients"))
                .andExpect(model().attributeExists("clients", "currentPage", "totalPages", "pageSize", "sortField", "sortDir"))
                .andExpect(model().attribute("clients", List.of(dto)))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("sortField", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("GET /clients/add: повинен повернути форму для додавання нового клієнта")
    void getAddClientForm_shouldReturnNewClientView() throws Exception {
        mockMvc.perform(get("/clients/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/newClient"))
                .andExpect(model().attributeExists("client"));
    }

    @Test
    @DisplayName("GET /clients?sort=name,desc: сортування з query-параметра має передаватись у сервіс")
    void getClients_withCustomSort_shouldBindFromRequest() throws Exception {
        when(clientService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/clients").param("sort", "name,desc"))
                .andExpect(model().attribute("sortField", "name"))
                .andExpect(model().attribute("sortDir", "desc"));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(clientService).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("name").getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("POST /clients/add: валідні дані — повинен зберегти клієнта та перенаправити на список")
    void postAddClient_validData_shouldSaveAndRedirect() throws Exception {
        mockMvc.perform(post("/clients/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Andrii")
                        .param("email", "andrii@kpi.ua")
                        .param("password", "Admin123")
                        .param("card", "1234-5678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).saveClient(any());
    }

    @Test
    @DisplayName("POST /clients/add: невалідні дані — повинен повернутись на форму без виклику сервісу")
    void postAddClient_invalidData_shouldReturnFormWithoutSaving() throws Exception {
        mockMvc.perform(post("/clients/add")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "")
                        .param("password", "")
                        .param("card", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("client/newClient"));

        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("GET /clients/{id}/edit: повинен повернути форму редагування з даними клієнта")
    void getEditClientForm_shouldReturnEditViewWithClientData() throws Exception {
        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        dto.setCard("1234-5678");
        when(clientService.findById(1L)).thenReturn(dto);

        mockMvc.perform(get("/clients/{id}/edit", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("client/editClient"))
                .andExpect(model().attribute("client", dto))
                .andExpect(model().attribute("clientId", 1L));
    }

    @Test
    @DisplayName("PUT /clients/{id}/edit: валідні дані — повинен оновити клієнта та перенаправити на список")
    void putEditClient_validData_shouldUpdateAndRedirect() throws Exception {
        mockMvc.perform(put("/clients/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "Andrii")
                        .param("email", "andrii@kpi.ua")
                        .param("card", "1234-5678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).updateClient(eq(1L), any());
    }

    @Test
    @DisplayName("PUT /clients/{id}/edit: невалідні дані — повинен повернутись на форму з clientId без виклику сервісу")
    void putEditClient_invalidData_shouldReturnFormWithClientId() throws Exception {
        mockMvc.perform(put("/clients/{id}/edit", 1L)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("name", "")
                        .param("email", "")
                        .param("card", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("client/editClient"))
                .andExpect(model().attribute("clientId", 1L));

        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("DELETE /clients/{id}: повинен видалити клієнта та перенаправити на список")
    void deleteClient_shouldDeleteAndRedirect() throws Exception {
        mockMvc.perform(delete("/clients/{id}", 1L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).deleteClientById(1L);
    }
}
