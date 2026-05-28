import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { AtomButtonComponent, AtomInputComponent, AtomSelectComponent, AtomSpinnerComponent, type SelectOption } from '../../atoms/index';

@Component({
  selector: 'molecule-search-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AtomButtonComponent, AtomInputComponent, AtomSelectComponent, AtomSpinnerComponent],
  templateUrl: './search-form.component.html',
  styleUrls: ['./search-form.component.css']
})
export class MoleculeSearchFormComponent {
  @Input() formGroup!: FormGroup;
  @Input() doctorOptions: SelectOption[] = [];
  @Input() isLoading = false;
  @Input() buttonText = 'Buscar';
  @Input() doctorLabel = 'Medico / Terapista *';
  @Input() doctorPlaceholder = 'Seleccione un medico';
  @Input() dateLabel = 'Fecha *';
  @Output() onSearch = new EventEmitter<void>();
}
