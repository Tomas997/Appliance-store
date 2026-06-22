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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@Controller
@RequestMapping("/orders")
public class OrdersController {
    private final OrderService orderService;
    private final EmployeeService employeeService;
    private final ClientService clientService;
    private final ApplianceService applianceService;

    @GetMapping("")
    public String orders(@RequestParam(defaultValue = "false") boolean cancelled,
                         @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        Page<OrderResponseDTO> orders = cancelled ? orderService.findCancelled(pageable) : orderService.findAll(pageable);
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", orders.getNumber());
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageSize", orders.getSize());
        model.addAttribute("sortField", order.getProperty());
        model.addAttribute("sortDir", order.getDirection().name().toLowerCase());
        model.addAttribute("cancelled", cancelled);
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
        model.addAttribute("orderInfo", orderService.findResponseById(id));
        model.addAttribute("orderId", id);
        model.addAttribute("rows", orderService.getOrderRows(id));
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

    @DeleteMapping("/{id}")
    public String deleteOrder(@PathVariable Long id) {
        orderService.deleteOrderById(id);
        return "redirect:/orders";
    }

    @PatchMapping("/{id}/employee-approve")
    public String approveByEmployee(@PathVariable Long id,
                                    @RequestParam(required = false) String note,
                                    Authentication authentication) {
        orderService.approveByEmployee(id, note, authentication.getName());
        return "redirect:/orders";
    }

    @PatchMapping("/{id}/employee-revision")
    public String requestRevision(@PathVariable Long id,
                                  @RequestParam String note,
                                  Authentication authentication) {
        orderService.requestRevision(id, note, authentication.getName());
        return "redirect:/orders";
    }

    @GetMapping("/{id}/cancel")
    public String cancelOrderForm(@PathVariable Long id, Model model) {
        model.addAttribute("orderInfo", orderService.findResponseById(id));
        model.addAttribute("orderId", id);
        return "order/cancelOrder";
    }

    @PatchMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id,
                              @RequestParam String reason,
                              Authentication authentication) {
        orderService.cancelOrder(id, reason, authentication.getName());
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
                               @RequestParam Long numbers) {
        orderService.addRowToOrder(ordersId, applianceId, numbers);
        return "redirect:/orders/" + ordersId + "/edit";
    }

    @DeleteMapping("/{rowId}/row")
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
