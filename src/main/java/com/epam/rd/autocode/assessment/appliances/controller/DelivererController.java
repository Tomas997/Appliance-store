package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.DelivererRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.DelivererUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.service.DelivererService;
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

@AllArgsConstructor
@Controller
@RequestMapping("/deliverers")
public class DelivererController {
    private final DelivererService delivererService;

    @GetMapping("")
    public String deliverers(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                             Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        Page<?> page = delivererService.findAll(pageable);
        model.addAttribute("deliverers", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("sortField", order.getProperty());
        model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        return "deliverer/deliverers";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("deliverer", new DelivererRequestDTO());
        return "deliverer/newDeliverer";
    }

    @PostMapping("/add")
    public String create(@Valid @ModelAttribute("deliverer") DelivererRequestDTO deliverer,
                         BindingResult result) {
        if (result.hasErrors()) {
            return "deliverer/newDeliverer";
        }
        delivererService.saveDeliverer(deliverer);
        return "redirect:/deliverers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("deliverer", delivererService.findById(id));
        model.addAttribute("delivererId", id);
        return "deliverer/editDeliverer";
    }

    @PutMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("deliverer") DelivererUpdateDTO deliverer,
                       BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("delivererId", id);
            return "deliverer/editDeliverer";
        }
        delivererService.updateDeliverer(id, deliverer);
        return "redirect:/deliverers";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        delivererService.deleteDelivererById(id);
        return "redirect:/deliverers";
    }
}
