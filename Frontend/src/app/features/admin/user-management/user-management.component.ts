import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';
import {
  KeycloakAdminService,
  KeycloakUser,
  KeycloakRole,
  CreateUserPayload
} from '../../../core/services/keycloak-admin.service';

/** Roles de negocio que gestiona el administrador */
const APP_ROLES = ['ADMIN', 'MEDICO', 'AGENDADOR', 'PACIENTE'] as const;
type AppRoleName = typeof APP_ROLES[number];

interface UserWithRole extends KeycloakUser {
  rolActual?: string;
}

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-management.component.html',
  styleUrl:    './user-management.component.css'
})
export class UserManagementComponent implements OnInit {

  // ── Estado ────────────────────────────────────────────────────────────────
  readonly usuarios   = signal<UserWithRole[]>([]);
  readonly allRoles   = signal<KeycloakRole[]>([]);
  readonly appRoles   = APP_ROLES;
  readonly loading    = signal(false);
  readonly errorMsg   = signal('');
  readonly successMsg = signal('');
  readonly showForm   = signal(false);
  readonly searchTerm = signal('');

  // ── Formulario de creación ─────────────────────────────────────────────────
  readonly form = this.fb.nonNullable.group({
    username:  ['', [Validators.required, Validators.minLength(5)]],
    firstName: ['', Validators.required],
    lastName:  ['', Validators.required],
    email:     ['', Validators.email],
    rol:       ['PACIENTE' as AppRoleName, Validators.required]
  });

  constructor(
    private readonly kcAdmin: KeycloakAdminService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.cargarDatos();
  }

  // ── Carga inicial ──────────────────────────────────────────────────────────
  cargarDatos(): void {
    this.loading.set(true);
    this.errorMsg.set('');

    forkJoin({
      usuarios: this.kcAdmin.listarUsuarios(),
      roles:    this.kcAdmin.listarRoles()
    }).subscribe({
      next: ({ usuarios, roles }) => {
        // Filtrar solo los roles de negocio
        const appRolesFiltrados = roles.filter(r =>
          (APP_ROLES as readonly string[]).includes(r.name)
        );
        this.allRoles.set(appRolesFiltrados);

        // Asociar el rol actual a cada usuario
        const conRoles: UserWithRole[] = usuarios.map(u => ({
          ...u,
          rolActual: undefined
        }));

        // Cargar roles de cada usuario en paralelo (hasta 20 para no sobrecargar)
        const primeros = conRoles.slice(0, 20);
        forkJoin(
          primeros.map(u => this.kcAdmin.obtenerRolesDeUsuario(u.id))
        ).subscribe(rolesLista => {
          rolesLista.forEach((roles, i) => {
            const rolApp = roles.find(r =>
              (APP_ROLES as readonly string[]).includes(r.name)
            );
            primeros[i].rolActual = rolApp?.name;
          });
          this.usuarios.set(conRoles);
          this.loading.set(false);
        });
      },
      error: (err) => {
        this.errorMsg.set('Error al conectar con Keycloak. Verifica que el servidor esté corriendo en puerto 8180 y que tu usuario tenga el rol manage-users.');
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  // ── Búsqueda ───────────────────────────────────────────────────────────────
  buscar(event: Event): void {
    const term = (event.target as HTMLInputElement).value;
    this.searchTerm.set(term);
  }

  get usuariosFiltrados(): UserWithRole[] {
    const term = this.searchTerm().toLowerCase();
    if (!term) return this.usuarios();
    return this.usuarios().filter(u =>
      u.username.toLowerCase().includes(term) ||
      (u.firstName ?? '').toLowerCase().includes(term) ||
      (u.lastName  ?? '').toLowerCase().includes(term)
    );
  }

  // ── Crear usuario ──────────────────────────────────────────────────────────
  toggleForm(): void {
    this.showForm.update(v => !v);
    this.form.reset({ rol: 'PACIENTE' });
    this.successMsg.set('');
    this.errorMsg.set('');
  }

  crearUsuario(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, firstName, lastName, email, rol } = this.form.getRawValue();
    this.loading.set(true);

    const payload: CreateUserPayload = {
      username,
      firstName,
      lastName,
      email: email || undefined,
      enabled: true,
      credentials: [{
        type: 'password',
        value: username,   // Contraseña inicial = número de documento
        temporary: false   // No obliga al usuario a cambiarla
      }]
    };

    this.kcAdmin.crearUsuario(payload).subscribe({
      next: () => {
        // Buscar al usuario recién creado para asignarle el rol
        this.kcAdmin.listarUsuarios(username).subscribe(users => {
          const nuevoUsuario = users.find(u => u.username === username);
          if (nuevoUsuario) {
            const rolObj = this.allRoles().find(r => r.name === rol);
            if (rolObj) {
              this.kcAdmin.asignarRol(nuevoUsuario.id, [rolObj]).subscribe({
                next: () => {
                  this.successMsg.set(`✓ Usuario "${username}" creado con rol ${rol}.`);
                  this.showForm.set(false);
                  this.loading.set(false);
                  this.cargarDatos();
                },
                error: () => {
                  this.successMsg.set(`⚠ Usuario creado pero no se pudo asignar el rol. Asígnalo manualmente.`);
                  this.loading.set(false);
                  this.cargarDatos();
                }
              });
            }
          }
        });
      },
      error: (err) => {
        if (err.status === 409) {
          this.errorMsg.set('Ya existe un usuario con ese número de documento.');
        } else {
          this.errorMsg.set('Error al crear el usuario. Revisa la consola para más detalles.');
        }
        this.loading.set(false);
        console.error(err);
      }
    });
  }

  // ── Cambiar rol ────────────────────────────────────────────────────────────
  cambiarRol(usuario: UserWithRole, nuevoRolNombre: string): void {
    const nuevoRol = this.allRoles().find(r => r.name === nuevoRolNombre);
    if (!nuevoRol) return;

    this.loading.set(true);

    // Primero obtenemos los roles actuales del usuario
    this.kcAdmin.obtenerRolesDeUsuario(usuario.id).subscribe(rolesActuales => {
      const rolesARevocars = rolesActuales.filter(r =>
        (APP_ROLES as readonly string[]).includes(r.name)
      );

      // Función auxiliar que asigna el nuevo rol y actualiza la UI
      const asignar = () => {
        this.kcAdmin.asignarRol(usuario.id, [nuevoRol]).subscribe({
          next: () => {
            usuario.rolActual = nuevoRolNombre;
            this.usuarios.update(list => [...list]);
            this.successMsg.set(`✓ Rol de "${usuario.username}" actualizado a ${nuevoRolNombre}.`);
            this.loading.set(false);
            setTimeout(() => this.successMsg.set(''), 4000);
          },
          error: () => {
            this.errorMsg.set('Error al asignar el nuevo rol.');
            this.loading.set(false);
          }
        });
      };

      if (rolesARevocars.length > 0) {
        // Revocar primero, luego asignar
        this.kcAdmin.revocarRol(usuario.id, rolesARevocars).subscribe({
          next: () => asignar(),
          error: () => {
            this.errorMsg.set('Error al revocar el rol anterior.');
            this.loading.set(false);
          }
        });
      } else {
        // Sin roles previos que revocar — asignar directamente
        asignar();
      }
    });
  }

  // ── Habilitar / Deshabilitar ───────────────────────────────────────────────
  toggleEstado(usuario: UserWithRole): void {
    this.loading.set(true);
    this.kcAdmin.toggleUsuario(usuario.id, !usuario.enabled).subscribe({
      next: () => {
        usuario.enabled = !usuario.enabled;
        this.usuarios.update(list => [...list]);
        this.loading.set(false);
      },
      error: () => {
        this.errorMsg.set('Error al cambiar el estado del usuario.');
        this.loading.set(false);
      }
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────
  getNombreCompleto(u: KeycloakUser): string {
    return `${u.firstName ?? ''} ${u.lastName ?? ''}`.trim() || '—';
  }

  getRolBadgeClass(rol?: string): string {
    const map: Record<string, string> = {
      ADMIN:     'badge badge--admin',
      MEDICO:    'badge badge--medico',
      AGENDADOR: 'badge badge--agendador',
      PACIENTE:  'badge badge--paciente'
    };
    return rol ? (map[rol] ?? 'badge badge--default') : 'badge badge--default';
  }

  isInvalidField(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!(ctrl?.invalid && ctrl.touched);
  }
}
