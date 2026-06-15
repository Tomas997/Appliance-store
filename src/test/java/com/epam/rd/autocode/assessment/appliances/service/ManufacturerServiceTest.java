package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ManufacturerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ManufacturerServiceTest {

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ManufacturerServiceImpl manufacturerService;

    @Test
    @DisplayName("saveManufacturer: повинен відобразити DTO на сутність і зберегти")
    void saveManufacturer_shouldMapAndSave() {
        ManufacturerRequestDTO dto = new ManufacturerRequestDTO();
        Manufacturer entity = new Manufacturer();

        when(modelMapper.map(dto, Manufacturer.class)).thenReturn(entity);

        manufacturerService.saveManufacturer(dto);

        verify(manufacturerRepository).save(entity);
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO для всіх виробників")
    void findAll_shouldReturnMappedDtoList() {
        Manufacturer m1 = new Manufacturer();
        Manufacturer m2 = new Manufacturer();
        ManufacturerResponseDTO dto1 = new ManufacturerResponseDTO();
        ManufacturerResponseDTO dto2 = new ManufacturerResponseDTO();

        when(manufacturerRepository.findAll()).thenReturn(List.of(m1, m2));
        when(modelMapper.map(m1, ManufacturerResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(m2, ManufacturerResponseDTO.class)).thenReturn(dto2);

        List<ManufacturerResponseDTO> result = manufacturerService.findAll();

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        when(manufacturerRepository.findAll()).thenReturn(List.of());

        List<ManufacturerResponseDTO> result = manufacturerService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Manufacturer manufacturer = new Manufacturer();
        ManufacturerResponseDTO dto = new ManufacturerResponseDTO();
        Page<Manufacturer> page = new PageImpl<>(List.of(manufacturer), pageable, 1);

        when(manufacturerRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(manufacturer, ManufacturerResponseDTO.class)).thenReturn(dto);

        Page<ManufacturerResponseDTO> result = manufacturerService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById: повинен повернути ManufacturerRequestDTO якщо виробника знайдено")
    void findById_whenFound_shouldReturnDTO() {
        Manufacturer manufacturer = new Manufacturer();
        ManufacturerRequestDTO expected = new ManufacturerRequestDTO();

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(manufacturer));
        when(modelMapper.map(manufacturer, ManufacturerRequestDTO.class)).thenReturn(expected);

        ManufacturerRequestDTO result = manufacturerService.findById(1L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("findById: повинен кинути ResourceNotFoundException якщо виробника не знайдено")
    void findById_whenNotFound_shouldThrow() {
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manufacturerService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateManufacturer: повинен оновити назву і зберегти")
    void updateManufacturer_shouldUpdateNameAndSave() {
        Manufacturer existing = new Manufacturer(1L, "Old Name");
        ManufacturerRequestDTO dto = new ManufacturerRequestDTO();
        dto.setName("New Name");

        when(manufacturerRepository.findById(1L)).thenReturn(Optional.of(existing));

        manufacturerService.updateManufacturer(1L, dto);

        ArgumentCaptor<Manufacturer> captor = ArgumentCaptor.forClass(Manufacturer.class);
        verify(manufacturerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("New Name");
    }

    @Test
    @DisplayName("updateManufacturer: повинен кинути ResourceNotFoundException якщо виробника не знайдено")
    void updateManufacturer_whenNotFound_shouldThrow() {
        when(manufacturerRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> manufacturerService.updateManufacturer(42L, new ManufacturerRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("42");
    }

    @Test
    @DisplayName("deleteManufacturerById: повинен делегувати видалення в репозиторій")
    void deleteManufacturerById_shouldDelegateToRepository() {
        manufacturerService.deleteManufacturerById(3L);

        verify(manufacturerRepository).deleteById(3L);
        verifyNoMoreInteractions(manufacturerRepository);
    }
}
