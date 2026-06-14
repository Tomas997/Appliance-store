package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ClientRequestDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientResponseDTO;
import com.epam.rd.autocode.assessment.appliances.dto.ClientUpdateDTO;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Controller
@RequestMapping("/clients")
public class ClientController {
    private ClientService clientService;

    @GetMapping("")
    public String clients(@PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable, Model model) {
        Sort.Order order = pageable.getSort().iterator().next();
        String sortField = order.getProperty();
        String sortDir = order.getDirection().name().toLowerCase();

        Page<ClientResponseDTO> clients = clientService.findAll(pageable);
        model.addAttribute("clients", clients.getContent());
        model.addAttribute("currentPage", clients.getNumber());
        model.addAttribute("totalPages", clients.getTotalPages());
        model.addAttribute("pageSize", clients.getSize());
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        return "client/clients";
    }

    @GetMapping("/add")
    public String addClients(Model model, ClientRequestDTO client) {
        model.addAttribute("client", client);
        return "client/newClient";
    }

    @PostMapping("/add")
    public String addClient(@Valid @ModelAttribute("client") ClientRequestDTO client, BindingResult result) {
        if (result.hasErrors()) {
            return "client/newClient";
        }
        clientService.saveClient(client);
        return "redirect:/clients";
    }

    @GetMapping("/{id}/edit")
    public String editClientForm(@PathVariable Long id, Model model) {
        model.addAttribute("client", clientService.findById(id));
        model.addAttribute("clientId", id);
        return "client/editClient";
    }

    @PutMapping("/{id}/edit")
    public String editClient(@PathVariable Long id,
                             @Valid @ModelAttribute("client") ClientUpdateDTO client,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("clientId", id);
            return "client/editClient";
        }
        clientService.updateClient(id, client);
        return "redirect:/clients";
    }

    @DeleteMapping("/{clientId}")
    public String deleteClient(@PathVariable Long clientId) {
        clientService.deleteClientById(clientId);
        return "redirect:/clients";
    }
}
