import { Component, Output, EventEmitter } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { PatientApiService } from '../../../core/services/patient-api.service';
import { PatientRequest } from '../../../core/models/patient.model';
import { AtomButtonComponent } from '../../atoms/button/button.component';
import { AtomInputComponent } from '../../atoms/input/input.component';
import { AtomSelectComponent } from '../../atoms/select/select.component';
import { AtomSpinnerComponent } from '../../atoms/spinner/spinner.component';
import { MoleculeFormFieldComponent } from '../../molecules/form-field/form-field.component';
import {
  colombianCellphoneValidator,
  digitsOnlyValidator,
  lettersOnlyValidator,
  noMaliciousCharsValidator,
  notFutureDateValidator
} from '../../validators/custom-validators';

@Component({
  selector: 'organismo-register-form',
  standalone: true,
  imports: [
    ReactiveFormsModule, CommonModule,
    AtomButtonComponent, AtomInputComponent,
    AtomSelectComponent, AtomSpinnerComponent,
    MoleculeFormFieldComponent
  ],
  templateUrl: './register-form.organism.html',
  styleUrls: ['./register-form.organism.css']
})
export class RegisterFormOrganism {
  @Output() registerSuccess = new EventEmitter<void>();
  @Output() switchToLogin   = new EventEmitter<void>();

  readonly form = this.fb.group({
    documentNumber: ['', [Validators.required, digitsOnlyValidator(), noMaliciousCharsValidator()]],
    documentType:   ['CC'],
    firstName:  ['', [Validators.required, lettersOnlyValidator(), noMaliciousCharsValidator()]],
    lastName:   ['', [Validators.required, lettersOnlyValidator(), noMaliciousCharsValidator()]],
    phone:      ['', [Validators.required, colombianCellphoneValidator(), digitsOnlyValidator()]],
    gender:     [''],
    birthDate:  ['', notFutureDateValidator()],
    email:      ['', Validators.email]
  });

  cargando = false;
  error    = '';
  success  = '';

  constructor(
    private readonly fb: FormBuilder,
    private readonly patientApi: PatientApiService
  ) {}

  get f() { return this.form.controls; }

  onRegister(): void {
    this.error = '';
    this.success = '';

    if (this.form.invalid) {
      const c = this.f;
      if (c.documentNumber.errors?.['required'] || c.firstName.errors?.['required']
          || c.lastName.errors?.['required'] || c.phone.errors?.['required']) {
        this.error = 'Por favor completa todos los campos requeridos';
      } else if (c.documentNumber.errors?.['digitsOnly']) {
        this.error = 'El número de documento solo acepta números';
      } else if (c.firstName.errors?.['lettersOnly'] || c.lastName.errors?.['lettersOnly']) {
        this.error = 'Los nombres solo aceptan letras';
      } else if (c.phone.errors?.['colombianCellphone']) {
        this.error = c.phone.errors?.['colombianCellphone'];
      } else if (c.phone.errors?.['digitsOnly']) {
        this.error = 'El celular solo acepta números';
      } else if (c.birthDate.errors?.['notFutureDate']) {
        this.error = 'La fecha de nacimiento no puede ser posterior al día de hoy';
      } else if (c.email.errors?.['email']) {
        this.error = 'Correo electrónico inválido';
      } else {
        this.error = 'Corrige los errores del formulario';
      }
      return;
    }

    this.cargando = true;
    const generoMap: Record<string, 'HOMBRE' | 'MUJER' | 'OTRO'> = {
      M: 'HOMBRE', F: 'MUJER', OTRO: 'OTRO', PND: 'OTRO'
    };
    const v = this.form.getRawValue();
    const payload: PatientRequest = {
      numeroDocumento: v.documentNumber ?? '',
      nombres:         v.firstName     ?? '',
      apellidos:       v.lastName      ?? '',
      correo:          v.email         ?? '',
      contrasena:      v.documentNumber ?? '',
      celular:         v.phone         ?? '',
      genero:          generoMap[v.gender ?? ''] ?? 'OTRO',
      fechaNacimiento: v.birthDate || null
    };

    this.patientApi.save(payload).subscribe({
      next: () => {
        this.cargando = false;
        this.success  = 'Registro exitoso. Ya puedes iniciar sesión.';
      },
      error: (err) => {
        this.cargando = false;
        this.error = err?.error?.message || err?.message || 'Error al conectar con el servidor';
      }
    });
  }

  onSuccessClose(): void { this.registerSuccess.emit(); }
  onSwitchToLogin(): void { this.switchToLogin.emit(); }
}
