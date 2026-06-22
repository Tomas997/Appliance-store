package com.epam.rd.autocode.assessment.appliances.service.impl;

import com.epam.rd.autocode.assessment.appliances.aspect.Loggable;
import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public List<EmployeeResponseDTO> findAll() {
        return employeeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN','EMPLOYEE')")
    public Page<EmployeeResponseDTO> findAll(Pageable pageable) {
        return employeeRepository.findAll(pageable)
                .map(this::toDto);
    }

    @Override
    @Loggable
    @PreAuthorize("hasRole('ADMIN')")
    public void saveEmployee(EmployeeRequestDTO dto) {
        Employee employee = toEntity(dto);
        employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        employeeRepository.save(employee);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public EmployeeRequestDTO findById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        return modelMapper.map(employee, EmployeeRequestDTO.class);
    }

    @Override
    @Loggable
    @PreAuthorize("hasRole('ADMIN')")
    public void updateEmployee(Long id, EmployeeUpdateDTO dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", id));
        employee.setName(dto.getName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            employee.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        employeeRepository.save(employee);
    }

    @Override
    @Loggable
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteEmployeeById(Long id) {
        employeeRepository.deleteById(id);
    }

    private EmployeeResponseDTO toDto(Employee employee) {
        return modelMapper.map(employee, EmployeeResponseDTO.class);
    }

    private Employee toEntity(EmployeeRequestDTO dto) {
        return modelMapper.map(dto, Employee.class);
    }
}
