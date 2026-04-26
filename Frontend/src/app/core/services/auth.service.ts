import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { keycloak } from './keycloak-init';

export type AppRole = 'ADMIN' | 'AGENDADOR' | 'MEDICO' | 'PACIENTE';


@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private readonly router: Router) {}

  /** Retorna true si hay una sesión Keycloak activa con token válido. */
  isAuthenticated(): boolean {
    return !!keycloak.authenticated;
  }

  /**
   * Retorna el rol principal del usuario según el claim "roles" del JWT.
   * Prioridad: ADMIN > MEDICO > AGENDADOR > PACIENTE
   */
  getRole(): AppRole | null {
    // Keycloak emite los roles en realm_access.roles, NO en un claim 'roles' plano
    const realmAccess = keycloak.tokenParsed?.['realm_access'] as { roles?: string[] } | undefined;
    const roles = realmAccess?.roles;
    if (!roles) return null;

    if (roles.includes('ADMIN'))     return 'ADMIN';
    if (roles.includes('MEDICO'))    return 'MEDICO';
    if (roles.includes('AGENDADOR')) return 'AGENDADOR';
    if (roles.includes('PACIENTE'))  return 'PACIENTE';
    return null;
  }

  /** Verifica si el usuario tiene un rol específico. */
  hasRole(role: AppRole): boolean {
    const realmAccess = keycloak.tokenParsed?.['realm_access'] as { roles?: string[] } | undefined;
    return realmAccess?.roles?.includes(role) ?? false;
  }

  /**
   * Retorna el número de identificación del usuario.
   * (Mapeado en Keycloak como preferred_username = numeroDocumento)
   */
  getUsername(): string {
    return keycloak.tokenParsed?.['preferred_username'] ?? '';
  }

  /** Nombre completo del usuario desde el token (claim 'name'). */
  getFullName(): string {
    return keycloak.tokenParsed?.['name'] ?? '';
  }

  /**
   * Primer nombre / nombres del usuario (claim 'given_name' de Keycloak).
   * Corresponde al campo "First Name" en la consola de Keycloak.
   */
  getFirstName(): string {
    return keycloak.tokenParsed?.['given_name'] ?? '';
  }

  /**
   * Apellidos del usuario (claim 'family_name' de Keycloak).
   * Corresponde al campo "Last Name" en la consola de Keycloak.
   */
  getLastName(): string {
    return keycloak.tokenParsed?.['family_name'] ?? '';
  }

  /** Token de acceso JWT actual. Usar en interceptores HTTP. */
  getToken(): string | undefined {
    return keycloak.token;
  }

    /** ID único del usuario (claim 'sub' en Keycloak). */
  getUserId(): string {
    return keycloak.tokenParsed?.['sub'] ?? '';
  }

  /** Redirige al login de Keycloak (en español). */
  login(): void {
    keycloak.login({ locale: 'es' });
  }

  /** Cierra la sesión en Keycloak y redirige al inicio de la app. */
  logout(): void {
    keycloak.logout({ redirectUri: window.location.origin });
  }

  /**
   * Redirige al usuario a su interfaz correspondiente según el rol.
   * Debe llamarse después del login exitoso desde el componente raíz.
   */
  redirectToRoleHome(): void {
    const role = this.getRole();
    switch (role) {
      case 'ADMIN':
        this.router.navigate(['/admin/disponibilidad']);
        break;
      case 'MEDICO':
        this.router.navigate(['/medico/reportes']);
        break;
      case 'AGENDADOR':
        this.router.navigate(['/agendador/consulta']);
        break;
      case 'PACIENTE':
        this.router.navigate(['/paciente/portal']);
        break;
      default:
        this.router.navigate(['/inicio']);
    }
  }
}
