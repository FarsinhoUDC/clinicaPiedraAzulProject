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

// Registrar eventos de Keycloak para mantener automáticamente sincronizado el localStorage
keycloak.onAuthSuccess = () => {
  if (keycloak.token) localStorage.setItem('kc_token', keycloak.token);
  if (keycloak.refreshToken) localStorage.setItem('kc_refreshToken', keycloak.refreshToken);
  if (keycloak.idToken) localStorage.setItem('kc_idToken', keycloak.idToken);
};

keycloak.onAuthRefreshSuccess = () => {
  if (keycloak.token) localStorage.setItem('kc_token', keycloak.token);
  if (keycloak.refreshToken) localStorage.setItem('kc_refreshToken', keycloak.refreshToken);
  if (keycloak.idToken) localStorage.setItem('kc_idToken', keycloak.idToken);
};

keycloak.onAuthRefreshError = () => {
  clearStoredTokens();
};

keycloak.onAuthLogout = () => {
  clearStoredTokens();
};

keycloak.onTokenExpired = () => {
  // El interceptor se encarga de actualizar el token al detectar que expiró,
  // pero si falla, se limpiará en onAuthRefreshError.
};

function clearStoredTokens(): void {
  localStorage.removeItem('kc_token');
  localStorage.removeItem('kc_refreshToken');
  localStorage.removeItem('kc_idToken');
  localStorage.removeItem('piedrazul_user');
}

/**
 * Función de inicialización que ejecuta Angular al arrancar.
 * Carga tokens de localStorage si están presentes para restaurar sesión,
 * de lo contrario realiza check-sso clásico.
 */
export function initializeKeycloak(): () => Promise<boolean> {
  return () => {
    const token = localStorage.getItem('kc_token');
    const refreshToken = localStorage.getItem('kc_refreshToken');
    const idToken = localStorage.getItem('kc_idToken');

    const options: any = {
      onLoad: 'check-sso',
      // silentCheckSsoRedirectUri comentado para evitar bloqueo de iframe en local
      // silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
      pkceMethod: 'S256',
      checkLoginIframe: false
    };

    // Si existen tokens locales válidos, los inyectamos en la inicialización
    if (token && token !== 'undefined' && token !== 'null' &&
        refreshToken && refreshToken !== 'undefined' && refreshToken !== 'null') {
      options.token = token;
      options.refreshToken = refreshToken;
      if (idToken && idToken !== 'undefined' && idToken !== 'null') {
        options.idToken = idToken;
      }
    }

    return keycloak.init(options).catch(err => {
      console.warn('Error inicializando Keycloak con tokens locales guardados. Limpiando sesión...', err);
      clearStoredTokens();
      window.location.reload();
      return new Promise<boolean>(() => {});
    });
  };
}
