import Keycloak from 'keycloak-js';
import { environment } from '../../../environments/environment';

/**
 * Instancia singleton de Keycloak.
 * Se registra como APP_INITIALIZER en app.config.ts para garantizar
 * que la autenticación se inicialice ANTES de montar cualquier componente.
 */
export const keycloak = new Keycloak({
  url:      environment.keycloak.url,
  realm:    environment.keycloak.realm,
  clientId: environment.keycloak.clientId
});

/**
 * Función de inicialización que ejecuta Angular al arrancar.
 *
 * onLoad: 'check-sso'
 *   → Comprueba silenciosamente si ya hay una sesión SSO activa.
 *   → NO fuerza el login al cargar la página (el guard lo gestiona).
 *
 * silentCheckSsoRedirectUri
 *   → Apunta a un HTML mínimo en /assets/ que postMessage al padre.
 *   → Permite renovar la sesión sin redirigir al usuario.
 *
 * pkceMethod: 'S256'
 *   → Authorization Code Flow con PKCE — el flujo más seguro para SPAs.
 *
 * checkLoginIframe: false
 *   → Desactiva el iframe de comprobación periódica (causa problemas de CORS
 *     con algunos navegadores/configuraciones de Keycloak).
 */
export function initializeKeycloak(): () => Promise<boolean> {
  return () =>
    keycloak.init({
      onLoad: 'check-sso',
      silentCheckSsoRedirectUri:
        window.location.origin + '/assets/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: false
    });
}
