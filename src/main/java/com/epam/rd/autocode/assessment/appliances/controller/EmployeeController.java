package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.EmployeeRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.EmployeeUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping("")
    public String employees(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        String sortField = order.getProperty();
        String sortDir = order.getDirection().name().toLowerCase();

        Page<EmployeeResponseDTO> employees = employeeService.findAll(pageable);
        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", employees.getNumber());
        model.addAttribute("totalPages", employees.getTotalPages());
        model.addAttribute("pageSize", employees.getSize());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        return "employee/employees";
    }

    @GetMapping("/add")
    public String addEmployeeForm(Model model) {
        model.addAttribute("employee", new EmployeeRequestDTO());
        return "employee/newEmployee";
    }

    @PostMapping("/add")
    public String createEmployee(@Valid @ModelAttribute("employee") EmployeeRequestDTO employee,
                                 BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "employee/newEmployee";
        }
        employeeService.saveEmployee(employee);
        return "redirect:/employees";
    }

    @GetMapping("/{id}/edit")
    public String editEmployeeForm(@PathVariable Long id, Model model) {
        model.addAttribute("employee", employeeService.findById(id));
        model.addAttribute("employeeId", id);
        return "employee/editEmployee";
    }

    @PutMapping("/{id}/edit")
    public String editEmployee(@PathVariable Long id,
                               @Valid @ModelAttribute("employee") EmployeeUpdateDTO employee,
                               BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeId", id);
            return "employee/editEmployee";
        }
        employeeService.updateEmployee(id, employee);
        return "redirect:/employees";
    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployeeById(id);
        return "redirect:/employees";
    }
}
