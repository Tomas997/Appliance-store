package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.OrderRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.OrderResponseDTO;
import com.epam.rd.autocode.assessment.appliances.service.ApplianceService;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import com.epam.rd.autocode.assessment.appliances.service.EmployeeService;
import com.epam.rd.autocode.assessment.appliances.service.OrderService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Controller
@RequestMapping("/orders")
public class OrdersController {
    private final OrderService orderService;
    private final EmployeeService employeeService;
    private final ClientService clientService;
    private final ApplianceService applianceService;

    @GetMapping("")
    public String orders(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        Page<OrderResponseDTO> orders = orderService.findAll(pageable);
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", orders.getNumber());
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageSize", orders.getSize());
        model.addAttribute("sortField", order.getProperty());
        model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        return "order/orders";
    }

    @GetMapping("/add")
    public String addOrderForm(Model model) {
        model.addAttribute("order", new OrderRequestDTO());
        addFormData(model);
        return "order/newOrder";
    }

    @PostMapping("/add")
    public String addOrder(@Valid @ModelAttribute("order") OrderRequestDTO order,
                           BindingResult result, Model model) {
        if (result.hasErrors()) {
            addFormData(model);
            return "order/newOrder";
        }
        orderService.saveOrder(order);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/edit")
    public String editOrderForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("orderId", id);
        model.addAttribute("rows", orderService.getOrderRows(id));
        addFormData(model);
        return "order/editOrder";
    }

    @PutMapping("/{id}/edit")
    public String editOrder(@PathVariable Long id,
                            @Valid @ModelAttribute("order") OrderRequestDTO order,
                            BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("orderId", id);
            addFormData(model);
            return "order/editOrder";
        }
        orderService.updateOrder(id, order);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/delete")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/approved")
    public String approveOrder(@PathVariable Long id) {
        orderService.approveOrder(id, true);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/unapproved")
    public String unapproveOrder(@PathVariable Long id) {
        orderService.approveOrder(id, false);
        return "redirect:/orders";
    }

    @GetMapping("/{id}/choice-appliance")
    public String choiceAppliance(@PathVariable Long id, Model model) {
        model.addAttribute("ordersId", id);
        model.addAttribute("appliances", applianceService.findAll());
        return "order/choiceAppliance";
    }

    @PostMapping("/add-into-order")
    public String addIntoOrder(@RequestParam Long ordersId,
                               @RequestParam Long applianceId,
                               @RequestParam Long numbers,
                               @RequestParam BigDecimal price) {
        orderService.addRowToOrder(ordersId, applianceId, numbers, price);
        return "redirect:/orders/" + ordersId + "/edit";
    }

    @GetMapping("/{rowId}/delete-row")
    public String deleteRow(@PathVariable Long rowId,
                            @RequestParam Long orderId) {
        orderService.deleteRowFromOrder(rowId);
        return "redirect:/orders/" + orderId + "/edit";
    }

    private void addFormData(Model model) {
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("clients", clientService.findAll());
    }
}
