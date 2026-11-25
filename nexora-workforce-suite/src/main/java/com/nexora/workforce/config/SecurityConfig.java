package com.nexora.workforce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.workforce.repository.EmployeeRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${nexora.security.username}") String username,
            @Value("${nexora.security.password}") String password,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder) {
        String encodedAdminPassword = passwordEncoder.encode(password);
        return requestedUsername -> {
            if (username.equals(requestedUsername)) {
                return User.withUsername(username)
                        .password(encodedAdminPassword)
                        .roles("ADMIN")
                        .build();
            }

            return employeeRepository
                    .findByEmailIgnoreCaseAndPasswordHashIsNotNull(requestedUsername)
                    .map(employee -> User.withUsername(employee.getEmail())
                            .password(employee.getPasswordHash())
                            .roles("EMPLOYEE")
                            .build())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "No account found for username: " + requestedUsername));
        };
    }

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectMapper objectMapper) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/me",
                                "/api/employees/count",
                                "/api/employees/me")
                        .hasAnyRole("ADMIN", "EMPLOYEE")
                        .anyRequest().hasRole("ADMIN"))
                .httpBasic(basic ->
                        basic.authenticationEntryPoint(apiAuthenticationEntryPoint(objectMapper)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(apiAuthenticationEntryPoint(objectMapper)));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/signin.html", "/signin.js", "/style.css",
                                "/favicon.ico", "/csrf")
                        .permitAll()
                        .requestMatchers("/employee-registration.html")
                        .hasRole("ADMIN")
                        .requestMatchers("/", "/index.html", "/script.js")
                        .authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/signin.html")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/signin.html?error"))
                .logout(logout -> logout
                        .logoutSuccessUrl("/signin.html?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));

        return http.build();
    }

    private AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(401);
            response.setContentType("application/json");
            response.setHeader("WWW-Authenticate", "Basic");
            objectMapper.writeValue(response.getOutputStream(),
                    Map.of("error", "Authentication required"));
        };
    }
}
