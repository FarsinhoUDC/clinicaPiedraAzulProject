import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AtomButtonComponent, AtomInputComponent, AtomSelectComponent, type SelectOption } from '../../../shared/atoms/index';
import { MoleculeStepIndicatorComponent, MoleculeDoctorCardComponent, MoleculeSlotPickerComponent, type DoctorCardData, type SlotOption } from '../../../shared/molecules/index';
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
  imports: [CommonModule, AtomButtonComponent, AtomInputComponent, AtomSelectComponent, MoleculeStepIndicatorComponent, MoleculeDoctorCardComponent, MoleculeSlotPickerComponent],
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
  ];

  get doctorCards(): DoctorCardData[] {
    return this.filteredDoctors.map(d => ({
      id: d.id,
      nombres: d.nombres,
      apellidos: d.apellidos,
      especialidad: d.especialidad
    }));
  }

  get slotOptions(): SlotOption[] {
    return this.availableSlots.map(s => ({ hora: s.hora }));
  }

  onSelectDoctor(doctor: DoctorCardData): void {
    const full = this.doctors.find(d => d.id === doctor.id);
    if (full) this.selectDoctor(full);
  }

  get specialtyOptions(): SelectOption[] {
    return [
      { label: 'Todas', value: '' },
      ...this.specialties.map(s => ({ label: s, value: s }))
    ];
  }

  constructor(
    public readonly wizardStore: BookingWizardStore,
    private readonly doctorApi: DoctorApiService,
    private readonly appointmentApi: AppointmentApiService,
    private readonly configurationApi: ConfigurationApiService,
    private readonly auth: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.sessionName = this.auth.getFullName() || this.auth.getUsername() || 'Paciente';
    this.doctorApi.list().subscribe((doctors) => {
      this.doctors = doctors;
      this.filteredDoctors = doctors;
      this.specialties = Array.from(new Set(doctors.map((d) => d.especialidad || 'General')));
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
      ? this.doctors.filter((d) => (d.especialidad || 'General') === value)
      : this.doctors;
  }

  selectDoctor(doctor: Doctor): void {
    this.wizardStore.setStep(2);
    this.wizardStore.setDoctor(doctor);
    this.wizardStore.setDate(null);
    this.wizardStore.setSlot(null);
    this.availableSlots = [];
  }

  loadSlots(date: string): void {
    const doctor = this.wizardStore.selectedDoctor();
    if (!doctor) return;

    this.dayUnavailableMessage = '';
    this.availableSlots = [];

    const availability = this.doctorAvailabilities.find(a => Number(a.medicoId) === Number(doctor.id));
    if (availability) {
      const dayName = this.getDayName(date);
      if (!availability.diasSemana.includes(dayName)) {
        this.wizardStore.setDate(date);
        this.wizardStore.setSlot(null);
        this.dayUnavailableMessage = `Este día no está disponible para este médico. Atiende los días: ${this.formatDays(availability.diasSemana)}.`;
        return;
      }
    }

    this.wizardStore.setDate(date);
    this.wizardStore.setSlot(null);
    this.appointmentApi.getAvailableSlots(doctor.id, date).subscribe((slots) => {
      this.availableSlots = slots;
    });
  }

  selectSlot(slot: TimeSlot): void {
    this.wizardStore.setSlot(slot);
  }

  onSlotSelected(hora: string): void {
    const slot: TimeSlot = { hora, disponible: true };
    this.selectSlot(slot);
  }

  goToConfirmation(): void {
    this.wizardStore.setStep(3);
  }

  confirmAppointment(): void {
    const doctor = this.wizardStore.selectedDoctor();
    const date   = this.wizardStore.selectedDate();
    const slot   = this.wizardStore.selectedSlot();
    const numeroDocumento = this.auth.getUsername();
    const nombres         = this.auth.getFirstName();
    const apellidos       = this.auth.getLastName();

    if (!doctor || !date || !slot || !numeroDocumento) return;

    this.confirmError = '';
    this.appointmentApi.create({
      paciente: {
        numeroDocumento,
        nombres:         nombres   || numeroDocumento,
        apellidos:       apellidos || '',
        celular:         null as any,
        genero:          null as any,
        correo:          '',
        fechaNacimiento: null
      },
      medicoId: doctor.id,
      fechaHora: `${date}T${slot.hora}:00`
    }, 'PACIENTE').subscribe({
      next: (appointment) => { this.confirmation = appointment; },
      error: (err) => {
        this.confirmation = null;
        this.confirmError = err.error?.message || 'No se pudo crear la cita. Intenta de nuevo.';
      }
    });
  }

  backToDoctorSelection(): void {
    this.wizardStore.setStep(1);
    this.wizardStore.setDoctor(null);
    this.wizardStore.setDate(null);
    this.wizardStore.setSlot(null);
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
