package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.ApplianceServiceImpl;
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
public class ApplianceServiceTest {

    @Mock
    private ApplianceRepository applianceRepository;

    @Mock
    private ManufacturerRepository manufacturerRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ApplianceServiceImpl applianceService;

    @Test
    @DisplayName("saveAppliance: повинен знайти виробника, відобразити DTO і зберегти")
    void saveAppliance_shouldResolveManufacturerAndSave() {
        ApplianceRequestDTO dto = new ApplianceRequestDTO();
        dto.setManufacturerId(10L);

        Manufacturer manufacturer = new Manufacturer(10L, "Samsung");
        Appliance appliance = new Appliance();

        when(modelMapper.map(dto, Appliance.class)).thenReturn(appliance);
        when(manufacturerRepository.findById(10L)).thenReturn(Optional.of(manufacturer));

        applianceService.saveAppliance(dto);

        ArgumentCaptor<Appliance> captor = ArgumentCaptor.forClass(Appliance.class);
        verify(applianceRepository).save(captor.capture());
        assertThat(captor.getValue().getManufacturer()).isSameAs(manufacturer);
    }

    @Test
    @DisplayName("saveAppliance: якщо виробника не знайдено — кинути ResourceNotFoundException")
    void saveAppliance_whenManufacturerNotFound_shouldThrow() {
        ApplianceRequestDTO dto = new ApplianceRequestDTO();
        dto.setManufacturerId(99L);

        Appliance appliance = new Appliance();
        when(modelMapper.map(dto, Appliance.class)).thenReturn(appliance);
        when(manufacturerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applianceService.saveAppliance(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO для всіх приладів")
    void findAll_shouldReturnMappedDtoList() {
        Appliance a1 = new Appliance();
        Appliance a2 = new Appliance();
        ApplianceResponseDTO dto1 = new ApplianceResponseDTO();
        ApplianceResponseDTO dto2 = new ApplianceResponseDTO();

        when(applianceRepository.findAll()).thenReturn(List.of(a1, a2));
        when(modelMapper.map(a1, ApplianceResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(a2, ApplianceResponseDTO.class)).thenReturn(dto2);

        List<ApplianceResponseDTO> result = applianceService.findAll();

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenEmpty_shouldReturnEmptyList() {
        when(applianceRepository.findAll()).thenReturn(List.of());

        List<ApplianceResponseDTO> result = applianceService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 5, Sort.by("name"));
        Appliance appliance = new Appliance();
        ApplianceResponseDTO dto = new ApplianceResponseDTO();
        Page<Appliance> page = new PageImpl<>(List.of(appliance), pageable, 1);

        when(applianceRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(appliance, ApplianceResponseDTO.class)).thenReturn(dto);

        Page<ApplianceResponseDTO> result = applianceService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById: повинен повернути ApplianceRequestDTO з manufacturerId якщо прилад знайдено")
    void findById_whenFound_shouldReturnDTOWithManufacturerId() {
        Manufacturer manufacturer = new Manufacturer(5L, "LG");
        Appliance appliance = new Appliance();
        appliance.setManufacturer(manufacturer);

        ApplianceRequestDTO dto = new ApplianceRequestDTO();

        when(applianceRepository.findById(1L)).thenReturn(Optional.of(appliance));
        when(modelMapper.map(appliance, ApplianceRequestDTO.class)).thenReturn(dto);

        ApplianceRequestDTO result = applianceService.findById(1L);

        assertThat(result).isSameAs(dto);
        assertThat(result.getManufacturerId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("findById: повинен кинути ResourceNotFoundException якщо прилад не знайдено")
    void findById_whenNotFound_shouldThrow() {
        when(applianceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applianceService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateAppliance: повинен відобразити DTO, встановити виробника і зберегти")
    void updateAppliance_shouldMapAndUpdateManufacturerAndSave() {
        Appliance existing = new Appliance();
        Manufacturer manufacturer = new Manufacturer(2L, "Bosch");

        ApplianceRequestDTO dto = new ApplianceRequestDTO();
        dto.setManufacturerId(2L);

        when(applianceRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(manufacturerRepository.findById(2L)).thenReturn(Optional.of(manufacturer));

        applianceService.updateAppliance(1L, dto);

        ArgumentCaptor<Appliance> captor = ArgumentCaptor.forClass(Appliance.class);
        verify(applianceRepository).save(captor.capture());
        assertThat(captor.getValue().getManufacturer()).isSameAs(manufacturer);
    }

    @Test
    @DisplayName("updateAppliance: повинен кинути ResourceNotFoundException якщо прилад не знайдено")
    void updateAppliance_whenApplianceNotFound_shouldThrow() {
        when(applianceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applianceService.updateAppliance(99L, new ApplianceRequestDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateAppliance: повинен кинути ResourceNotFoundException якщо виробника не знайдено")
    void updateAppliance_whenManufacturerNotFound_shouldThrow() {
        ApplianceRequestDTO dto = new ApplianceRequestDTO();
        dto.setManufacturerId(88L);

        when(applianceRepository.findById(1L)).thenReturn(Optional.of(new Appliance()));
        when(manufacturerRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applianceService.updateAppliance(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("88");
    }

    @Test
    @DisplayName("deleteApplianceById: повинен делегувати видалення в репозиторій")
    void deleteApplianceById_shouldDelegateToRepository() {
        applianceService.deleteApplianceById(4L);

        verify(applianceRepository).deleteById(4L);
        verifyNoMoreInteractions(applianceRepository);
    }
}
