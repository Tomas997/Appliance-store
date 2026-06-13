package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.Manufacturer;
import com.epam.rd.autocode.assessment.appliances.repository.ManufacturerRepository;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ManufacturerServiceImpl implements ManufacturerService {
    private final ManufacturerRepository manufacturerRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<ManufacturerResponseDTO> findAll() {
        return manufacturerRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public Page<ManufacturerResponseDTO> findAll(Pageable pageable) {
        return manufacturerRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    public void saveManufacturer(ManufacturerRequestDTO dto) {
        manufacturerRepository.save(toEntity(dto));
    }

    @Override
    public ManufacturerRequestDTO findById(Long id) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manufacturer not found: " + id));
        return modelMapper.map(manufacturer, ManufacturerRequestDTO.class);
    }

    @Override
    public void updateManufacturer(Long id, ManufacturerRequestDTO dto) {
        Manufacturer manufacturer = manufacturerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Manufacturer not found: " + id));
        manufacturer.setName(dto.getName());
        manufacturerRepository.save(manufacturer);
    }

    @Override
    public void deleteManufacturerById(Long id) {
        manufacturerRepository.deleteById(id);
    }

    private ManufacturerResponseDTO toDto(Manufacturer manufacturer) {
        return modelMapper.map(manufacturer, ManufacturerResponseDTO.class);
    }

    private Manufacturer toEntity(ManufacturerRequestDTO dto) {
        return modelMapper.map(dto, Manufacturer.class);
    }
}
