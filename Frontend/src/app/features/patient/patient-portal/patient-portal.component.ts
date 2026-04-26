import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Appointment } from '../../../core/models/appointment.model';
import { Doctor, TimeSlot, DoctorAvailability } from '../../../core/models/doctor.model';
import { AppointmentApiService } from '../../../core/services/appointment-api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ConfigurationApiService } from '../../../core/services/configuration-api.service';
import { DoctorApiService } from '../../../core/services/doctor-api.service';
import { BookingWizardStore } from '../../../core/state/booking-wizard.store';
import { calculateWindowEndDate } from '../../../core/utils/slot-calculator.util';


@Component({
  selector: 'app-patient-portal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './patient-portal.component.html',
  styleUrls: ['./patient-portal.component.css']
})
export class PatientPortalComponent implements OnInit {
  doctors: Doctor[] = [];
  filteredDoctors: Doctor[] = [];
  specialties: string[] = [];
  selectedSpecialty = '';
  availableSlots: TimeSlot[] = [];
  confirmation: Appointment | null = null;
  sessionName = '';
  minDate = new Date().toISOString().slice(0, 10);
  maxDate = this.minDate;
  doctorAvailabilities: DoctorAvailability[] = [];
  dayUnavailableMessage = '';
  confirmError = '';

  readonly steps = [
    { index: 1, title: '1. Médico' },
    { index: 2, title: '2. Fecha y hora' },
    { index: 3, title: '3. Confirmación' }
  ] as const;

  constructor(
    public readonly wizardStore: BookingWizardStore,
    private readonly doctorApi: DoctorApiService,
    private readonly appointmentApi: AppointmentApiService,
    private readonly configurationApi: ConfigurationApiService,
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    // Nombre del paciente desde el JWT (preferred_username = nro. documento)
    this.sessionName = this.auth.getFullName() || this.auth.getUsername() || 'Paciente';
    this.doctorApi.list().subscribe((doctors) => {
      this.doctors = doctors;
      this.filteredDoctors = doctors;
      this.specialties = Array.from(new Set(doctors.map((doctor) => doctor.especialidad || 'General')));
    });

    this.configurationApi.getAppointmentWindow().subscribe((weeks) => {
      this.maxDate = calculateWindowEndDate(this.minDate, weeks);
    });
      this.configurationApi.listDoctorAvailability().subscribe((availabilities) => {
      this.doctorAvailabilities = availabilities;
    });
  }

  filterBySpecialty(value: string): void {
    this.selectedSpecialty = value;
    this.filteredDoctors = value
      ? this.doctors.filter((doctor) => (doctor.especialidad || 'General') === value)
      : this.doctors;
  }

  selectDoctor(doctor: Doctor): void {
    this.wizardStore.update({
      step: 2,
      selectedDoctor: doctor,
      selectedDate: null,
      selectedSlot: null
    });
    this.availableSlots = [];
  }

  loadSlots(date: string): void {
  const doctor = this.wizardStore.snapshot.selectedDoctor;
  if (!doctor) return;

  this.dayUnavailableMessage = '';
  this.availableSlots = [];

  const availability = this.doctorAvailabilities.find(a => Number(a.medicoId) === Number(doctor.id));
  if (availability) {
    const dayName = this.getDayName(date);
    if (!availability.diasSemana.includes(dayName)) {
      console.log('Doctor seleccionado:', doctor.id);
      console.log('Disponibilidades:', this.doctorAvailabilities);
      console.log('Availability encontrada:', availability);
      this.wizardStore.update({ selectedDate: date, selectedSlot: null });
      this.dayUnavailableMessage = `Este día no está disponible para este médico. Atiende los días: ${this.formatDays(availability.diasSemana)}.`;
      return;
    }
  }

  this.wizardStore.update({ selectedDate: date, selectedSlot: null });
  this.appointmentApi.getAvailableSlots(doctor.id, date).subscribe((slots) => {
    this.availableSlots = slots;
  });
}

  selectSlot(slot: TimeSlot): void {
    this.wizardStore.update({
      selectedSlot: slot,
      step: 3
    });
  }

  confirmAppointment(): void {
    const snapshot = this.wizardStore.snapshot;
    // Leer datos del paciente directamente del JWT de Keycloak
    const numeroDocumento = this.auth.getUsername();  // preferred_username = nro. documento
    const nombres         = this.auth.getFirstName(); // given_name de Keycloak
    const apellidos       = this.auth.getLastName();  // family_name de Keycloak

    if (!snapshot.selectedDoctor || !snapshot.selectedDate || !snapshot.selectedSlot || !numeroDocumento) {
      return;
    }
    this.confirmError = '';
    this.appointmentApi.create({
      paciente: {
        numeroDocumento,
        nombres:         nombres   || numeroDocumento, // fallback: usar documento si no hay nombre
        apellidos:       apellidos || '',
        celular:         null as any,
        genero:          null as any,
        correo:          '',
        fechaNacimiento: null
      },
      medicoId: snapshot.selectedDoctor.id,
      fechaHora: `${snapshot.selectedDate}T${snapshot.selectedSlot.hora}:00`
    }, 'PACIENTE').subscribe({
      next: (appointment) => {
        this.confirmation = appointment;
      },
      error: (err) => {
        this.confirmation = null;
        this.confirmError = err.error?.message || 'No se pudo crear la cita. Intenta de nuevo.';
      }
    });
  }

  backToDoctorSelection(): void {
    this.wizardStore.update({ step: 1, selectedDoctor: null, selectedDate: null, selectedSlot: null });
    this.availableSlots = [];
    this.confirmation = null;
  }

  closeConfirmation(): void {
  this.confirmation = null;
  this.wizardStore.reset();
  this.router.navigate(['/inicio']);
}
private getDayName(fecha: string): string {
  const days = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];
  return days[new Date(`${fecha}T00:00:00`).getDay()];
}

private formatDays(days: string[]): string {
  const labels: Record<string, string> = {
    MONDAY: 'Lunes', TUESDAY: 'Martes', WEDNESDAY: 'Miércoles',
    THURSDAY: 'Jueves', FRIDAY: 'Viernes', SATURDAY: 'Sábado', SUNDAY: 'Domingo'
  };
  return days.map(d => labels[d] ?? d).join(', ');
}
}
