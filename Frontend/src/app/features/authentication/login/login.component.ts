import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginFormOrganism } from '../../../shared/organisms/login-form/login-form.organism';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, LoginFormOrganism],
  template: `<organismo-login-form (loginSuccess)="loginSuccess.emit()" (switchToRegister)="switchToRegister.emit()"></organismo-login-form>`,
  styles: [`:host { display: block; width: 100%; }`]
})
export class LoginComponent {
  @Output() loginSuccess = new EventEmitter<void>();
  @Output() switchToRegister = new EventEmitter<void>();
}