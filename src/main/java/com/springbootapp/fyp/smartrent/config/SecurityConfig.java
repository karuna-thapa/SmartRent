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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

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
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                                "/payment/success", "/payment/failure",
                                "/favicon.ico"
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
                        .requestMatchers("/api/payment/esewa/**").hasAnyRole("CUSTOMER", "customer")
                        .requestMatchers("/api/customer/**").hasAnyRole("CUSTOMER", "customer")
                        .requestMatchers("/api/vendor/**").hasAnyRole("VENDOR", "vendor")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "admin")
                        // Vendor approval actions are admin-only
                        .requestMatchers("/api/admin/vendors/**").hasAnyRole("ADMIN", "admin")
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
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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