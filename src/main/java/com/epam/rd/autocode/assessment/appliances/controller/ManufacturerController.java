package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import com.epam.rd.autocode.assessment.appliances.service.ManufacturerService;
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
@RequestMapping("/manufacturers")
public class ManufacturerController {
    private final ManufacturerService manufacturerService;

    @GetMapping("")
    public String manufacturers(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Model model) {
        Page<ManufacturerResponseDTO> manufacturers = manufacturerService.findAll(pageable);

        Sort.Order order = pageable.getSort().iterator().next();
        String sortField = order.getProperty();
        String sortDir = order.getDirection().name().toLowerCase();

        model.addAttribute("manufacturers", manufacturers.getContent());
        model.addAttribute("currentPage", manufacturers.getNumber());
        model.addAttribute("totalPages", manufacturers.getTotalPages());
        model.addAttribute("pageSize", manufacturers.getSize());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        return "manufacture/manufacturers";
    }

    @GetMapping("/add")
    public String addManufacturerForm(Model model) {
        model.addAttribute("manufacturer", new ManufacturerRequestDTO());
        return "manufacture/newManufacturer";
    }

    @PostMapping("/add")
    public String addManufacturer(@Valid @ModelAttribute("manufacturer") ManufacturerRequestDTO manufacturer,
                                  BindingResult result) {
        if (result.hasErrors()) {
            return "manufacture/newManufacturer";
        }
        manufacturerService.saveManufacturer(manufacturer);
        return "redirect:/manufacturers";
    }

    @GetMapping("/{id}/edit")
    public String editManufacturerForm(@PathVariable Long id, Model model) {
        model.addAttribute("manufacturer", manufacturerService.findById(id));
        model.addAttribute("manufacturerId", id);
        return "manufacture/editManufacturer";
    }

    @PutMapping("/{id}/edit")
    public String editManufacturer(@PathVariable Long id,
                                   @Valid @ModelAttribute("manufacturer") ManufacturerRequestDTO manufacturer,
                                   BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("manufacturerId", id);
            return "manufacture/editManufacturer";
        }
        manufacturerService.updateManufacturer(id, manufacturer);
        return "redirect:/manufacturers";
    }

    @GetMapping("/{id}/delete")
    public String deleteManufacturer(@PathVariable Long id) {
        manufacturerService.deleteManufacturerById(id);
        return "redirect:/manufacturers";
    }
}
