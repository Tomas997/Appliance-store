package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
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

import java.math.BigDecimal;

@AllArgsConstructor
@Controller
@RequestMapping("/my-orders")
public class MyOrdersController {
    private final OrderService orderService;
    private final ApplianceService applianceService;

    @GetMapping("")
    public String myOrders(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
                           Model model, Authentication authentication) {
        Sort.Order sort = pageable.getSort().iterator().next();
        Page<?> page = orderService.findByClientEmail(authentication.getName(), pageable);
        model.addAttribute("orders", page.getContent());
        model.addAttribute("currentPage", page.getNumber());
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("pageSize", page.getSize());
        model.addAttribute("sortField", sort.getProperty());
        model.addAttribute("sortDir", sort.getDirection().name().toLowerCase());
        return "my-orders/myOrders";
    }

    @PostMapping("/create")
    public String createOrder(Authentication authentication) {
        Long orderId = orderService.createClientOrder(authentication.getName());
        return "redirect:/my-orders/" + orderId + "/edit";
    }

    @GetMapping("/{id}/edit")
    public String editMyOrder(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findResponseById(id));
        model.addAttribute("orderId", id);
        model.addAttribute("rows", orderService.getOrderRows(id));
        return "my-orders/editMyOrder";
    }

    @GetMapping("/{id}/choice-appliance")
    public String choiceAppliance(@PathVariable Long id, Model model) {
        model.addAttribute("ordersId", id);
        model.addAttribute("appliances", applianceService.findAll());
        return "my-orders/choiceAppliance";
    }

    @PostMapping("/add-into-order")
    public String addIntoOrder(@RequestParam Long ordersId,
                               @RequestParam Long applianceId,
                               @RequestParam Long numbers,
                               @RequestParam BigDecimal price) {
        orderService.addRowToOrder(ordersId, applianceId, numbers, price);
        return "redirect:/my-orders/" + ordersId + "/edit";
    }

    @DeleteMapping("/{rowId}/row")
    public String deleteRow(@PathVariable Long rowId, @RequestParam Long orderId) {
        orderService.deleteRowFromOrder(rowId);
        return "redirect:/my-orders/" + orderId + "/edit";
    }

    @PatchMapping("/{id}/submit")
    public String submitForReview(@PathVariable Long id) {
        orderService.submitForReview(id);
        return "redirect:/my-orders";
    }

    @PatchMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "redirect:/my-orders";
    }

    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return "redirect:/my-orders";
    }
}
