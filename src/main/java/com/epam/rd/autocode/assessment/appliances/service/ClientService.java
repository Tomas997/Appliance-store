package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ClientRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {
    void saveClient(ClientRequestDTO client);
    List<ClientResponseDTO> findAll();
    Page<ClientResponseDTO> findAll(Pageable pageable);
    ClientUpdateDTO findById(Long id);
    void updateClient(Long id, ClientUpdateDTO client);
    void deleteClientById(Long clientId);
}
