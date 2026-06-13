package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ApplianceRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ApplianceResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ManufacturerResponseDTO;
import com.epam.rd.autocode.assessment.appliances.model.Category;
import com.epam.rd.autocode.assessment.appliances.model.PowerType;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
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
@RequestMapping("/appliances")
public class ApplianceController {
    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;

    @GetMapping("")
    public String appliances(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        Page<ApplianceResponseDTO> appliances = applianceService.findAll(pageable);
        model.addAttribute("appliances", appliances.getContent());
        model.addAttribute("currentPage", appliances.getNumber());
        model.addAttribute("totalPages", appliances.getTotalPages());
        model.addAttribute("pageSize", appliances.getSize());
        model.addAttribute("sortField", order.getProperty());
        model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        return "appliance/appliances";
    }

    @GetMapping("/add")
    public String addApplianceForm(Model model) {
        model.addAttribute("appliance", new ApplianceRequestDTO());
        addFormData(model);
        return "appliance/newAppliance";
    }

    @PostMapping("/add")
    public String addAppliance(@Valid @ModelAttribute("appliance") ApplianceRequestDTO appliance,
                               BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormData(model);
            return "appliance/newAppliance";
        }
        applianceService.saveAppliance(appliance);
        return "redirect:/appliances";
    }

    @GetMapping("/{id}/edit")
    public String editApplianceForm(@PathVariable Long id, Model model) {
        model.addAttribute("appliance", applianceService.findById(id));
        model.addAttribute("applianceId", id);
        addFormData(model);
        return "appliance/editAppliance";
    }

    @PutMapping("/{id}/edit")
    public String editAppliance(@PathVariable Long id,
                                @Valid @ModelAttribute("appliance") ApplianceRequestDTO appliance,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("applianceId", id);
            addFormData(model);
            return "appliance/editAppliance";
        }
        applianceService.updateAppliance(id, appliance);
        return "redirect:/appliances";
    }

    @GetMapping("/{id}/delete")
    public String deleteAppliance(@PathVariable Long id) {
        applianceService.deleteApplianceById(id);
        return "redirect:/appliances";
    }

    private void addFormData(Model model) {
        model.addAttribute("categories", Category.values());
        model.addAttribute("powerTypes", PowerType.values());
        List<ManufacturerResponseDTO> manufacturers = manufacturerService.findAll();
        model.addAttribute("manufacturers", manufacturers);
    }
}
