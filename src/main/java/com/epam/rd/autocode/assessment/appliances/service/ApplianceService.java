package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ApplianceService {
    List<ApplianceResponseDTO> findAll();
    Page<ApplianceResponseDTO> findAll(Pageable pageable);
    Page<ApplianceResponseDTO> search(String name, Pageable pageable);
    void saveAppliance(ApplianceRequestDTO dto);
    ApplianceRequestDTO findById(Long id);
    void updateAppliance(Long id, ApplianceRequestDTO dto);
    void deleteApplianceById(Long id);
}
