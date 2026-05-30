package com.piedrazul.shared;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // Preflight CORS
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Registro de pacientes: cualquiera puede registrarse
                .requestMatchers(HttpMethod.POST, "/api/pacientes").permitAll()
                // Iniciar sesión localmente (público)
                .requestMatchers(HttpMethod.POST, "/api/sesion/login").permitAll()

                // Health check para Railway (verifica que el contenedor está vivo)
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                // Reportes CSV — EXCLUSIVO MEDICO
                .requestMatchers("/api/reportes/**").hasRole("MEDICO")

                // Configuración del sistema
                .requestMatchers(HttpMethod.POST,   "/api/configuracion/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/configuracion/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/configuracion/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,    "/api/configuracion/**")
                    .hasAnyRole("ADMIN", "AGENDADOR", "MEDICO", "PACIENTE")

                // Citas
                .requestMatchers("/api/citas/**")
                    .hasAnyRole("AGENDADOR", "MEDICO", "ADMIN", "PACIENTE")

                // Médicos (GET público para landing page)
                .requestMatchers(HttpMethod.GET, "/api/medicos/**").permitAll()
                .requestMatchers(HttpMethod.POST,   "/api/medicos/**").hasAnyRole("AGENDADOR", "MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/medicos/**").hasAnyRole("AGENDADOR", "MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/medicos/**").hasAnyRole("AGENDADOR", "MEDICO", "ADMIN")

                // Agendadores — solo ADMIN puede crear
                .requestMatchers(HttpMethod.POST, "/api/agendadores/**").hasRole("ADMIN")

                // Pacientes (lectura) — cualquier rol autenticado
                .requestMatchers(HttpMethod.GET, "/api/pacientes/**")
                    .hasAnyRole("AGENDADOR", "MEDICO", "ADMIN", "PACIENTE")

                .anyRequest().authenticated()
            )

            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtConverter)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}