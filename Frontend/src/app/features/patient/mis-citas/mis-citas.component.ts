import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Appointment } from '../../../core/models/appointment.model';
import { AppointmentApiService } from '../../../core/services/appointment-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { AtomButtonComponent, AtomBadgeComponent, AtomSpinnerComponent } from '../../../shared/atoms/index';


@Component({
  selector: 'app-mis-citas',
  standalone: true,
  imports: [CommonModule, AtomButtonComponent, AtomBadgeComponent, AtomSpinnerComponent],
  templateUrl: './mis-citas.component.html',
  styleUrls: ['./mis-citas.component.css']
})
export class MisCitasComponent implements OnInit {
  citas: Appointment[] = [];
  isLoading = true;
  errorMessage = '';
  sessionName = '';

  constructor(
    private readonly appointmentApi: AppointmentApiService,
    private readonly auth: AuthService
  ) { }

  ngOnInit(): void {
    // Nombre del paciente desde el JWT
    this.sessionName = this.auth.getFullName() || this.auth.getUsername() || 'Paciente';

    // Cargar citas — el backend identifica al paciente por el JWT
    this.appointmentApi.getMisCitas().subscribe({
      next: (citas) => {
        this.citas = citas;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No fue posible cargar las citas. Intenta de nuevo.';
        this.isLoading = false;
      }
    });
  }


  getBadgeVariant(status?: string): 'admin' | 'medico' | 'agendador' | 'paciente' | 'default' | 'success' | 'warning' | 'error' {
    switch (status) {
      case 'CONFIRMADA': return 'success';
      case 'CANCELADA': return 'error';
      case 'REAGENDADA': return 'warning';
      case 'FINALIZADA': return 'default';
      default: return 'warning';
    }
  }

  esFutura(cita: Appointment): boolean {
    return new Date(cita.fechaHora) > new Date();
  }
}