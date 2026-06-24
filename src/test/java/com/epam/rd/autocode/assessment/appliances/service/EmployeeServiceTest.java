package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.dto.EmployeeRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.exception.EmailAlreadyInUseException;
import com.epam.rd.autocode.assessment.appliances.exception.ResourceNotFoundException;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import com.epam.rd.autocode.assessment.appliances.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private EmailUniquenessService emailUniquenessService;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    @DisplayName("saveEmployee: повинен закодувати пароль перед збереженням")
    void saveEmployee_shouldEncodePasswordBeforeSaving() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setPassword("rawPassword");

        Employee mapped = new Employee();
        when(modelMapper.map(dto, Employee.class)).thenReturn(mapped);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");

        employeeService.saveEmployee(dto);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("saveEmployee: якщо репозиторій кидає DataIntegrityViolationException — кинути EmailAlreadyInUseException")
    void saveEmployee_whenEmailAlreadyExists_shouldThrowEmailAlreadyInUseException() {
        EmployeeRequestDTO dto = new EmployeeRequestDTO();
        dto.setEmail("dup@epam.com");
        dto.setPassword("rawPassword");

        Employee mapped = new Employee();
        when(modelMapper.map(dto, Employee.class)).thenReturn(mapped);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(employeeRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> employeeService.saveEmployee(dto))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    @DisplayName("findAll: повинен повернути список DTO для всіх співробітників")
    void findAll_shouldReturnMappedDtoList() {
        Employee e1 = new Employee();
        Employee e2 = new Employee();
        EmployeeResponseDTO dto1 = new EmployeeResponseDTO();
        EmployeeResponseDTO dto2 = new EmployeeResponseDTO();

        when(employeeRepository.findAll()).thenReturn(List.of(e1, e2));
        when(modelMapper.map(e1, EmployeeResponseDTO.class)).thenReturn(dto1);
        when(modelMapper.map(e2, EmployeeResponseDTO.class)).thenReturn(dto2);

        List<EmployeeResponseDTO> result = employeeService.findAll();

        assertThat(result).containsExactly(dto1, dto2);
    }

    @Test
    @DisplayName("findAll: якщо репозиторій порожній — повернути порожній список")
    void findAll_whenNoEmployees_shouldReturnEmptyList() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        List<EmployeeResponseDTO> result = employeeService.findAll();

        assertThat(result).isEmpty();
        verifyNoInteractions(modelMapper);
    }

    @Test
    @DisplayName("findAll(Pageable): повинен передати Pageable в репозиторій і повернути сторінку DTO")
    void findAll_withPageable_shouldReturnMappedPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name"));
        Employee employee = new Employee();
        EmployeeResponseDTO dto = new EmployeeResponseDTO();
        Page<Employee> page = new PageImpl<>(List.of(employee), pageable, 1);

        when(employeeRepository.findAll(pageable)).thenReturn(page);
        when(modelMapper.map(employee, EmployeeResponseDTO.class)).thenReturn(dto);

        Page<EmployeeResponseDTO> result = employeeService.findAll(pageable);

        assertThat(result.getContent()).containsExactly(dto);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById: повинен повернути EmployeeRequestDTO якщо співробітник існує")
    void findById_whenEmployeeExists_shouldReturnDTO() {
        Employee employee = new Employee();
        EmployeeRequestDTO expected = new EmployeeRequestDTO();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(modelMapper.map(employee, EmployeeRequestDTO.class)).thenReturn(expected);

        EmployeeRequestDTO result = employeeService.findById(1L);

        assertThat(result).isSameAs(expected);
    }

    @Test
    @DisplayName("findById: повинен кинути ResourceNotFoundException якщо співробітника не знайдено")
    void findById_whenNotFound_shouldThrow() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateEmployee: повинен оновити поля і закодувати новий пароль")
    void updateEmployee_withNewPassword_shouldEncodeAndSave() {
        Employee existing = new Employee();
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setName("Іван");
        dto.setEmail("ivan@epam.com");
        dto.setDepartment("IT");
        dto.setPassword("newSecret");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("newSecret")).thenReturn("encodedSecret");

        employeeService.updateEmployee(1L, dto);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).saveAndFlush(captor.capture());

        Employee saved = captor.getValue();
        assertThat(saved.getName()).isEqualTo("Іван");
        assertThat(saved.getEmail()).isEqualTo("ivan@epam.com");
        assertThat(saved.getDepartment()).isEqualTo("IT");
        assertThat(saved.getPassword()).isEqualTo("encodedSecret");
    }

    @Test
    @DisplayName("updateEmployee: якщо пароль null — НЕ повинен оновлювати пароль")
    void updateEmployee_whenPasswordNull_shouldNotChangePassword() {
        Employee existing = new Employee();
        existing.setPassword("oldEncoded");
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));

        employeeService.updateEmployee(1L, dto);

        verifyNoInteractions(passwordEncoder);
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("oldEncoded");
    }

    @Test
    @DisplayName("updateEmployee: якщо пароль blank — НЕ повинен оновлювати пароль")
    void updateEmployee_whenPasswordBlank_shouldNotChangePassword() {
        Employee existing = new Employee();
        existing.setPassword("oldEncoded");
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setPassword("   ");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));

        employeeService.updateEmployee(1L, dto);

        verifyNoInteractions(passwordEncoder);
    }

    @Test
    @DisplayName("updateEmployee: повинен кинути ResourceNotFoundException якщо співробітника не знайдено")
    void updateEmployee_whenNotFound_shouldThrow() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, new EmployeeUpdateDTO()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateEmployee: якщо репозиторій кидає DataIntegrityViolationException — кинути EmailAlreadyInUseException")
    void updateEmployee_whenEmailAlreadyExists_shouldThrowEmailAlreadyInUseException() {
        Employee existing = new Employee();
        EmployeeUpdateDTO dto = new EmployeeUpdateDTO();
        dto.setEmail("dup@epam.com");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> employeeService.updateEmployee(1L, dto))
                .isInstanceOf(EmailAlreadyInUseException.class);
    }

    @Test
    @DisplayName("deleteEmployeeById: повинен делегувати видалення в репозиторій")
    void deleteEmployeeById_shouldDelegateToRepository() {
        employeeService.deleteEmployeeById(7L);

        verify(employeeRepository).deleteById(7L);
        verifyNoMoreInteractions(employeeRepository);
    }
}
