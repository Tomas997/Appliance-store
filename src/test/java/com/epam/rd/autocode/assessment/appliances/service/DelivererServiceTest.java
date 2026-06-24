package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.DelivererRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.model.Deliverer;
import com.epam.rd.autocode.assessment.appliances.repository.DelivererRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.DelivererServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DelivererServiceTest {
    @Mock
    private DelivererRepository delivererRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailUniquenessService emailUniquenessService;

    @InjectMocks
    private DelivererServiceImpl delivererService;

    @Test
    @DisplayName("saveDeliverer: повинен закодувати пароль перед збереженням")
    void saveDeliverer_shouldEncodePasswordBeforeSaving() {
        DelivererRequestDTO dto = new DelivererRequestDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        dto.setPassword("rawPassword");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        delivererService.saveDeliverer(dto);

        ArgumentCaptor<Deliverer> captor = ArgumentCaptor.forClass(Deliverer.class);
        verify(delivererRepository).saveAndFlush(captor.capture());

        Deliverer saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Andrii");
        assertThat(saved.getEmail()).isEqualTo("andrii@kpi.ua");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO для всіх доставщиків")
    void findAll_shouldReturnMappedDtoList() {
        Deliverer deliverer1 = new Deliverer(1L, "Name1", "email1@test.com", "pass1");
        Deliverer deliverer2 = new Deliverer(2L, "Name2", "email2@test.com", "pass2");

        when(delivererRepository.findAll()).thenReturn(List.of(deliverer1, deliverer2));

        List<DelivererResponseDTO> all = delivererService.findAll();

        assertThat(all).hasSize(2);
        assertThat(all.get(0).getId()).isEqualTo(1L);
        assertThat(all.get(0).getName()).isEqualTo("Name1");
        assertThat(all.get(0).getEmail()).isEqualTo("email1@test.com");
        assertThat(all.get(1).getId()).isEqualTo(2L);
        assertThat(all.get(1).getName()).isEqualTo("Name2");
        assertThat(all.get(1).getEmail()).isEqualTo("email2@test.com");
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenNoDeliverers_shouldReturnEmptyList() {
        when(delivererRepository.findAll()).thenReturn(List.of());

        List<DelivererResponseDTO> result = delivererService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Deliverer deliverer = new Deliverer(1L, "Name1", "email1@test.com", "pass1");
        Page<Deliverer> page = new PageImpl<>(List.of(deliverer), pageable, 1);

        when(delivererRepository.findAll(pageable)).thenReturn(page);

        Page<DelivererResponseDTO> all = delivererService.findAll(pageable);

        assertThat(all.getContent()).hasSize(1);
        assertThat(all.getContent().get(0).getId()).isEqualTo(1L);
        assertThat(all.getContent().get(0).getName()).isEqualTo("Name1");
        assertThat(all.getContent().get(0).getEmail()).isEqualTo("email1@test.com");
        assertThat(all.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById: повинен повернути DelivererUpdateDTO якщо доставщик існує")
    void findById_whenDelivererExists_shouldReturnUpdateDTO() {
        Deliverer deliverer = new Deliverer(1L, "Andrii", "andrii@kpi.ua", "encoded");

        when(delivererRepository.findById(1L)).thenReturn(Optional.of(deliverer));

        DelivererUpdateDTO result = delivererService.findById(1L);

        assertThat(result.getName()).isEqualTo("Andrii");
        assertThat(result.getEmail()).isEqualTo("andrii@kpi.ua");
    }

    @Test
    @DisplayName("findById: повинен кинути RuntimeException якщо доставщик не знайдений")
    void findById_whenDelivererNotFound_shouldThrowRuntimeException() {
        when(delivererRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delivererService.findById(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateDeliverer: повинен оновити поля і закодувати новий пароль")
    void updateDeliverer_withNewPassword_shouldEncodeAndSave() {
        Deliverer existingDeliverer = new Deliverer(1L, "OldName", "old@test.com", "oldEncoded");
        DelivererUpdateDTO dto = new DelivererUpdateDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        dto.setPassword("newPassword");

        when(delivererRepository.findById(1L)).thenReturn(Optional.of(existingDeliverer));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        delivererService.updateDeliverer(1L, dto);

        ArgumentCaptor<Deliverer> captor = ArgumentCaptor.forClass(Deliverer.class);
        verify(delivererRepository).saveAndFlush(captor.capture());

        Deliverer saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Andrii");
        assertThat(saved.getEmail()).isEqualTo("andrii@kpi.ua");
        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("updateDeliverer: якщо пароль null — НЕ повинен оновлювати пароль")
    void updateDeliverer_whenPasswordNull_shouldNotChangePassword() {
        Deliverer existingDeliverer = new Deliverer(1L, "OldName", "old@test.com", "oldEncoded");
        DelivererUpdateDTO dto = new DelivererUpdateDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");

        when(delivererRepository.findById(1L)).thenReturn(Optional.of(existingDeliverer));

        delivererService.updateDeliverer(1L, dto);

        verifyNoInteractions(passwordEncoder);
        ArgumentCaptor<Deliverer> captor = ArgumentCaptor.forClass(Deliverer.class);
        verify(delivererRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("oldEncoded");
    }

    @Test
    @DisplayName("updateDeliverer: якщо пароль blank — НЕ повинен оновлювати пароль")
    void updateDeliverer_whenPasswordBlank_shouldNotChangePassword() {
        Deliverer existingDeliverer = new Deliverer(1L, "OldName", "old@test.com", "oldEncoded");
        DelivererUpdateDTO dto = new DelivererUpdateDTO();
        dto.setName("Andrii");
        dto.setEmail("andrii@kpi.ua");
        dto.setPassword("   ");

        when(delivererRepository.findById(1L)).thenReturn(Optional.of(existingDeliverer));

        delivererService.updateDeliverer(1L, dto);

        verifyNoInteractions(passwordEncoder);
        ArgumentCaptor<Deliverer> captor = ArgumentCaptor.forClass(Deliverer.class);
        verify(delivererRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("oldEncoded");
    }

    @Test
    @DisplayName("updateDeliverer: повинен кинути RuntimeException якщо доставщик не знайдений")
    void updateDeliverer_whenDelivererNotFound_shouldThrow() {
        when(delivererRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> delivererService.updateDeliverer(404L, new DelivererUpdateDTO()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("deleteDelivererById: повинен делегувати видалення в репозиторій")
    void deleteDelivererById_shouldDelegateToRepository() {
        delivererService.deleteDelivererById(5L);

        verify(delivererRepository).deleteById(5L);
        verifyNoMoreInteractions(delivererRepository);
    }
}