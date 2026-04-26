import { HttpInterceptorFn } from '@angular/common/http';
import { from, switchMap } from 'rxjs';
import { keycloak } from '../services/keycloak-init';
import { environment } from '../../../environments/environment';


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Solo intercepta peticiones a la API propia — deja pasar el resto
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  // Si no está autenticado, simplemente pasa la petición (ej. login, registro)
  if (!keycloak.authenticated) {
    return next(req);
  }

  // Renueva el token si quedan menos de 30 segundos de vida
  return from(keycloak.updateToken(30)).pipe(
    switchMap(() => {
      const token = keycloak.token;

      if (!token) return next(req);

      const authReq = req.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      return next(authReq);
    })
  );
};
