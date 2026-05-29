import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';
import { LoginRequest, LoginResponse } from '../../../core/models/login.model';

@Component({
  selector: 'organismo-login-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login-form.organism.html',
  styleUrls: ['./login-form.organism.css']
})
export class LoginFormOrganism {
  @Output() loginSuccess = new EventEmitter<void>();
  @Output() switchToRegister = new EventEmitter<void>();

  numeroDocumento: string = '';
  contrasena: string = '';
  mostrarContrasena: boolean = false;
  cargando: boolean = false;
  error: string = '';

  constructor(
    private readonly auth: AuthService,
    private readonly http: HttpClient
  ) {}

  onLogin(): void {
    const documentoTrimmed = this.numeroDocumento ? this.numeroDocumento.trim() : '';
    const contrasenaTrimmed = this.contrasena ? this.contrasena.trim() : '';
    
    if (!documentoTrimmed) {
      this.error = 'Por favor ingresa tu número de documento';
      return;
    }

    // Por requerimiento, el número de identificación puede actuar como usuario y contraseña a la vez
    const contrasenaToUse = contrasenaTrimmed || documentoTrimmed;

    this.cargando = true;
    this.error = '';

    // 1. Autenticar en Keycloak mediante Direct Grant (ROPC Flow)
    this.auth.loginWithCredentials(documentoTrimmed, contrasenaToUse).subscribe({
      next: (kcResponse) => {
        // Almacenar temporalmente los tokens de Keycloak en localStorage
        if (kcResponse.access_token) {
          localStorage.setItem('kc_token', kcResponse.access_token);
        } else {
          localStorage.removeItem('kc_token');
        }

        if (kcResponse.refresh_token) {
          localStorage.setItem('kc_refreshToken', kcResponse.refresh_token);
        } else {
          localStorage.removeItem('kc_refreshToken');
        }

        if (kcResponse.id_token) {
          localStorage.setItem('kc_idToken', kcResponse.id_token);
        } else {
          localStorage.removeItem('kc_idToken');
        }

        // 2. Autenticar y cargar perfil desde el Backend local
        // Enviar el JWT de Keycloak para que el backend confíe en él
        // y omita la validación BCrypt local.
        const request: LoginRequest = {
          numeroDocumento: documentoTrimmed,
          contrasena: contrasenaToUse
        };

        const kcHeaders = new HttpHeaders({
          'Authorization': `Bearer ${kcResponse.access_token}`
        });

        this.http.post<any>(`${environment.apiBaseUrl}/sesion/login`, request, { headers: kcHeaders }).subscribe({
          next: (response) => {
            this.cargando = false;
            if (response.success && response.data) {
              const user: LoginResponse = response.data;
              if (user.activo) {
                this.guardarSesion(user);
                this.loginSuccess.emit();
              } else {
                this.limpiarSesionLocal();
                this.error = 'Tu cuenta está inactiva. Contacta al administrador.';
              }
            } else {
              this.limpiarSesionLocal();
              this.error = response.message || 'Credenciales incorrectas';
            }
          },
          error: (err) => {
            this.cargando = false;
            this.limpiarSesionLocal();
            if (err.status === 401 || err.status === 400) {
              this.error = 'Número de documento o contraseña incorrectos';
            } else {
              this.error = 'Error al conectar con el servidor. Intenta más tarde.';
            }
          }
        });
      },
      error: (err) => {
        // Keycloak ROPC falló: el usuario puede no existir en Keycloak
        // (ej: admin, agendador, médicos solo están en la BD local).
        // Intentar autenticación solo contra el backend local.
        console.warn('Keycloak ROPC falló, intentando autenticación local:', err.status);
        
        const request: LoginRequest = {
          numeroDocumento: documentoTrimmed,
          contrasena: contrasenaToUse
        };

        this.http.post<any>(`${environment.apiBaseUrl}/sesion/login`, request).subscribe({
          next: (response) => {
            this.cargando = false;
            if (response.success && response.data) {
              const user: LoginResponse = response.data;
              if (user.activo) {
                this.guardarSesion(user);
                this.loginSuccess.emit();
              } else {
                this.limpiarSesionLocal();
                this.error = 'Tu cuenta está inactiva. Contacta al administrador.';
              }
            } else {
              this.limpiarSesionLocal();
              this.error = response.message || 'Credenciales incorrectas';
            }
          },
          error: (backendErr) => {
            this.cargando = false;
            this.limpiarSesionLocal();
            if (backendErr.status === 404) {
              this.error = 'Número de documento no registrado';
            } else if (backendErr.status === 422 || backendErr.status === 400) {
              this.error = 'Número de documento o contraseña incorrectos';
            } else {
              this.error = 'Error al conectar con el servidor. Intenta más tarde.';
            }
          }
        });
      }
    });
  }

  private guardarSesion(user: LoginResponse): void {
    const pacienteId = user.rol === 'PACIENTE' ? user.id : null;
    const sessionData = {
      id: user.id,
      numeroDocumento: user.numeroDocumento,
      nombres: user.nombres,
      apellidos: user.apellidos,
      correo: user.correo,
      rol: user.rol,
      activo: user.activo,
      patientId: pacienteId,
      patientName: `${user.nombres} ${user.apellidos}`,
      celular: user.celular || '',
      genero: (user.genero || 'HOMBRE') as 'HOMBRE' | 'MUJER' | 'OTRO'
    };
    localStorage.setItem('piedrazul_user', JSON.stringify(sessionData));
  }

  private limpiarSesionLocal(): void {
    localStorage.removeItem('kc_token');
    localStorage.removeItem('kc_refreshToken');
    localStorage.removeItem('kc_idToken');
    localStorage.removeItem('piedrazul_user');
  }

  toggleMostrarContrasena(): void {
    this.mostrarContrasena = !this.mostrarContrasena;
  }

  onSwitchToRegister(): void {
    this.switchToRegister.emit();
  }
}
