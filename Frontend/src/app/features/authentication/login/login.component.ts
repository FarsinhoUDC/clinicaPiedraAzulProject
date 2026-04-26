import { Component } from '@angular/core';
import { Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';


@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  @Output() switchToRegister = new EventEmitter<void>();

  cargando = false;

  constructor(private readonly auth: AuthService) {}

  /** Redirige al login oficial de Keycloak */
  onLogin(): void {
    this.cargando = true;
    this.auth.login(); // → keycloak.login({ locale: 'es' })
  }

  onSwitchToRegister(): void {
    this.switchToRegister.emit();
  }
}