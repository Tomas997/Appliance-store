package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ClientRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ClientServiceImpl clientService;


    @Test
    @DisplayName("saveClient: повинен закодувати пароль перед збереженням")
    void saveClient_shouldEncodePasswordBeforeSaving() {
        ClientRequestDTO clientRequestDTO= new ClientRequestDTO();
        clientRequestDTO.setPassword("rawPassword");

        Client mappedClient = new Client();
        when(modelMapper.map(clientRequestDTO,Client.class)).thenReturn(mappedClient);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        clientService.saveClient(clientRequestDTO);

        ArgumentCaptor<Client> argumentCaptor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO для всіх клієнтів")
    void findAll_shouldReturnMappedDtoList() {
        // Arrange
        Client client1 = new Client();
        Client client2 = new Client();
        ClientResponseDTO dto1 = new ClientResponseDTO();
        ClientResponseDTO dto2 = new ClientResponseDTO();

        when(clientRepository.findAll()).thenReturn(List.of(client1,client2));
        when(modelMapper.map(client1,ClientResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(client2,ClientResponseDTO.class)).thenReturn(dto2);

        List<ClientResponseDTO> all = clientService.findAll();

        assertThat(all).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenNoClients_shouldReturnEmptyList() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientResponseDTO> result = clientService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0,10, Sort.by("name"));
        Client client = new Client();
        ClientResponseDTO clientResponseDTO = new ClientResponseDTO();
        Page<Client> page = new PageImpl<>(List.of(client),pageable,1);

        when(modelMapper.map(client,ClientResponseDTO.class)).thenReturn(clientResponseDTO);
        when(clientRepository.findAll(pageable)).thenReturn(page);

        Page<ClientResponseDTO> all = clientService.findAll(pageable);

        assertThat(all.getContent()).containsExactly(clientResponseDTO);
        assertThat(all.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("findById: повинен повернути ClientUpdateDTO якщо клієнт існує")
    void findById_whenClientExists_shouldReturnUpdateDTO() {
        Client client = new Client();
        ClientUpdateDTO expectedDto = new ClientUpdateDTO();

        when(modelMapper.map(client, ClientUpdateDTO.class)).thenReturn(expectedDto);
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        ClientUpdateDTO clientById = clientService.findById(1L);

        assertThat(clientById).isSameAs(expectedDto);
    }

    @Test
    @DisplayName("findById: повинен кинути RuntimeException якщо клієнт не знайдений")
    void findById_whenClientNotFound_shouldThrowRuntimeException() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateClient: повинен оновити поля і закодувати новий пароль")
    void updateClient_withNewPassword_shouldEncodeAndSave() {
        Client existingClient = new Client();
        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        dto.setCard("1234");
        dto.setPassword("newPassword");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        clientService.updateClient(1L,dto);

        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());

        Client saved = captor.getValue();

        assertThat(saved.getName()).isEqualTo("Andrii");
        assertThat(saved.getEmail()).isEqualTo("andrii@kpi.ua");
        assertThat(saved.getCard()).isEqualTo("1234");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("updateClient: якщо пароль null — НЕ повинен оновлювати пароль")
    void updateClient_whenPasswordNull_shouldNotChangePassword() {
        Client existingClient = new Client();
        existingClient.setPassword("oldEncoded");
        ClientUpdateDTO clientUpdateDTO = new ClientUpdateDTO();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));

        clientService.updateClient(1L,clientUpdateDTO);

        verifyNoInteractions(passwordEncoder);
        ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
        verify(clientRepository).save(captor.capture());
        Client saved = captor.getValue();

        assertThat(saved.getPassword()).isEqualTo("oldEncoded");
    }

    @Test
    @DisplayName("updateClient: якщо пароль blank — НЕ повинен оновлювати пароль")
    void updateClient_whenPasswordBlank_shouldNotChangePassword() {
        Client existingClient = new Client();
        existingClient.setPassword("oldEncoded");

        ClientUpdateDTO dto = new ClientUpdateDTO();
        dto.setPassword("   ");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));

        clientService.updateClient(1L, dto);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updateClient: повинен кинути RuntimeException якщо клієнт не знайдений")
    void updateClient_whenClientNotFound_shouldThrow() {
        when(clientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.updateClient(404L, new ClientUpdateDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("deleteClientById: повинен делегувати видалення в репозиторій")
    void deleteClientById_shouldDelegateToRepository() {
        clientService.deleteClientById(5L);

        verify(clientRepository).deleteById(5L);
        verifyNoMoreInteractions(clientRepository);
    }
}
