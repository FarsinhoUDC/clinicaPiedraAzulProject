package com.piedrazul.shared;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convierte los roles del JWT de Keycloak
 * en GrantedAuthority con prefijo ROLE_ para Spring Security.
 *
 * Busca los roles primero en realm_access.roles y, si no están presentes,
 * los busca en el claim plano "roles".
 *
 * Spring Security requiere el prefijo "ROLE_" para que hasRole("MEDICO")
 * funcione correctamente en el SecurityFilterChain y en @PreAuthorize.
 */
public class KeycloakRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        List<String> roles = null;

        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles = (List<String>) realmAccess.get("roles");
        } else {
            roles = jwt.getClaimAsStringList("roles");
        }

        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
