import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AtomButtonComponent } from '../../atoms/index';

export interface DoctorCardData {
  id: number;
  nombres: string;
  apellidos: string;
  especialidad?: string;
}

@Component({
  selector: 'molecule-doctor-card',
  standalone: true,
  imports: [CommonModule, AtomButtonComponent],
  templateUrl: './doctor-card.component.html',
  styleUrls: ['./doctor-card.component.css']
})
export class MoleculeDoctorCardComponent {
  @Input() doctor!: DoctorCardData;
  @Input() buttonText = 'Agendar';
  @Output() onSelect = new EventEmitter<DoctorCardData>();

  select(): void {
    this.onSelect.emit(this.doctor);
  }
}
