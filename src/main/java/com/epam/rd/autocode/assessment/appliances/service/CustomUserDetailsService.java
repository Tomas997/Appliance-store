package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Deliverer;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.model.User;
import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));

        String role = roleOf(user);
        return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(),
                List.of(new SimpleGrantedAuthority(role)));
    }

    private String roleOf(User user) {
        if (user instanceof Employee) {
            return "ROLE_EMPLOYEE";
        }
        if (user instanceof Client) {
            return "ROLE_CLIENT";
        }
        if (user instanceof Deliverer) {
            return "ROLE_DELIVERER";
        }
        throw new UsernameNotFoundException("No role mapped for user: " + user.getEmail());
    }
}
