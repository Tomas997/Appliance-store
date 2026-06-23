package com.epam.rd.autocode.assessment.appliances.controller;

import com.epam.rd.autocode.assessment.appliances.dto.ClientRequestDTO;
import com.epam.rd.autocode.assessment.appliances.exception.EmailAlreadyInUseException;
import com.epam.rd.autocode.assessment.appliances.service.ClientService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@AllArgsConstructor
@Controller
public class RegistrationController {
    private final ClientService clientService;

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("client", new ClientRequestDTO());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("client") ClientRequestDTO client, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            clientService.saveClient(client);
        } catch (EmailAlreadyInUseException e) {
            result.addError(new FieldError("client", "email", null, false,
                    new String[]{"error.registration.failed"}, null, null));
            return "register";
        }
        return "redirect:/login?registered=true";
    }
}
