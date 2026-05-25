import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Appointment, AppointmentSummaryItem } from '../../../core/models/appointment.model';
import { Doctor } from '../../../core/models/doctor.model';
import { AppointmentSummaryFactory } from '../../../core/factories/appointment-summary.factory';
import { AppointmentApiService } from '../../../core/services/appointment-api.service';
import { DoctorApiService } from '../../../core/services/doctor-api.service';
import { UiMappersService } from '../../../core/services/ui-mappers.service';
import { formatDateLabel, toHourLabel } from '../../../core/utils/date-time.utils';

import { AtomButtonComponent, AtomInputComponent, AtomSelectComponent, AtomBadgeComponent, type SelectOption } from '../../../shared/atoms/index';

@Component({
  selector: 'app-appointment-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AtomButtonComponent, AtomInputComponent, AtomSelectComponent, AtomBadgeComponent],
  templateUrl: './appointment-search.component.html',
  styleUrls: ['./appointment-search.component.css']
})
export class AppointmentSearchComponent implements OnInit {
  readonly searchForm = this.formBuilder.group({
    medicoId: ['', Validators.required],
    fecha: ['', Validators.required]
  });

  doctors: Doctor[] = [];
  appointments: Appointment[] = [];
  summary: AppointmentSummaryItem[] = [];
  isLoading = false;
  searchExecuted = false;
  errorMessage = '';

  constructor(
    private readonly formBuilder: FormBuilder,
    private readonly appointmentApi: AppointmentApiService,
    private readonly doctorApi: DoctorApiService,
    private readonly uiMappers: UiMappersService
  ) {}

  ngOnInit(): void {
    this.doctorApi.list().subscribe({
      next: (doctors) => {
        this.doctors = doctors;
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los médicos. Verifica la conexión.';
      }
    });
  }

  search(): void {
    this.searchExecuted = true;
    this.errorMessage = '';
    this.appointments = [];
    this.summary = [];

    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    const medicoId = Number(this.searchForm.value.medicoId);
    const fecha = this.searchForm.value.fecha ?? '';
    this.isLoading = true;

    this.appointmentApi.searchByDoctorAndDate(medicoId, fecha).subscribe({
      next: (appointments) => {
        try {
          this.appointments = this.uiMappers.hydrateStatus(appointments);
          this.summary = AppointmentSummaryFactory.create(this.appointments);
        } catch {
          this.appointments = appointments;
          this.summary = [];
        }
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No fue posible consultar las citas. Verifica la conexión con la API.';
        this.isLoading = false;
      }
    });
  }

  get selectedDateLabel(): string {
    const fecha = this.searchForm.value.fecha;
    return fecha ? formatDateLabel(fecha) : '';
  }

  hourLabel(dateTime: string): string {
    return toHourLabel(dateTime);
  }

  getMetricValue(label: string): number {
    return this.summary.find((item) => item.label === label)?.total ?? 0;
  }

  get doctorOptions(): SelectOption[] {
    return this.doctors.map(d => ({
      label: `${d.nombres} ${d.apellidos}${d.especialidad ? ' - ' + d.especialidad : ''}`,
      value: d.id
    }));
  }

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

  downloadCSV(): void {
    if (this.appointments.length === 0) return;

    const headers = ['ID', 'Paciente', 'Documento', 'Celular', 'Médico', 'Fecha', 'Hora', 'Estado', 'Origen'];
    const rows = this.appointments.map(apt => [
      apt.id,
      apt.nombrePaciente,
      apt.documentoPaciente,
      apt.celularPaciente,
      apt.nombreMedico,
      apt.fechaHora.split('T')[0],
      apt.fechaHora.split('T')[1]?.slice(0, 5),
      apt.estado,
      apt.origen
    ]);

    const csvContent = [headers, ...rows]
      .map(row => row.map(cell => `"${cell ?? ''}"`).join(','))
      .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `citas_${this.searchForm.value.fecha}_${this.searchForm.value.medicoId}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  }

  get canDownload(): boolean {
    return this.appointments.length > 0;
  }

  trackByAppointment(_: number, appointment: Appointment): number {
    return appointment.id;
  }
}
