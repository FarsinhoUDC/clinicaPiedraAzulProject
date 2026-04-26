import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Appointment } from '../../../core/models/appointment.model';
import { AppointmentApiService } from '../../../core/services/appointment-api.service';
import { AuthService } from '../../../core/services/auth.service';

/**
 * MisCitasComponent — Muestra las citas del paciente autenticado.
 *
 * Con Keycloak, el paciente es identificado por su JWT.
 * El endpoint GET /api/citas/mis-citas extrae el preferred_username
 * (número de documento) directamente del token en el backend,
 * por lo que no se necesita ningún ID de sesión local.
 */
@Component({
  selector: 'app-mis-citas',
  standalone: true,
  imports: [CommonModule],
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
  ) {}

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

  /** Determina si una cita es futura */
  esFutura(cita: Appointment): boolean {
    return new Date(cita.fechaHora) > new Date();
  }
}