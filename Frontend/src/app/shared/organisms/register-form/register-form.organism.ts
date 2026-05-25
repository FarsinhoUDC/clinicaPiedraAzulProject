import { Component, Output, EventEmitter } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { environment } from '../../../../environments/environment';
import { AtomButtonComponent } from '../../atoms/button/button.component';
import { AtomInputComponent } from '../../atoms/input/input.component';
import { AtomSelectComponent } from '../../atoms/select/select.component';
import { AtomSpinnerComponent } from '../../atoms/spinner/spinner.component';
import { colombianCellphoneValidator, digitsOnlyValidator, lettersOnlyValidator, noMaliciousCharsValidator, notFutureDateValidator } from '../../validators/custom-validators';

@Component({
  selector: 'organismo-register-form',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, AtomButtonComponent, AtomInputComponent, AtomSelectComponent, AtomSpinnerComponent],
  templateUrl: './register-form.organism.html',
  styleUrls: ['./register-form.organism.css']
})
export class RegisterFormOrganism {
  @Output() registerSuccess = new EventEmitter<void>();
  @Output() switchToLogin = new EventEmitter<void>();

  readonly form = this.formBuilder.group({
    documentNumber: ['', [Validators.required, digitsOnlyValidator(), noMaliciousCharsValidator()]],
    documentType: ['CC'],
    firstName: ['', [Validators.required, lettersOnlyValidator(), noMaliciousCharsValidator()]],
    lastName: ['', [Validators.required, lettersOnlyValidator(), noMaliciousCharsValidator()]],
    phone: ['', [Validators.required, colombianCellphoneValidator(), digitsOnlyValidator()]],
    gender: [''],
    birthDate: ['', notFutureDateValidator()],
    email: ['', Validators.email]
  });

  cargando: boolean = false;
  error: string = '';
  success: string = '';

  constructor(
    private readonly formBuilder: FormBuilder,
    private http: HttpClient
  ) {}

  get f() {
    return this.form.controls;
  }

  onRegister(): void {
    this.error = '';
    this.success = '';

    if (this.form.invalid) {
      if (this.f.documentNumber.errors?.['required'] || this.f.firstName.errors?.['required'] || this.f.lastName.errors?.['required'] || this.f.phone.errors?.['required']) {
        this.error = 'Por favor completa todos los campos requeridos';
      } else if (this.f.documentNumber.errors?.['digitsOnly']) {
        this.error = 'El número de documento solo acepta números';
      } else if (this.f.firstName.errors?.['lettersOnly'] || this.f.lastName.errors?.['lettersOnly']) {
        this.error = 'Los nombres solo aceptan letras';
      } else if (this.f.phone.errors?.['colombianCellphone']) {
        this.error = this.f.phone.errors?.['colombianCellphone'];
      } else if (this.f.phone.errors?.['digitsOnly']) {
        this.error = 'El celular solo acepta números';
      } else if (this.f.birthDate.errors?.['notFutureDate']) {
        this.error = 'La fecha de nacimiento no puede ser posterior al día de hoy';
      } else if (this.f.email.errors?.['email']) {
        this.error = 'Correo electrónico inválido';
      } else {
        this.error = 'Corrige los errores del formulario';
      }
      return;
    }

    this.cargando = true;

    const generoMap: { [key: string]: string } = {
      'M': 'HOMBRE', 'F': 'MUJER', 'OTRO': 'OTRO', 'PND': 'OTRO'
    };

    const v = this.form.getRawValue();
    const request = {
      numeroDocumento: v.documentNumber ?? '',
      nombres: v.firstName ?? '',
      apellidos: v.lastName ?? '',
      correo: v.email ?? '',
      contrasena: v.documentNumber ?? '',
      celular: v.phone ?? '',
      genero: generoMap[v.gender ?? ''] || 'OTRO',
      fechaNacimiento: v.birthDate || null
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
