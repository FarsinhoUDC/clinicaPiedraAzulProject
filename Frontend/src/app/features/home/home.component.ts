import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';

interface DoctorCard {
  name: string;
  specialty: string;
  experience: string;
  description: string;
  schedule: string;
}

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

  readonly specialties: Specialty[] = [
    { name: 'Terapia neural', key: 'terapia' },
    { name: 'Quiropraxia', key: 'quiro' },
    { name: 'Fisioterapia', key: 'fisio' },
    { name: 'Nutrición y Dietética Terapéutica', key: 'nutri' },
    { name: 'Dermatología', key: 'derma' },
    { name: 'Cardiología', key: 'cardio' }
  ];

  readonly doctors: DoctorCard[] = [
    {
      name: 'Dra. Laura Martínez',
      specialty: 'Medicina General',
      experience: '8 años de experiencia clínica',
      description: 'Profesional enfocada en atención integral y prevención de enfermedades crónicas.',
      schedule: 'Lunes a Viernes 8:00am - 2:00pm'
    },
    {
      name: 'Dr. Andrés Gómez',
      specialty: 'Cardiología',
      experience: '12 años en enfermedades cardiovasculares',
      description: 'Especialista en diagnóstico temprano y tratamiento de hipertensión.',
      schedule: 'Lunes, Miércoles y Viernes 2:00pm - 7:00pm'
    },
    {
      name: 'Dra. Carolina Ruiz',
      specialty: 'Pediatría',
      experience: '10 años en atención infantil',
      description: 'Comprometida con el bienestar y desarrollo saludable de niños y adolescentes.',
      schedule: 'Martes y Jueves 9:00am - 4:00pm'
    },
    {
      name: 'Dr. Sebastián Torres',
      specialty: 'Ortopedia y Traumatología',
      experience: '15 años en lesiones deportivas y fracturas',
      description: 'Enfocado en rehabilitación musculoesquelética y cirugías mínimamente invasivas.',
      schedule: 'Lunes a Viernes 3:00pm - 8:00pm'
    },
    {
      name: 'Dra. Natalia Herrera',
      specialty: 'Ginecología y Obstetricia',
      experience: '11 años en salud femenina',
      description: 'Atención integral en control prenatal, planificación familiar y salud reproductiva.',
      schedule: 'Lunes, Martes y Jueves 8:00am - 1:00pm'
    },
    {
      name: 'Dr. Miguel Ángel Rojas',
      specialty: 'Dermatología',
      experience: '9 años en tratamiento de enfermedades de la piel',
      description: 'Experto en acné, dermatitis, procedimientos dermatológicos y estética médica.',
      schedule: 'Miércoles y Viernes 10:00am - 6:00pm'
    }
  ];

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
  specialtyDescription = '';

  constructor() {}

  ngOnInit(): void {
    this.startAutoSlide();
  }

  ngOnDestroy(): void {
    this.stopAutoSlide();
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

  selectSpecialty(specialty: Specialty): void {
    this.selectedSpecialty = specialty.key;
    const descriptions: Record<string, string> = {
      terapia: 'La terapia neural trata disfunciones del sistema nervioso mediante microinyecciones en puntos específicos del cuerpo.',
      quiro: 'La quiropraxia se enfoca en el diagnóstico y tratamiento de trastornos musculoesqueléticos, especialmente de la columna vertebral.',
      fisio: 'La fisioterapia ayuda a restaurar el movimiento y la función corporal después de lesiones, cirugías o enfermedades.',
      nutri: 'La nutrición terapéutica diseña planes alimenticios personalizados para prevenir y tratar enfermedades.',
      derma: 'La dermatología diagnostica y trata enfermedades de la piel, cabello y uñas.',
      cardio: 'La cardiología se ocupa de la prevención, diagnóstico y tratamiento de enfermedades del corazón y el sistema circulatorio.'
    };
    this.specialtyDescription = descriptions[specialty.key] || '';
  }


}
