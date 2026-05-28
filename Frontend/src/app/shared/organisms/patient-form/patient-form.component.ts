import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { AtomButtonComponent, AtomInputComponent, AtomSelectComponent, type SelectOption } from '../../atoms/index';

@Component({
  selector: 'organismo-patient-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AtomButtonComponent, AtomInputComponent, AtomSelectComponent],
  templateUrl: './patient-form.component.html',
  styleUrls: ['./patient-form.component.css']
})
export class OrganismoPatientFormComponent {
  @Input() formGroup!: FormGroup;
  @Input() title = 'Datos del Paciente';
  @Input() subtitle = '';
  @Input() genderOptions: SelectOption[] = [];
  @Input() showPasswordFields = false;
  @Input() showDocumentType = false;
  @Input() submitText = 'Guardar';
  @Input() isLoading = false;
  @Input() errorMessage = '';
  @Input() successMessage = '';
  @Input() showNote = false;
  @Output() onSubmit = new EventEmitter<void>();

  get f() { return this.formGroup?.controls || {}; }

  isInvalidField(field: string): boolean {
    const ctrl = this.formGroup?.get(field);
    return !!(ctrl?.invalid && ctrl?.touched);
  }
}
