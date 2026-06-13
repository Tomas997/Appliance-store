package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ManufacturerService {
    List<ManufacturerResponseDTO> findAll();
    Page<ManufacturerResponseDTO> findAll(Pageable pageable);
    void saveManufacturer(ManufacturerRequestDTO dto);
    ManufacturerRequestDTO findById(Long id);
    void updateManufacturer(Long id, ManufacturerRequestDTO dto);
    void deleteManufacturerById(Long id);
}
