package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.DelivererRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DelivererService {
    List<DelivererResponseDTO> findAll();
    Page<DelivererResponseDTO> findAll(Pageable pageable);
    void saveDeliverer(DelivererRequestDTO dto);
    DelivererUpdateDTO findById(Long id);
    void updateDeliverer(Long id, DelivererUpdateDTO dto);
    void deleteDelivererById(Long id);
}
