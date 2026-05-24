import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RegisterFormOrganism } from '../../../shared/organisms/register-form/register-form.organism';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RegisterFormOrganism],
  template: `<organismo-register-form (registerSuccess)="registerSuccess.emit()" (switchToLogin)="switchToLogin.emit()"></organismo-register-form>`,
  styles: [`:host { display: block; width: 100%; }`]
})
export class RegisterComponent {
  @Output() registerSuccess = new EventEmitter<void>();
  @Output() switchToLogin = new EventEmitter<void>();
}
