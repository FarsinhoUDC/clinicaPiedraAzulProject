import { HttpInterceptorFn } from '@angular/common/http';
import { from, switchMap } from 'rxjs';
import { keycloak } from '../services/keycloak-init';
import { environment } from '../../../environments/environment';


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Solo intercepta peticiones a la API propia — deja pasar el resto
  if (!req.url.startsWith(environment.apiBaseUrl)) {
    return next(req);
  }

  // Renueva el token si quedan menos de 30 segundos de vida
  return from(keycloak.updateToken(30)).pipe(
    switchMap(() => {
      const token = keycloak.token;

      // Si no hay token (usuario no autenticado), deja pasar la petición
      // y el backend responderá 401 si la ruta lo requiere.
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
