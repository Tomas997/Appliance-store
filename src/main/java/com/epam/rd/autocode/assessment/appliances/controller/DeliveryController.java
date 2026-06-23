package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
@RequestMapping("/deliveries")
public class DeliveryController {
    private final OrderService orderService;

    @GetMapping("")
    public String deliveries(@RequestParam(defaultValue = "pending") String tab,
                             @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                             Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        Page<OrderResponseDTO> orders = switch (tab) {
            case "delivering" -> orderService.findDelivering(pageable);
            case "delivered" -> orderService.findDelivered(pageable);
            default -> orderService.findPendingForDelivery(pageable);
        };
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", orders.getNumber());
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageSize", orders.getSize());
        model.addAttribute("sortField", order.getProperty());
        model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        model.addAttribute("tab", tab);
        return "delivery/deliveries";
    }

    @PatchMapping("/{id}/confirm")
    public String confirmDelivery(@PathVariable Long id, Authentication authentication) {
        orderService.acceptByDeliverer(id, authentication.getName());
        return "redirect:/deliveries";
    }

    @PatchMapping("/{id}/deliver")
    public String markDelivered(@PathVariable Long id, Authentication authentication) {
        orderService.markAsDelivered(id, authentication.getName());
        return "redirect:/deliveries?tab=delivering";
    }
}
