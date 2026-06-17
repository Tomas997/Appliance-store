package com.epam.rd.autocode.assessment.appliances.config;

import com.epam.rd.autocode.assessment.appliances.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        UserDetails admin = User.builder()
                .username("admin@store.com")
                .password(passwordEncoder().encode("AdminPass1"))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider dbProvider = new DaoAuthenticationProvider();
        dbProvider.setUserDetailsService(customUserDetailsService);
        dbProvider.setPasswordEncoder(passwordEncoder());

        DaoAuthenticationProvider inMemoryProvider = new DaoAuthenticationProvider();
        inMemoryProvider.setUserDetailsService(inMemoryUserDetailsManager());
        inMemoryProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(List.of(dbProvider, inMemoryProvider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/error").permitAll()
                .requestMatchers("/css/**", "/js/**").permitAll()
                .requestMatchers("/h2-console/**").hasRole("ADMIN")
                .requestMatchers("/appliances/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/manufacturers/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/orders/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers(HttpMethod.GET, "/clients/**").hasAnyRole("ADMIN", "EMPLOYEE")
                .requestMatchers("/clients/**").hasRole("ADMIN")
                .requestMatchers("/employees/**").hasRole("ADMIN")
                .requestMatchers("/deliverers/**").hasRole("ADMIN")
                .requestMatchers("/my-orders/**").hasRole("CLIENT")
                .requestMatchers("/deliveries/**").hasRole("DELIVERER")
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}
