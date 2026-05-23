import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';

import { Appointment } from '../../../core/models/appointment.model';
import { Doctor } from '../../../core/models/doctor.model';
import { TimeSlot } from '../../../core/models/doctor.model';
import { AppointmentApiService } from '../../../core/services/appointment-api.service';
import { DoctorApiService } from '../../../core/services/doctor-api.service';
import { formatDateLabel, toHourLabel } from '../../../core/utils/date-time.utils';

type ViewState = 'search' | 'select-slot' | 'confirm' | 'success' | 'error';

@Component({
  selector: 'app-reagendar-cita',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reagendar-cita.component.html',
  styleUrl: './reagendar-cita.component.css'
})
export class ReagendarCitaComponent implements OnInit {

  // ── Estado de la vista ────────────────────────────────────────────────────
  viewState: ViewState = 'search';

  // ── Formularios ───────────────────────────────────────────────────────────
  readonly searchForm = this.fb.group({
    medicoId: ['', Validators.required],
    fecha: ['', Validators.required]
  });

  readonly reagendarForm = this.fb.group({
    nuevaFecha: ['', [Validators.required, this.futureDateValidator]],
    horario: ['', Validators.required],
    motivo: ['', [Validators.maxLength(300)]]
  });

  // ── Datos ─────────────────────────────────────────────────────────────────
  doctors: Doctor[] = [];
  appointments: Appointment[] = [];
  selectedAppointment: Appointment | null = null;
  availableSlots: TimeSlot[] = [];
  confirmedAppointment: Appointment | null = null;

  // ── Control de UI ─────────────────────────────────────────────────────────
  isLoadingAppointments = false;
  isLoadingSlots = false;
  isSubmitting = false;
  searchError = '';
  slotError = '';
  submitError = '';
  searchExecuted = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly appointmentApi: AppointmentApiService,
    private readonly doctorApi: DoctorApiService
  ) {}

  ngOnInit(): void {
    this.doctorApi.list().subscribe({
      next: (doctors) => (this.doctors = doctors),
      error: () => { /* silencioso: la lista queda vacía */ }
    });
  }

  // ── Paso 1: Buscar citas ──────────────────────────────────────────────────

  searchAppointments(): void {
    this.searchExecuted = true;
    this.searchError = '';
    this.appointments = [];

    if (this.searchForm.invalid) {
      this.searchForm.markAllAsTouched();
      return;
    }

    const medicoId = Number(this.searchForm.value.medicoId);
    const fecha = this.searchForm.value.fecha ?? '';
    this.isLoadingAppointments = true;

    this.appointmentApi.searchByDoctorAndDate(medicoId, fecha).subscribe({
      next: (appointments) => {
        // Solo mostrar citas re-agendables (excluye CANCELADA y FINALIZADA)
        this.appointments = appointments.filter(
          (a) => a.estado !== 'CANCELADA' && a.estado !== 'FINALIZADA'
        );
        this.isLoadingAppointments = false;
      },
      error: () => {
        this.searchError = 'No fue posible consultar las citas. Verifica la conexión con la API.';
        this.isLoadingAppointments = false;
      }
    });
  }

  // ── Paso 2: Seleccionar cita y cargar slots ───────────────────────────────

  selectAppointment(appointment: Appointment): void {
    this.selectedAppointment = appointment;
    this.availableSlots = [];
    this.slotError = '';
    this.reagendarForm.reset();
    this.viewState = 'select-slot';
    this.isLoadingSlots = true;

    // Pre-fill fecha con la fecha actual de la cita
    const fechaCita = appointment.fechaHora.split('T')[0];
    this.reagendarForm.patchValue({ nuevaFecha: fechaCita });
    this.loadSlots(appointment.medicoId, fechaCita);
  }

  onDateChange(): void {
    const fecha = this.reagendarForm.value.nuevaFecha;
    if (!fecha || !this.selectedAppointment) return;
    this.reagendarForm.patchValue({ horario: '' });
    this.availableSlots = [];
    this.slotError = '';
    this.isLoadingSlots = true;
    this.loadSlots(this.selectedAppointment.medicoId, fecha);
  }

  private loadSlots(medicoId: number, fecha: string): void {
    this.appointmentApi.getAvailableSlots(medicoId, fecha).subscribe({
      next: (slots) => {
        this.availableSlots = slots;
        this.isLoadingSlots = false;
        if (slots.length === 0) {
          this.slotError = 'No hay franjas disponibles para este médico en la fecha seleccionada.';
        }
      },
      error: () => {
        this.slotError = 'No fue posible cargar los horarios disponibles.';
        this.isLoadingSlots = false;
      }
    });
  }

  selectSlot(hora: string): void {
    this.reagendarForm.patchValue({ horario: hora });
  }

  proceedToConfirm(): void {
    if (this.reagendarForm.invalid) {
      this.reagendarForm.markAllAsTouched();
      return;
    }
    this.viewState = 'confirm';
  }

  // ── Paso 3: Confirmar re-agendamiento ─────────────────────────────────────

  confirmReschedule(): void {
    if (!this.selectedAppointment) return;

    const fecha = this.reagendarForm.value.nuevaFecha!;
    const hora = this.reagendarForm.value.horario!;
    const motivo = this.reagendarForm.value.motivo ?? '';

    // Backend espera ISO-8601: "2026-06-10T09:30:00"
    const nuevaFechaHora = `${fecha}T${hora}:00`;

    this.isSubmitting = true;
    this.submitError = '';

    this.appointmentApi
      .reschedule(this.selectedAppointment.id, { nuevaFechaHora, motivo })
      .subscribe({
        next: (updated) => {
          this.confirmedAppointment = updated;
          this.isSubmitting = false;
          this.viewState = 'success';
        },
        error: (err) => {
          this.submitError =
            err?.error?.message ?? 'No fue posible reagendar la cita. Inténtalo nuevamente.';
          this.isSubmitting = false;
          this.viewState = 'error';
        }
      });
  }

  // ── Navegación ────────────────────────────────────────────────────────────

  goBackToSearch(): void {
    this.viewState = 'search';
    this.selectedAppointment = null;
    this.confirmedAppointment = null;
    this.submitError = '';
  }

  goBackToSlots(): void {
    this.viewState = 'select-slot';
    this.submitError = '';
  }

  startOver(): void {
    this.viewState = 'search';
    this.selectedAppointment = null;
    this.confirmedAppointment = null;
    this.appointments = [];
    this.searchForm.reset();
    this.reagendarForm.reset();
    this.searchExecuted = false;
  }

  // ── Helpers de UI ─────────────────────────────────────────────────────────

  hourLabel(dateTime: string): string {
    return toHourLabel(dateTime);
  }

  dateLabel(dateTime: string): string {
    return formatDateLabel(dateTime.split('T')[0]);
  }

  get selectedDateLabel(): string {
    const fecha = this.reagendarForm.value.nuevaFecha;
    return fecha ? formatDateLabel(fecha) : '';
  }

  get selectedDoctor(): Doctor | undefined {
    const id = Number(this.searchForm.value.medicoId);
    return this.doctors.find((d) => d.id === id);
  }

  statusClass(status?: string): string {
    switch (status) {
      case 'CONFIRMADA':  return 'status-confirmed';
      case 'REAGENDADA':  return 'status-rescheduled';
      case 'CANCELADA':   return 'status-cancelled';
      default:            return 'status-pending';
    }
  }

  statusLabel(status?: string): string {
    switch (status) {
      case 'CONFIRMADA':  return 'Confirmada';
      case 'REAGENDADA':  return 'Reagendada';
      case 'CANCELADA':   return 'Cancelada';
      case 'FINALIZADA':  return 'Finalizada';
      default:            return 'Pendiente';
    }
  }

  trackByAppointment(_: number, a: Appointment): number { return a.id; }
  trackBySlot(_: number, s: TimeSlot): string { return s.hora; }

  // ── Validador personalizado ───────────────────────────────────────────────

  private futureDateValidator(control: AbstractControl): ValidationErrors | null {
    if (!control.value) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const selected = new Date(`${control.value}T00:00:00`);
    return selected >= today ? null : { pastDate: true };
  }
}