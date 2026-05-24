import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'molecule-form-field',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './form-field.component.html',
  styleUrls: ['./form-field.component.css']
})
export class MoleculeFormFieldComponent {
  @Input() id = '';
  @Input() label = '';
  @Input() hasError = false;
  @Input() errorMessage = '';
}
