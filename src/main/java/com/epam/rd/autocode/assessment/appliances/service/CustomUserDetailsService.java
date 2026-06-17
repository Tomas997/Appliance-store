package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Deliverer;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.ClientRepository;
import com.epam.rd.autocode.assessment.appliances.repository.DelivererRepository;
import com.epam.rd.autocode.assessment.appliances.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;
    private final ClientRepository clientRepository;
    private final DelivererRepository delivererRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Employee> emp = employeeRepository.findByEmail(email);
        if (emp.isPresent()) {
            return new User(emp.get().getEmail(), emp.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
        }

        Optional<Client> client = clientRepository.findByEmail(email);
        if (client.isPresent()) {
            return new User(client.get().getEmail(), client.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        }

        Optional<Deliverer> deliverer = delivererRepository.findByEmail(email);
        if (deliverer.isPresent()) {
            return new User(deliverer.get().getEmail(), deliverer.get().getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_DELIVERER")));
        }

        throw new UsernameNotFoundException("No user found with email: " + email);
    }
}
