package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.Appliance;
import com.epam.rd.autocode.assessment.appliances.repository.ApplianceRepository;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ApplianceServiceImpl implements ApplianceService {
    private final ApplianceRepository applianceRepository;
    private final ManufacturerRepository manufacturerRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ApplianceResponseDTO> findAll() {
        return applianceRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<ApplianceResponseDTO> findAll(Pageable pageable) {
        return applianceRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public void saveAppliance(ApplianceRequestDTO dto) {
        applianceRepository.save(toEntity(dto));
    }

    @Override
    public ApplianceRequestDTO findById(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", id));
        ApplianceRequestDTO dto = modelMapper.map(appliance, ApplianceRequestDTO.class);
        dto.setManufacturerId(appliance.getManufacturer().getId());
        return dto;
    }

    @Override
    public void updateAppliance(Long id, ApplianceRequestDTO dto) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", id));
        modelMapper.map(dto, appliance);
        appliance.setManufacturer(manufacturerRepository.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer", dto.getManufacturerId())));
        applianceRepository.save(appliance);
    }

    @Override
    public void deleteApplianceById(Long id) {
        applianceRepository.deleteById(id);
    }

    private ApplianceResponseDTO toDto(Appliance appliance) {
        return modelMapper.map(appliance, ApplianceResponseDTO.class);
    }

    private Appliance toEntity(ApplianceRequestDTO dto) {
        Appliance appliance = modelMapper.map(dto, Appliance.class);
        appliance.setManufacturer(manufacturerRepository.findById(dto.getManufacturerId())
                .orElseThrow(() -> new ResourceNotFoundException("Manufacturer", dto.getManufacturerId())));
        return appliance;
    }
}
