import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AtomChipComponent, AtomSpinnerComponent } from '../../atoms/index';

export interface SlotOption {
  hora: string;
  label?: string;
}

@Component({
  selector: 'molecule-slot-picker',
  standalone: true,
  imports: [CommonModule, AtomChipComponent, AtomSpinnerComponent],
  templateUrl: './slot-picker.component.html',
  styleUrls: ['./slot-picker.component.css']
})
export class MoleculeSlotPickerComponent {
  @Input() slots: SlotOption[] = [];
  @Input() selectedSlot = '';
  @Input() isLoading = false;
  @Input() message = '';
  @Input() errorMessage = '';
  @Input() dayUnavailableMessage = '';
  @Input() label = 'Hora disponible *';
  @Output() onSelectSlot = new EventEmitter<string>();

  selectSlot(hora: string): void {
    this.onSelectSlot.emit(hora);
  }
}
