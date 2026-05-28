import { Routes } from '@angular/router';

import { agendadorGuard, adminGuard, medicoGuard, pacienteGuard } from './core/guards/auth.guard';
import { AvailabilityConfigComponent } from './features/admin/availability-config/availability-config.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { AppointmentSearchComponent } from './features/agendador/appointment-search/appointment-search.component';
import { NewAppointmentFormComponent } from './features/agendador/new-appointment-form/new-appointment-form.component';
import { MedicoReportesComponent } from './features/medico/reportes/medico-reportes.component';
import { ReagendarCitaComponent } from './features/medico/reagendar/reagendar-cita.component';
import { PatientPortalComponent } from './features/patient/patient-portal/patient-portal.component';
import { PatientRegistrationComponent } from './features/patient/patient-registration/patient-registration.component';
import { HomeComponent } from './features/home/home.component';
import { MisCitasComponent } from './features/patient/mis-citas/mis-citas.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },

  // Página de inicio pública — visible para todos (logueados y no logueados)
  { path: 'inicio', component: HomeComponent },
  { path: 'paciente/registro', component: PatientRegistrationComponent },

  // Solo MEDICO / AGENDADOR
  { path: 'agendador/consulta',  component: AppointmentSearchComponent,  canActivate: [agendadorGuard] },
  { path: 'agendador/nuevaCita', component: NewAppointmentFormComponent,  canActivate: [agendadorGuard] },

  // Solo MEDICO — Exportación CSV
  { path: 'medico/reportes', component: MedicoReportesComponent, canActivate: [medicoGuard] },

  // Solo MEDICO — Re-agendamiento de citas
  { path: 'medico/reagendar', component: ReagendarCitaComponent, canActivate: [medicoGuard] },

  // Solo PACIENTE
  { path: 'paciente/portal',    component: PatientPortalComponent,      canActivate: [pacienteGuard] },
  { path: 'paciente/mis-citas', component: MisCitasComponent,           canActivate: [pacienteGuard] },

  // Solo ADMIN — Configuración y gestión de usuarios
  { path: 'admin/disponibilidad', component: AvailabilityConfigComponent, canActivate: [adminGuard] },
  { path: 'admin/usuarios',       component: UserManagementComponent,     canActivate: [adminGuard] },

  { path: '**', redirectTo: 'inicio' }
];