import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

const SESSION_KEY = 'piedrazul_user';

function getRole(): string | null {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw);
    return parsed?.rol?.toUpperCase() ?? null;
  } catch {
    return null;
  }
}

function isAuthenticated(): boolean {
  const raw = localStorage.getItem(SESSION_KEY);
  if (!raw) return false;
  try {
    const parsed = JSON.parse(raw);
    return !!(parsed?.id && parsed?.activo === true);
  } catch {
    return false;
  }
}

/** Protege rutas que requieren sesión activa (cualquier rol). */
export const authGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (isAuthenticated()) return true;
  return router.parseUrl('/inicio');
};

/**
 * Protege rutas exclusivas del agendador (rol MEDICO).
 * Un paciente no puede acceder a la búsqueda o creación de citas del agendador.
 */
export const agendadorGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (!isAuthenticated()) return router.parseUrl('/inicio');
  const rol = getRole();
  if (rol === 'MEDICO' || rol === 'AGENDADOR') return true;
  // Paciente autenticado → redirige a su portal
  if (rol === 'PACIENTE') return router.parseUrl('/paciente/portal');
  return router.parseUrl('/inicio');
};

/**
 * Protege rutas exclusivas del administrador (rol ADMIN).
 * Ni el agendador ni el paciente pueden acceder a configuración.
 */
export const adminGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (!isAuthenticated()) return router.parseUrl('/inicio');
  const rol = getRole();
  if (rol === 'ADMIN') return true;
  if (rol === 'PACIENTE') return router.parseUrl('/paciente/portal');
  // MEDICO/AGENDADOR → redirige a su vista
  return router.parseUrl('/agendador/consulta');
};

/**
 * Protege rutas exclusivas del paciente (rol PACIENTE).
 * Un agendador no puede acceder al portal del paciente.
 */
export const pacienteGuard: CanActivateFn = () => {
  const router = inject(Router);
  if (!isAuthenticated()) return router.parseUrl('/inicio');
  const rol = getRole();
  if (rol === 'PACIENTE') return true;
  if (rol === 'MEDICO' || rol === 'AGENDADOR') return router.parseUrl('/agendador/consulta');
  return router.parseUrl('/inicio');
};
