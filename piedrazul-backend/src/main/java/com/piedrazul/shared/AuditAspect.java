package com.piedrazul.shared;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Aspecto de auditoría que intercepta todos los endpoints anotados con
 * {@code @PreAuthorize} (y cualquier método en controladores REST)
 * para extraer el {@code sub} del JWT de Keycloak e inyectarlo en el MDC
 * de SLF4J, permitiendo que todos los logs del request incluyan el ID
 * único del usuario autenticado.
 *
 * <p><b>Trazabilidad:</b> el {@code sub} es el identificador inmutable del
 * usuario en Keycloak (UUID). No expone información sensible como el
 * número de documento.</p>
 *
 * <p>Uso en logback.xml / application.properties:</p>
 * <pre>
 *   logging.pattern.console=%d [%X{userId}] %-5level %logger{36} - %msg%n
 * </pre>
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);

    /**
     * Se ejecuta ANTES de cualquier método en clases anotadas con
     * {@code @RestController} o {@code @Controller}.
     */
    @Before("within(@org.springframework.web.bind.annotation.RestController *) ||" +
            "within(@org.springframework.stereotype.Controller *)")
    public void populateMdcWithUserId(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = (Jwt) jwtAuth.getPrincipal();

            String sub           = jwt.getSubject();                          // UUID Keycloak
            String username      = jwt.getClaimAsString("preferred_username"); // Nro. documento
            String preferredName = jwt.getClaimAsString("name");               // Nombre completo

            // Inyectar en MDC para que aparezcan en TODOS los logs del request
            MDC.put("userId",   sub      != null ? sub      : "anonymous");
            MDC.put("username", username != null ? username : "unknown");

            log.debug("AUDIT | usuario={} | sub={} | nombre={} | endpoint={}#{}",
                    username,
                    sub,
                    preferredName,
                    joinPoint.getTarget().getClass().getSimpleName(),
                    joinPoint.getSignature().getName());
        } else {
            // Petición sin autenticación JWT (rutas públicas)
            MDC.put("userId",   "anonymous");
            MDC.put("username", "anonymous");
        }
    }
}
