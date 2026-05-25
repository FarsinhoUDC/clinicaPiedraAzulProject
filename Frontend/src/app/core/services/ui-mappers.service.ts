import { Injectable } from '@angular/core';

import { Appointment, AppointmentStatus } from '../models/appointment.model';

@Injectable({ providedIn: 'root' })
export class UiMappersService {
  hydrateStatus(appointments: Appointment[]): Appointment[] {
    return appointments.map((item) => ({
      ...item,
      estado: this.resolveStatus(item)
    }));
  }

  private resolveStatus(item: Appointment): AppointmentStatus {
    if (item.estado) {
      if (item.estado === 'PROGRAMADA') return 'CONFIRMADA';
      return item.estado;
    }
    if (item.origen === 'PACIENTE') {
      return 'CONFIRMADA';
    }
    return 'PENDIENTE';
  }
}