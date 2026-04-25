import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { HeaderComponent } from './core/components/header/header.component';
import { LoginComponent } from './features/authentication/login/login.component';
import { RegisterComponent } from './features/authentication/register/register.component';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    HeaderComponent,
    LoginComponent,
    RegisterComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  menuOpen = false;
  showLoginModal    = false;
  showRegisterModal = false;

  constructor(
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  /**
   * Al arrancar la app, si Keycloak ya detectó una sesión SSO activa,
   * redirigimos directamente al dashboard del rol — elimina la pantalla blanca.
   */
  ngOnInit(): void {
    if (this.auth.isAuthenticated()) {
      this.auth.redirectToRoleHome();
    }
  }

  // ── Propiedades de sesión (Keycloak, NO localStorage) ────────────────────

  get isLoggedIn(): boolean {
    return this.auth.isAuthenticated();
  }

  /** El sidebar solo se muestra cuando hay sesión activa */
  get showSidebar(): boolean {
    return this.auth.isAuthenticated();
  }

  /** El header siempre es visible */
  get showHeader(): boolean {
    return true;
  }

  /** MEDICO y AGENDADOR comparten la vista de gestión de citas */
  get isAgendador(): boolean {
    const rol = this.auth.getRole();
    return rol === 'MEDICO' || rol === 'AGENDADOR';
  }

  get isAdmin(): boolean {
    return this.auth.getRole() === 'ADMIN';
  }

  get isPaciente(): boolean {
    return this.auth.getRole() === 'PACIENTE';
  }

  get isMedico(): boolean {
    return this.auth.getRole() === 'MEDICO';
  }

  get brandName(): string {
    const rol = this.auth.getRole();
    if (rol === 'ADMIN')     return 'Admin';
    if (rol === 'MEDICO')    return 'Médico';
    if (rol === 'AGENDADOR') return 'Agendador';
    if (rol === 'PACIENTE')  return 'Paciente';
    return 'Piedrazul';
  }

  get userLabel(): string {
    return this.auth.getFullName() || this.auth.getUsername();
  }

  get userInitials(): string {
    const name = this.auth.getFullName();
    if (!name) return this.auth.getUsername().substring(0, 2).toUpperCase();
    return name
      .split(' ')
      .filter(w => w.length > 0)
      .map(w => w[0].toUpperCase())
      .join('')
      .substring(0, 3);
  }

  

  toggleMenu(): void { this.menuOpen = !this.menuOpen; }
  closeMenu():  void { this.menuOpen = false; }

 

  openLoginModal():    void { this.showLoginModal = true; }
  closeLoginModal():   void { this.showLoginModal = false; }
  openRegisterModal(): void { this.showRegisterModal = true; }
  closeRegisterModal(): void { this.showRegisterModal = false; }

  /** Llamado cuando LoginComponent emite loginSuccess. */
  onLoginSuccess(): void {
    this.showLoginModal = false;
    this.auth.redirectToRoleHome();
  }

  /** Llamado cuando RegisterComponent emite registerSuccess. */
  onRegisterSuccess(): void {
    this.showRegisterModal = false;
    this.router.navigate(['/inicio']);
  }



  login():  void { this.auth.login(); }
  logout(): void { this.auth.logout(); }
}
