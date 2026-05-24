import { Component, Output, EventEmitter } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';
import { AtomButtonComponent } from '../../atoms/button/button.component';
import { AtomInputComponent } from '../../atoms/input/input.component';
import { AtomSelectComponent } from '../../atoms/select/select.component';
import { AtomSpinnerComponent } from '../../atoms/spinner/spinner.component';

@Component({
  selector: 'organismo-register-form',
  standalone: true,
  imports: [FormsModule, CommonModule, AtomButtonComponent, AtomInputComponent, AtomSelectComponent, AtomSpinnerComponent],
  templateUrl: './register-form.organism.html',
  styleUrls: ['./register-form.organism.css']
})
export class RegisterFormOrganism {
  @Output() registerSuccess = new EventEmitter<void>();
  @Output() switchToLogin = new EventEmitter<void>();

  documentNumber: string = '';
  documentType: string = 'CC';
  firstName: string = '';
  lastName: string = '';
  phone: string = '';
  gender: string = '';
  birthDate: string = '';
  email: string = '';

  cargando: boolean = false;
  error: string = '';
  success: string = '';

  constructor(private http: HttpClient) {}

  onRegister(): void {
    this.error = '';
    this.success = '';

    if (!this.documentNumber || !this.firstName || !this.lastName || !this.phone) {
      this.error = 'Por favor completa todos los campos requeridos';
      return;
    }

    this.cargando = true;

    const generoMap: { [key: string]: string } = {
      'M': 'HOMBRE', 'F': 'MUJER', 'OTRO': 'OTRO', 'PND': 'OTRO'
    };

    const request = {
      numeroDocumento: this.documentNumber,
      nombres: this.firstName,
      apellidos: this.lastName,
      correo: this.email,
      contrasena: this.documentNumber,
      celular: this.phone,
      genero: generoMap[this.gender] || 'OTRO',
      fechaNacimiento: this.birthDate || null
    };

    this.http.post<any>(`${environment.apiBaseUrl}/pacientes`, request).subscribe({
      next: (response) => {
        this.cargando = false;
        if (response.success) {
          this.success = 'Registro exitoso. Ya puedes iniciar sesión.';
        } else {
          this.error = response.message || 'Error al registrar';
        }
      },
      error: (err) => {
        this.cargando = false;
        this.error = err?.error?.message || err?.message || 'Error al conectar con el servidor';
      }
    });
  }

  onSuccessClose(): void {
    this.registerSuccess.emit();
  }

  onSwitchToLogin(): void {
    this.switchToLogin.emit();
  }
}
