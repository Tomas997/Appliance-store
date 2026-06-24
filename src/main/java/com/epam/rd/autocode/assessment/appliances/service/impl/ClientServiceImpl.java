package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.aspect.Loggable;
import com.epam.rd.autocode.assessment.appliances.dto.ClientRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.exception.EmailAlreadyInUseException;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import com.epam.rd.autocode.assessment.appliances.service.EmailUniquenessService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final EmailUniquenessService emailUniquenessService;

    @Override
    @Loggable
    @Transactional
    public void saveClient(ClientRequestDTO clientDto) {
        emailUniquenessService.verifyNotAdminEmail(clientDto.getEmail());
        Client client = toEntity(clientDto);
        client.setPassword(passwordEncoder.encode(clientDto.getPassword()));
        try {
            clientRepository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyInUseException(clientDto.getEmail());
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<ClientResponseDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Page<ClientResponseDTO> findAll(Pageable pageable) {
       return clientRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public ClientUpdateDTO findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        return modelMapper.map(client, ClientUpdateDTO.class);
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void updateClient(Long id, ClientUpdateDTO clientDto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        emailUniquenessService.verifyNotAdminEmail(clientDto.getEmail());
        client.setName(clientDto.getName());
        client.setEmail(clientDto.getEmail());
        client.setCard(clientDto.getCard());
        if (clientDto.getPassword() != null && !clientDto.getPassword().isBlank()) {
            client.setPassword(passwordEncoder.encode(clientDto.getPassword()));
        }
        try {
            clientRepository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyInUseException(clientDto.getEmail());
        }
    }

    @Override
    @Loggable
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public void deleteClientById(Long clientId) {
        clientRepository.deleteById(clientId);
    }

    public ClientResponseDTO toDto(Client entity) {
        return modelMapper.map(entity, ClientResponseDTO.class);
    }
    public Client toEntity(ClientRequestDTO entity) {
        return modelMapper.map(entity, Client.class);
    }
}
