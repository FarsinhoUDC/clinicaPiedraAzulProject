import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AtomBadgeComponent } from '../../atoms/index';

export interface AppointmentTableRow {
  id: number;
  index: number;
  paciente: string;
  documento: string;
  celular: string;
  hora: string;
  estado: string;
}

@Component({
  selector: 'organismo-appointment-table',
  standalone: true,
  imports: [CommonModule, AtomBadgeComponent],
  templateUrl: './appointment-table.component.html',
  styleUrls: ['./appointment-table.component.css']
})
export class OrganismoAppointmentTableComponent {
  @Input() rows: AppointmentTableRow[] = [];
  @Input() showAcciones = true;
  @Input() accionText = 'Ver';
  @Output() onAccion = new EventEmitter<AppointmentTableRow>();

  getBadgeVariant(status?: string): 'admin' | 'medico' | 'agendador' | 'paciente' | 'default' | 'success' | 'warning' | 'error' {
    switch (status) {
      case 'CONFIRMADA':
      case 'PROGRAMADA': return 'success';
      case 'CANCELADA': return 'error';
      case 'FINALIZADA': return 'default';
      default: return 'warning';
    }
  }

  statusLabel(status?: string): string {
    switch (status) {
      case 'CONFIRMADA':
      case 'PROGRAMADA': return 'Confirmada';
      case 'CANCELADA': return 'Cancelada';
      case 'REAGENDADA': return 'Reagendada';
      case 'FINALIZADA': return 'Finalizada';
      default: return 'Pendiente';
    }
  }

  trackByRow(_: number, row: AppointmentTableRow): number {
    return row.id;
  }
}
