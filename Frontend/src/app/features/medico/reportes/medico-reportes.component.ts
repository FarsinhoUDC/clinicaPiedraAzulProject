import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';
import { keycloak } from '../../../core/services/keycloak-init';

interface MedicoOption {
  id: number;
  nombre: string;
  especialidad?: string;
}


@Component({
  selector: 'app-medico-reportes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './medico-reportes.component.html',
  styleUrl:    './medico-reportes.component.css'
})
export class MedicoReportesComponent implements OnInit {

  // ── Estado ────────────────────────────────────────────────────────────────
  readonly medicos      = signal<MedicoOption[]>([]);
  readonly loading      = signal(false);
  readonly descargando  = signal(false);
  readonly errorMsg     = signal('');
  readonly successMsg   = signal('');
  readonly nombreMedico = signal('');
  readonly userId       = signal('');

  // Controla la fecha máxima (hoy) para el selector
  readonly hoy = new Date().toISOString().split('T')[0];

  // ── Formulario ─────────────────────────────────────────────────────────────
  readonly form = this.fb.nonNullable.group({
    medicoId: [0, [Validators.required, Validators.min(1)]],
    fecha:    ['', Validators.required]
  });

  constructor(
    private readonly fb:   FormBuilder,
    private readonly http: HttpClient,
    private readonly auth: AuthService
  ) {}

  ngOnInit(): void {
    // Extraer info del médico logueado desde el JWT
    this.nombreMedico.set(this.auth.getFullName() || this.auth.getUsername());
    this.userId.set(this.auth.getUserId());

    this.cargarMedicos();
  }

  // ── Cargar médicos ─────────────────────────────────────────────────────────
  cargarMedicos(): void {
    this.loading.set(true);
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${keycloak.token ?? ''}`
    });

    this.http.get<any[]>(`${environment.apiBaseUrl}/medicos`, { headers })
      .subscribe({
        next: (data) => {
          const opts: MedicoOption[] = data.map(m => ({
            id:           m.id ?? m.medicoId,
            nombre:       `${m.nombre ?? ''} ${m.apellido ?? m.apellidos ?? ''}`.trim(),
            especialidad: m.especialidad
          }));
          this.medicos.set(opts);
          this.loading.set(false);
        },
        error: () => {
          // Si falla la carga de médicos, mostramos un mensaje pero no bloqueamos
          this.errorMsg.set('No se pudieron cargar los médicos. Puedes ingresar el ID manualmente.');
          this.loading.set(false);
        }
      });
  }

  // ── Exportar CSV ───────────────────────────────────────────────────────────
  exportarCSV(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { medicoId, fecha } = this.form.getRawValue();
    this.descargando.set(true);
    this.errorMsg.set('');
    this.successMsg.set('');

    const url = `${environment.apiBaseUrl}/reportes/citas/${medicoId}/${fecha}`;
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${keycloak.token ?? ''}`
    });

    this.http.get(url, {
      headers,
      responseType: 'blob',
      observe: 'response'
    }).subscribe({
      next: (response) => {
        if (response.status === 204 || !response.body) {
          this.successMsg.set('ℹ No hay citas registradas para ese médico en esa fecha.');
          this.descargando.set(false);
          return;
        }

        // Extraer nombre del archivo del header Content-Disposition
        const cd = response.headers.get('Content-Disposition') ?? '';
        const filename = cd.match(/filename="(.+)"/)?.[1] ?? `reporte_${fecha}.csv`;

        // Descargar el blob automáticamente
        const blob = new Blob([response.body], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = filename;
        link.click();
        URL.revokeObjectURL(link.href);

        this.successMsg.set(`✓ Reporte "${filename}" descargado correctamente.`);
        this.descargando.set(false);
      },
      error: (err) => {
        if (err.status === 403) {
          this.errorMsg.set('Acceso denegado. Solo los médicos pueden exportar reportes.');
        } else if (err.status === 404) {
          this.errorMsg.set('No se encontró el médico especificado.');
        } else {
          this.errorMsg.set('Error al generar el reporte. Intenta nuevamente.');
        }
        this.descargando.set(false);
        console.error(err);
      }
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  isInvalidField(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl.touched);
  }

  resetForm(): void {
    this.form.reset({ medicoId: 0, fecha: '' });
    this.successMsg.set('');
    this.errorMsg.set('');
  }
}
