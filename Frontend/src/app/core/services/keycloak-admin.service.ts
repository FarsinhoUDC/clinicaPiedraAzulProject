import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { keycloak } from './keycloak-init';

// ─── Modelos de la Admin REST API de Keycloak ────────────────────────────────

export interface KeycloakUser {
  id: string;
  username: string;         // Número de documento
  firstName?: string;
  lastName?: string;
  email?: string;
  enabled: boolean;
  createdTimestamp?: number;
}

export interface KeycloakRole {
  id: string;
  name: string;
  description?: string;
  composite?: boolean;
  clientRole?: boolean;
}

export interface CreateUserPayload {
  username: string;         // Número de documento
  firstName: string;
  lastName: string;
  email?: string;
  enabled: boolean;
  credentials: Array<{
    type: 'password';
    value: string;
    temporary: boolean;
  }>;
}


@Injectable({ providedIn: 'root' })
export class KeycloakAdminService {

  private readonly adminBaseUrl: string;

  constructor(private readonly http: HttpClient) {
    const { url, realm } = environment.keycloak;
    this.adminBaseUrl = `${url}/admin/realms/${realm}`;
  }

  /** Cabeceras con el Bearer token del admin actual */
  private get headers(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${keycloak.token ?? ''}`,
      'Content-Type':  'application/json'
    });
  }


  /**
   * Lista todos los usuarios del realm.
   * @param search Filtro opcional por nombre de usuario, nombre o email.
   * @param max    Máximo de resultados (default 100).
   */
  listarUsuarios(search = '', max = 100): Observable<KeycloakUser[]> {
    const params = search
      ? `?search=${encodeURIComponent(search)}&max=${max}`
      : `?max=${max}`;
    return this.http.get<KeycloakUser[]>(
      `${this.adminBaseUrl}/users${params}`,
      { headers: this.headers }
    );
  }

  /**
   * Crea un nuevo usuario en Keycloak.
   * El `username` debe ser el número de documento del paciente/médico/etc.
   * La contraseña inicial es también el número de documento (temporary: false).
   */
  crearUsuario(payload: CreateUserPayload): Observable<void> {
    return this.http.post<void>(
      `${this.adminBaseUrl}/users`,
      payload,
      { headers: this.headers }
    );
  }

  /**
   * Elimina un usuario por su ID de Keycloak.
   */
  eliminarUsuario(userId: string): Observable<void> {
    return this.http.delete<void>(
      `${this.adminBaseUrl}/users/${userId}`,
      { headers: this.headers }
    );
  }

  /**
   * Habilita o deshabilita un usuario.
   */
  toggleUsuario(userId: string, enabled: boolean): Observable<void> {
    return this.http.put<void>(
      `${this.adminBaseUrl}/users/${userId}`,
      { enabled },
      { headers: this.headers }
    );
  }

  // ─── Roles de Realm ────────────────────────────────────────────────────────

  /**
   * Lista todos los roles disponibles en el realm.
   */
  listarRoles(): Observable<KeycloakRole[]> {
    return this.http.get<KeycloakRole[]>(
      `${this.adminBaseUrl}/roles`,
      { headers: this.headers }
    );
  }

  /**
   * Obtiene los roles asignados actualmente a un usuario.
   */
  obtenerRolesDeUsuario(userId: string): Observable<KeycloakRole[]> {
    return this.http.get<KeycloakRole[]>(
      `${this.adminBaseUrl}/users/${userId}/role-mappings/realm`,
      { headers: this.headers }
    );
  }

  /**
   * Asigna uno o más roles de realm a un usuario.
   */
  asignarRol(userId: string, roles: KeycloakRole[]): Observable<void> {
    return this.http.post<void>(
      `${this.adminBaseUrl}/users/${userId}/role-mappings/realm`,
      roles,
      { headers: this.headers }
    );
  }

  /**
   * Revoca uno o más roles de realm de un usuario.
   */
  revocarRol(userId: string, roles: KeycloakRole[]): Observable<void> {
    return this.http.delete<void>(
      `${this.adminBaseUrl}/users/${userId}/role-mappings/realm`,
      { headers: this.headers, body: roles }
    );
  }
}
