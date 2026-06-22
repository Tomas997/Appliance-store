package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.aspect.Loggable;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Deliverer;
import com.epam.rd.autocode.assessment.appliances.repository.DelivererRepository;
import com.epam.rd.autocode.assessment.appliances.service.DelivererService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DelivererServiceImpl implements DelivererService {
    private final DelivererRepository delivererRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<DelivererResponseDTO> findAll() {
        return delivererRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<DelivererResponseDTO> findAll(Pageable pageable) {
        return delivererRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Loggable
    @PreAuthorize("hasRole('ADMIN')")
    public void saveDeliverer(DelivererRequestDTO dto) {
        Deliverer deliverer = new Deliverer();
        deliverer.setName(dto.getName());
        deliverer.setEmail(dto.getEmail());
        deliverer.setPassword(passwordEncoder.encode(dto.getPassword()));
        delivererRepository.save(deliverer);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public DelivererUpdateDTO findById(Long id) {
        Deliverer deliverer = delivererRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverer", id));
        DelivererUpdateDTO dto = new DelivererUpdateDTO();
        dto.setName(deliverer.getName());
        dto.setEmail(deliverer.getEmail());
        return dto;
    }

    @Override
    @Loggable
    @PreAuthorize("hasRole('ADMIN')")
    public void updateDeliverer(Long id, DelivererUpdateDTO dto) {
        Deliverer deliverer = delivererRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deliverer", id));
        deliverer.setName(dto.getName());
        deliverer.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            deliverer.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        delivererRepository.save(deliverer);
    }

    @Override
    @Loggable
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteDelivererById(Long id) {
        delivererRepository.deleteById(id);
    }

    private DelivererResponseDTO toDto(Deliverer deliverer) {
        return new DelivererResponseDTO(deliverer.getId(), deliverer.getName(), deliverer.getEmail());
    }
}
