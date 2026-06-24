package com.epam.rd.autocode.assessment.appliances.service;

import com.epam.rd.autocode.assessment.appliances.model.Client;
import com.epam.rd.autocode.assessment.appliances.model.Deliverer;
import com.epam.rd.autocode.assessment.appliances.model.Employee;
import com.epam.rd.autocode.assessment.appliances.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("loadUserByUsername: якщо знайдено працівника — повинен повернути UserDetails з ROLE_EMPLOYEE")
    void loadUserByUsername_whenEmployeeFound_shouldReturnUserDetailsWithEmployeeRole() {
        Employee employee = new Employee(1L, "Andrii", "andrii@kpi.ua", "encodedPassword", "IT");

        when(userRepository.findByEmail("andrii@kpi.ua")).thenReturn(Optional.of(employee));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("andrii@kpi.ua");

        assertThat(userDetails.getUsername()).isEqualTo("andrii@kpi.ua");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_EMPLOYEE");
    }

    @Test
    @DisplayName("loadUserByUsername: якщо знайдено клієнта — повинен повернути UserDetails з ROLE_CLIENT")
    void loadUserByUsername_whenClientFound_shouldReturnUserDetailsWithClientRole() {
        Client client = new Client(2L, "Olena", "olena@test.com", "encodedPassword", "1234");

        when(userRepository.findByEmail("olena@test.com")).thenReturn(Optional.of(client));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("olena@test.com");

        assertThat(userDetails.getUsername()).isEqualTo("olena@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_CLIENT");
    }

    @Test
    @DisplayName("loadUserByUsername: якщо знайдено доставщика — повинен повернути UserDetails з ROLE_DELIVERER")
    void loadUserByUsername_whenDelivererFound_shouldReturnUserDetailsWithDelivererRole() {
        Deliverer deliverer = new Deliverer(3L, "Petro", "petro@test.com", "encodedPassword");

        when(userRepository.findByEmail("petro@test.com")).thenReturn(Optional.of(deliverer));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("petro@test.com");

        assertThat(userDetails.getUsername()).isEqualTo("petro@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_DELIVERER");
    }

    @Test
    @DisplayName("loadUserByUsername: якщо користувача не знайдено — повинен кинути UsernameNotFoundException")
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("unknown@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown@test.com");
    }
}
