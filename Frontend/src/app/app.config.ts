import { ApplicationConfig, APP_INITIALIZER } from '@angular/core';
import {
  provideHttpClient,
  withInterceptors
} from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { initializeKeycloak } from './core/services/keycloak-init';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Inicializa Keycloak ANTES de montar la aplicación.
    // Si Keycloak no está disponible, la app no arrancará.
    {
      provide: APP_INITIALIZER,
      useFactory: initializeKeycloak,
      multi: true
    },
    // HTTP Client con interceptor JWT automático
    provideHttpClient(withInterceptors([authInterceptor])),
    provideRouter(routes)
  ]
};
