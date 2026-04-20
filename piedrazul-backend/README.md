# Piedrazul Backend — Sistema de Reserva de Citas Médicas

Spring Boot 3.2 · Monolito Modular · Lombok · H2 (dev) / PostgreSQL (prod)

---

## Estructura de módulos

```
com.piedrazul
├── shared/           ← CorsConfig, ApiResponse, excepciones globales
├── sesion/           ← Usuario (base), RolUsuario, SesionService, SesionController
├── medicos/          ← Medico extends Usuario, MedicoService, MedicoController
├── pacientes/        ← Paciente extends Usuario, PacienteService, PacienteController
├── configuracion/    ← DisponibilidadMedico, FranjaHorariaService, ConfiguracionService
└── citas/            ← Cita, CitaService, AgendamientoService (Facade), CitaController
```

### Jerarquía de dominio

`Medico` y `Paciente` extienden de `Usuario` (módulo `sesion`) usando
herencia JPA con estrategia `JOINED`. Esto genera tres tablas:

```
usuarios   → id, nombres, apellidos, correo, contrasena, rol, activo
medicos    → usuario_id (FK), especialidad
pacientes  → usuario_id (FK), numeroDocumento, celular, genero, fechaNacimiento
```

El módulo `sesion` expone `UsuarioRepository` para autenticar a cualquier
tipo de usuario sin depender de los módulos `medicos` o `pacientes`.

---

## Arrancar

```bash
./mvnw spring-boot:run
```

- App: `http://localhost:8080`
- Consola H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:piedrazuldb`
  - User: `sa` / Password: (vacío)

Al arrancar, `DataInitializer` crea dos médicos de prueba con disponibilidad
configurada y credenciales de acceso:

| Correo                          | Contraseña | Especialidad      |
|---------------------------------|------------|-------------------|
| carlos.gomez@piedrazul.com      | 1234       | Medicina General  |
| laura.martinez@piedrazul.com    | 1234       | Fisioterapia      |

---

## Endpoints

### Sesión
| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/sesion/login` | Iniciar sesión (médico o paciente) |
| GET  | `/api/sesion/usuario/{id}` | Consultar datos básicos de un usuario |

### Reportes
| Método | URL | Descripción |
|--------|-----|-------------|
| GET  | `/api/reportes/citas/{medicoId}/{fecha}` | Obtener el reporte de las citas de un medico en una fecha especifica |

### Médicos
| Método | URL | Descripción |
|--------|-----|-------------|
| GET  | `/api/medicos` | Listar médicos activos |
| POST | `/api/medicos` | Registrar nuevo médico |

### Pacientes
| Método | URL | Descripción |
|--------|-----|-------------|
| GET  | `/api/pacientes/documento/{numero}` | Buscar paciente por documento |
| POST | `/api/pacientes` | Crear / actualizar paciente |

### Citas
| Método | URL | Descripción |
|--------|-----|-------------|
| GET  | `/api/citas/{medicoId}/{fecha}` | **HU-01** Listar citas del día |
| POST | `/api/citas/agendador` | **HU-02** Crear cita (agendador) |
| POST | `/api/citas/paciente` | **HU-03** Crear cita (paciente) |
| GET  | `/api/citas/franjas/{medicoId}?fecha=2026-03-27` | Ver franjas disponibles |
| GET  | `/api/citas/{id}` | Detalle de una cita |

### Configuración
| Método | URL | Descripción |
|--------|-----|-------------|
| GET  | `/api/configuracion` | Listar disponibilidades |
| POST | `/api/configuracion/disponibilidad` | Configurar horario médico (**HU-04**) |
| POST | `/api/configuracion/sistema` | Configurar ventana de semanas |

---

## Ejemplos de peticiones

### POST `/api/sesion/login`

```json
{
  "correo": "carlos.gomez@piedrazul.com",
  "contrasena": "1234"
}
```

Respuesta:
```json
{
  "data": {
    "id": 1,
    "nombres": "Carlos",
    "apellidos": "Gomez",
    "correo": "carlos.gomez@piedrazul.com",
    "rol": "MEDICO",
    "activo": true
  }
}
```

### POST `/api/medicos`

```json
{
  "nombres": "Pedro",
  "apellidos": "Ramirez",
  "correo": "pedro.ramirez@piedrazul.com",
  "contrasena": "segura123",
  "especialidad": "Cardiología"
}
```

### POST `/api/pacientes`

```json
{
  "numeroDocumento": "1061000001",
  "nombres": "Ana",
  "apellidos": "Torres",
  "correo": "ana.torres@correo.com",
  "contrasena": "pass456",
  "celular": "3101234567",
  "genero": "MUJER"
}
```

### POST `/api/citas/agendador`

```json
{
  "paciente": {
    "numeroDocumento": "1061000001",
    "nombres": "Ana",
    "apellidos": "Torres",
    "correo": "ana.torres@correo.com",
    "contrasena": "pass456",
    "celular": "3101234567",
    "genero": "MUJER"
  },
  "medicoId": 1,
  "fechaHora": "2026-03-30T08:00:00"
}
```

---

## Pruebas unitarias

```bash
./mvnw test
```

| Clase de prueba | Módulo | Casos cubiertos |
|---|---|---|
| `SesionServiceTest` | sesion | Login correcto (médico y paciente), correo no encontrado, contraseña incorrecta, usuario inactivo, consulta por id |
| `PacienteServiceTest` | pacientes | Crear paciente nuevo, correo duplicado, actualizar existente (sin cambiar credenciales), buscar por documento, not found |
| `CitaServiceTest` | citas | Guardar cita válida, horario ocupado, listar por médico y fecha, lista vacía |
| `FranjaHorariaServiceTest` | configuracion | Día hábil (8 franjas), día no hábil, hora ocupada, médico sin configuración |

> La contraseña se compara en texto plano porque el proyecto aún no integra
> Spring Security. Cuando se agregue, reemplazar la comparación directa en
> `SesionService` por `BCryptPasswordEncoder.matches(raw, encoded)`.

