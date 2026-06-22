package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    List<EmployeeResponseDTO> findAll();
    Page<EmployeeResponseDTO> findAll(Pageable pageable);
    void saveEmployee(EmployeeRequestDTO dto);
    EmployeeRequestDTO findById(Long id);
    void updateEmployee(Long id, EmployeeUpdateDTO dto);
    void deleteEmployeeById(Long id);
}
