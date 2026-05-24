import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { AtomButtonComponent } from '../../atoms/button/button.component';
import { AtomSpinnerComponent } from '../../atoms/spinner/spinner.component';

@Component({
  selector: 'organismo-login-form',
  standalone: true,
  imports: [CommonModule, AtomButtonComponent, AtomSpinnerComponent],
  templateUrl: './login-form.organism.html',
  styleUrls: ['./login-form.organism.css']
})
export class LoginFormOrganism {
  @Output() loginSuccess = new EventEmitter<void>();
  @Output() switchToRegister = new EventEmitter<void>();

  cargando = false;

  constructor(private readonly auth: AuthService) {}

  onLogin(): void {
    this.cargando = true;
    this.auth.login();
  }

  onSwitchToRegister(): void {
    this.switchToRegister.emit();
  }
}
