import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Appointment } from '../../../core/models/appointment.model';
import { AppointmentApiService } from '../../../core/services/appointment-api.service';
import { SessionService } from '../../../core/services/session.service';

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
    private readonly sessionService: SessionService
  ) {}

  ngOnInit(): void {
    this.sessionName = this.sessionService.getSession()?.patientName ?? 'Paciente';
    const session = this.sessionService.getSession();
    if (!session || !session.patientId) {
      this.errorMessage = 'Sesión no válida';
      this.isLoading = false;
      return;
    }

    this.appointmentApi.getByPatient(session.patientId).subscribe({
      next: (citas) => {
        this.citas = citas;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No fue posible cargar las citas';
        this.isLoading = false;
      }
    });
  }
}