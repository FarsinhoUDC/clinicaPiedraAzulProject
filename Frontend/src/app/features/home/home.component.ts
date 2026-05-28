import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Doctor } from '../../core/models/doctor.model';
import { DoctorApiService } from '../../core/services/doctor-api.service';

interface Specialty {
  name: string;
  key: string;
}

interface Promotion {
  title: string;
  description: string;
  image: string;
  active: boolean;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit, OnDestroy {
  currentSlide = 0;
  private slideInterval: ReturnType<typeof setInterval> | null = null;

  doctors: Doctor[] = [];
  loading = true;
  errorMessage = '';

  readonly promotions: Promotion[] = [
    {
      title: 'Chequeo Médico General',
      description: '30% de descuento durante este mes.',
      image: 'https://images.pexels.com/photos/7579825/pexels-photo-7579825.jpeg',
      active: true
    },
    {
      title: 'Fisioterapia',
      description: 'Plan de rehabilitación con 15% de descuento.',
      image: 'https://images.pexels.com/photos/7088539/pexels-photo-7088539.jpeg',
      active: false
    },
    {
      title: 'Terapia Neural',
      description: '20% de descuento en tu primera sesión.',
      image: 'https://images.pexels.com/photos/3735746/pexels-photo-3735746.jpeg',
      active: false
    }
  ];

  selectedSpecialty = '';

  constructor(private readonly doctorApi: DoctorApiService) {}

  ngOnInit(): void {
    this.startAutoSlide();
    this.doctorApi.list().subscribe({
      next: (doctors) => {
        this.doctors = doctors;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los médicos. Intente más tarde.';
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    this.stopAutoSlide();
  }

  get specialties(): Specialty[] {
    const keys = new Set<string>();
    for (const d of this.doctors) {
      const s = d.especialidad?.trim();
      if (s) keys.add(s);
    }
    return Array.from(keys).sort().map(k => ({ name: k, key: k }));
  }

  get filteredDoctors(): Doctor[] {
    if (!this.selectedSpecialty) return this.doctors;
    return this.doctors.filter(d => d.especialidad?.trim() === this.selectedSpecialty);
  }

  selectSpecialty(key: string): void {
    this.selectedSpecialty = this.selectedSpecialty === key ? '' : key;
  }

  private startAutoSlide(): void {
    this.stopAutoSlide();
    this.slideInterval = setInterval(() => this.nextSlide(), 5000);
  }

  private stopAutoSlide(): void {
    if (this.slideInterval) {
      clearInterval(this.slideInterval);
      this.slideInterval = null;
    }
  }

  prevSlide(): void {
    this.currentSlide = this.currentSlide === 0 ? this.promotions.length - 1 : this.currentSlide - 1;
    this.startAutoSlide();
  }

  nextSlide(): void {
    this.currentSlide = this.currentSlide === this.promotions.length - 1 ? 0 : this.currentSlide + 1;
    this.startAutoSlide();
  }

  goToSlide(index: number): void {
    this.currentSlide = index;
    this.startAutoSlide();
  }
}
