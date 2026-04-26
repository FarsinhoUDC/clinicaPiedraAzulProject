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

/**
 * Configura Spring Security como OAuth2 Resource Server.
 * Valida JWTs emitidos por Keycloak (Piedrazul-Realm).
 *
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // Configura el converter que extrae roles del claim "roles" del JWT
        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
            // Sin estado de sesión HTTP (JWT stateless)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // CORS manejado por CorsConfig.java existente
            .cors(cors -> {})

            // CSRF deshabilitado (API REST stateless con Bearer tokens)
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth
                // ── Endpoints públicos ─────────────────────────────────────
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Registro de pacientes: cualquiera puede registrarse
                .requestMatchers(HttpMethod.POST, "/api/pacientes").permitAll()

                // ── Reportes CSV — EXCLUSIVO MEDICO ───────────────────────
                .requestMatchers("/api/reportes/**").hasRole("MEDICO")

                // ── Configuración del sistema ──────────────────────────────
                // Escritura (POST/PUT/DELETE): solo ADMIN
                .requestMatchers(HttpMethod.POST,   "/api/configuracion/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/configuracion/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/configuracion/**").hasRole("ADMIN")
                // Lectura (GET): ADMIN y PACIENTE (necesario para ventana de citas y disponibilidad)
                .requestMatchers(HttpMethod.GET, "/api/configuracion/**")
                    .hasAnyRole("ADMIN", "AGENDADOR", "MEDICO", "PACIENTE")

                // ── Citas ──────────────────────────────────────────────────
                // PACIENTE puede crear y consultar sus propias citas
                .requestMatchers("/api/citas/**")
                    .hasAnyRole("AGENDADOR", "MEDICO", "ADMIN", "PACIENTE")

                // ── Médicos (lectura) — necesario para el agendamiento ─────
                // PACIENTE necesita ver la lista de médicos para elegir
                .requestMatchers(HttpMethod.GET, "/api/medicos/**")
                    .hasAnyRole("AGENDADOR", "MEDICO", "ADMIN", "PACIENTE")
                // Escritura sobre médicos: solo roles de gestión
                .requestMatchers(HttpMethod.POST,   "/api/medicos/**").hasAnyRole("AGENDADOR", "MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/medicos/**").hasAnyRole("AGENDADOR", "MEDICO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/medicos/**").hasAnyRole("AGENDADOR", "MEDICO", "ADMIN")

                // ── Pacientes (lectura) — cualquier rol autenticado ────────
                .requestMatchers(HttpMethod.GET, "/api/pacientes/**")
                    .hasAnyRole("AGENDADOR", "MEDICO", "ADMIN", "PACIENTE")

                // Cualquier otra ruta: requiere autenticación
                .anyRequest().authenticated()
            )

            // Configurar como Resource Server: valida JWT con Keycloak JWK Set
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(jwtConverter)));

        // Permitir frames de H2 Console (desarrollo)
        http.headers(headers ->
            headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
