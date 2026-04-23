import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/components/header/header.component';
import { LoginComponent } from './features/authentication/login/login.component';
import { RegisterComponent } from './features/authentication/register/register.component';

const SESSION_KEY = 'piedrazul_user';

interface UserSession {
  id: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  activo: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, HeaderComponent, LoginComponent, RegisterComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  menuOpen = false;
  showLoginModal = false;
  showRegisterModal = false;

  constructor(private router: Router) {}

  // ── Helpers de sesión ──────────────────────────────────────────

  private getUser(): UserSession | null {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw) as UserSession; } catch { return null; }
  }

  private getRol(): string {
    return this.getUser()?.rol?.toUpperCase() ?? '';
  }

  // ── Propiedades de rol ─────────────────────────────────────────

  get isLoggedIn(): boolean {
    const user = this.getUser();
    return !!(user?.id && user?.activo === true);
  }

  /** Solo el médico/agendador ve las opciones de consulta y creación de citas */
  get isAgendador(): boolean {
    const rol = this.getRol();
    return rol === 'MEDICO' || rol === 'AGENDADOR';
  }

  /** Solo el administrador ve la configuración del sistema */
  get isAdmin(): boolean {
    return this.getRol() === 'ADMIN';
  }

  /** Solo el paciente ve el portal de agendamiento propio */
  get isPaciente(): boolean {
    return this.getRol() === 'PACIENTE';
  }

  // ── UI helpers ─────────────────────────────────────────────────

  get showSidebar(): boolean {
    return this.isLoggedIn;
  }

  get showHeader(): boolean {
    return true;
  }

  get brandName(): string {
    const user = this.getUser();
    const rol = user?.rol?.toUpperCase() ?? '';
    if (rol === 'ADMIN')       return 'Admin';
    if (rol === 'MEDICO')      return 'Medico';
    if (rol === 'AGENDADOR')   return 'Agendador';
    if (rol === 'PACIENTE')   return 'Paciente';
    return 'Agenda';
  }

  get userLabel(): string {
    const user = this.getUser();
    if (!user) return '';
    return `${user.nombres} ${user.apellidos}`;
  }

  get userInitials(): string {
    const user = this.getUser();
    if (!user) return '';
    const nombre  = user.nombres.split(' ').map(n => n.charAt(0).toUpperCase()).join('');
    const apellido = user.apellidos.split(' ')[0]?.charAt(0).toUpperCase() ?? '';
    return nombre + apellido;
  }

  // ── Acciones ───────────────────────────────────────────────────

  toggleMenu():  void { this.menuOpen = !this.menuOpen; }
  closeMenu():   void { this.menuOpen = false; }

  openLoginModal():    void { this.showLoginModal = true; }
  closeLoginModal():   void { this.showLoginModal = false; }
  openRegisterModal(): void { this.showRegisterModal = true; }
  closeRegisterModal():void { this.showRegisterModal = false; }

  onLoginSuccess(): void {
    this.showLoginModal = false;
    const rol = this.getRol();
    if (rol === 'PACIENTE')                      this.router.navigate(['/paciente/portal']);
    else if (rol === 'ADMIN')                    this.router.navigate(['/admin/disponibilidad']);
    else /* MEDICO / AGENDADOR */                this.router.navigate(['/agendador/consulta']);
  }

  onRegisterSuccess(): void {
    this.showRegisterModal = false;
    this.router.navigate(['/inicio']);
  }

  logout(): void {
    localStorage.removeItem(SESSION_KEY);
    localStorage.removeItem('piedrazul.patient.session');
    window.location.href = '/inicio';
  }
}
