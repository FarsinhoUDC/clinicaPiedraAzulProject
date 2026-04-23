import { Routes } from '@angular/router';

import { agendadorGuard, adminGuard, authGuard, pacienteGuard } from './core/guards/auth.guard';
import { AvailabilityConfigComponent } from './features/admin/availability-config/availability-config.component';
import { AppointmentSearchComponent } from './features/agendador/appointment-search/appointment-search.component';
import { NewAppointmentFormComponent } from './features/agendador/new-appointment-form/new-appointment-form.component';
import { PatientPortalComponent } from './features/patient/patient-portal/patient-portal.component';
import { PatientRegistrationComponent } from './features/patient/patient-registration/patient-registration.component';
import { DashboardComponent } from './features/agendador/dashboard/dashboard.component';
import { MisCitasComponent } from './features/patient/mis-citas/mis-citas.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'inicio' },

  // Pública
  { path: 'inicio', component: DashboardComponent },
  { path: 'paciente/registro', component: PatientRegistrationComponent },

  // Solo MEDICO / AGENDADOR
  { path: 'agendador/consulta',  component: AppointmentSearchComponent,  canActivate: [agendadorGuard] },
  { path: 'agendador/nuevaCita', component: NewAppointmentFormComponent,  canActivate: [agendadorGuard] },

  // Solo PACIENTE
  { path: 'paciente/portal', component: PatientPortalComponent, canActivate: [pacienteGuard] },
  { path: 'paciente/mis-citas', component: MisCitasComponent, canActivate: [pacienteGuard] },

  // Solo ADMIN
  { path: 'admin/disponibilidad', component: AvailabilityConfigComponent, canActivate: [adminGuard] },

  { path: '**', redirectTo: 'inicio' }
];
