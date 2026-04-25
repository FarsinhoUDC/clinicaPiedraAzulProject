import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { keycloak } from '../services/keycloak-init';

/**
 * Protege rutas que requieren sesión activa (cualquier rol).
 * Redirige al login de Keycloak si no hay token.
 */
export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (auth.isAuthenticated()) return true;

  keycloak.login({ locale: 'es' });
  return false;
};

/**
 * Protege rutas de AGENDADOR (y permite acceso a MEDICO y ADMIN).
 * Un PACIENTE es redirigido a su portal propio.
 */
export const agendadorGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    keycloak.login({ locale: 'es' });
    return false;
  }

  const rol = auth.getRole();
  if (rol === 'MEDICO' || rol === 'AGENDADOR' || rol === 'ADMIN') return true;
  if (rol === 'PACIENTE') return router.parseUrl('/paciente/portal');
  return router.parseUrl('/inicio');
};

/**
 * Protege rutas exclusivas del ADMIN (configuración del sistema).
 * PACIENTE → portal. MEDICO/AGENDADOR → consulta de citas.
 */
export const adminGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    keycloak.login({ locale: 'es' });
    return false;
  }

  const rol = auth.getRole();
  if (rol === 'ADMIN')    return true;
  if (rol === 'PACIENTE') return router.parseUrl('/paciente/portal');
  // MEDICO/AGENDADOR → su vista de citas
  return router.parseUrl('/agendador/consulta');
};

/**
 * Protege rutas exclusivas del PACIENTE (portal personal, mis citas).
 * MEDICO/AGENDADOR son redirigidos a la vista de agendamiento.
 */
export const pacienteGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    keycloak.login({ locale: 'es' });
    return false;
  }

  const rol = auth.getRole();
  if (rol === 'PACIENTE') return true;
  if (rol === 'MEDICO' || rol === 'AGENDADOR') return router.parseUrl('/agendador/consulta');
  return router.parseUrl('/inicio');
};

/**
 * Protege la ruta de exportación CSV — EXCLUSIVO MEDICO.
 * Cualquier otro rol autenticado es redirigido al inicio.
 */
export const medicoGuard: CanActivateFn = () => {
  const auth   = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    keycloak.login({ locale: 'es' });
    return false;
  }

  const rol = auth.getRole();
  if (rol === 'MEDICO') return true;
  if (rol === 'ADMIN')  return router.parseUrl('/admin/disponibilidad');
  if (rol === 'PACIENTE') return router.parseUrl('/paciente/portal');
  return router.parseUrl('/agendador/consulta');
};

/**
 * Redirige a los usuarios a su ruta principal correspondiente según su rol.
 * Si no está logueado, permite el acceso (para ver la página de inicio pública).
 */
export const roleRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    // Si no está logueado, lo dejamos ver el componente Dashboard/Inicio público
    return true; 
  }

  // Si está logueado, lo mandamos a SU pantalla para evitar la "pantalla blanca"
  const rol = auth.getRole();
  switch (rol) {
    case 'MEDICO':
    case 'AGENDADOR':
      return router.parseUrl('/agendador/consulta');
    case 'ADMIN':
      return router.parseUrl('/admin/disponibilidad');
    case 'PACIENTE':
      return router.parseUrl('/paciente/portal');
    default:
      return true; // Fallback
  }
};
