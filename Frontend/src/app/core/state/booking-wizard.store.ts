import { Injectable, computed, signal } from '@angular/core';
import { Doctor, TimeSlot } from '../models/doctor.model';

/**
 * PATRON SIGNAL-BASED STATE:
 * Reemplaza BehaviorSubject con Signals de Angular 17+.
 * Ventajas: sin dependencia de RxJS para estado simple,
 * detección de cambios más eficiente y API más ergonómica.
 */
@Injectable({ providedIn: 'root' })
export class BookingWizardStore {
  // ── Estado atómico ─────────────────────────────────────
  readonly step            = signal<1 | 2 | 3>(1);
  readonly selectedDoctor  = signal<Doctor | null>(null);
  readonly selectedDate    = signal<string | null>(null);
  readonly selectedSlot    = signal<TimeSlot | null>(null);

  // ── Estado derivado ────────────────────────────────────
  readonly wizardState = computed(() => ({
    step:   this.step(),
    doctor: this.selectedDoctor(),
    date:   this.selectedDate(),
    slot:   this.selectedSlot()
  }));

  readonly canAdvance = computed(() =>
    this.step() === 1
      ? this.selectedDoctor() !== null
      : this.step() === 2
        ? this.selectedDate() !== null && this.selectedSlot() !== null
        : false
  );

  // ── Mutaciones ─────────────────────────────────────────
  setStep(step: 1 | 2 | 3): void        { this.step.set(step); }
  setDoctor(d: Doctor | null): void     { this.selectedDoctor.set(d); }
  setDate(date: string | null): void    { this.selectedDate.set(date); }
  setSlot(slot: TimeSlot | null): void  { this.selectedSlot.set(slot); }

  reset(): void {
    this.step.set(1);
    this.selectedDoctor.set(null);
    this.selectedDate.set(null);
    this.selectedSlot.set(null);
  }
}
