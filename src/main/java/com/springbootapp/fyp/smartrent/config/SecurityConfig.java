package com.springbootapp.fyp.smartrent.config;

import com.springbootapp.fyp.smartrent.security.JwtAuthFilter;
import com.springbootapp.fyp.smartrent.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        // Allow page routes and static files
                        .requestMatchers(
                                "/", "/home", "/login", "/register", "/verify-otp",
                                "/vehicle-rentals", "/about", "/contact", "/profile",
                                "/booking",
                                "/vendor/dashboard", "/admin/dashboard",
                                "/home.html", "/login.html", "/register.html",
                                "/css/**", "/js/**", "/images/**", "/static/**",
                                // eSewa callback URLs (public — eSewa redirects here)
                                "/payment/success", "/payment/failure"
                        ).permitAll()
                        // Allow auth APIs (includes /api/auth/register-vendor)
                        .requestMatchers("/api/auth/**").permitAll()
                        // Public read-only APIs for home page
                        .requestMatchers("/api/vehicles/**").permitAll()
                        .requestMatchers("/api/brands/**").permitAll()
                        .requestMatchers("/api/reviews/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        // Uploaded files (vehicle images)
                        .requestMatchers("/uploads/**").permitAll()
                        // Role based
                        .requestMatchers("/api/payment/esewa/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                        .requestMatchers("/api/vendor/**").hasRole("VENDOR")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Vendor approval actions are admin-only
                        .requestMatchers("/api/admin/vendors/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}