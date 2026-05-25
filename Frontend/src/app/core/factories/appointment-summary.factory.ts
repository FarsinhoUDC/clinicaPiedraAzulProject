import { Appointment, AppointmentStatus, AppointmentSummaryItem } from '../models/appointment.model';

export class AppointmentSummaryFactory {
  static create(items: Appointment[]): AppointmentSummaryItem[] {
    const counts = items.reduce<Record<AppointmentStatus, number>>((acc, item) => {
      const status = item.estado ?? 'PENDIENTE';
      acc[status] += 1;
      return acc;
    }, {
      CONFIRMADA: 0,
      PROGRAMADA: 0,
      PENDIENTE:  0,
      CANCELADA:  0,
      REAGENDADA: 0,
      FINALIZADA: 0
    });

    return [
      { label: 'Confirmadas', total: counts.CONFIRMADA + counts.PROGRAMADA, tone: 'primary' },
      { label: 'Pendientes',  total: counts.PENDIENTE,                     tone: 'warning' },
      { label: 'Canceladas',  total: counts.CANCELADA,                     tone: 'neutral' },
      { label: 'Reagendadas', total: counts.REAGENDADA,                    tone: 'primary' },
    ];
  }
}