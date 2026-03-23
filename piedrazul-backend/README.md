# Piedrazul Backend — Sistema de Reserva de Citas Médicas

Spring Boot 3.2 · Monolito Modular · Lombok · H2 (dev) / PostgreSQL (prod)

> **Sin autenticación** — versión de desarrollo para construcción del frontend Angular.

## Estructura

```
com.piedrazul
├── shared/                     ← CorsConfig, ApiResponse, excepciones
├── pacientes/                  ← Paciente, PacienteService, PacienteController
├── medicos/                    ← Medico, MedicoService, MedicoController
├── configuracion/              ← DisponibilidadMedico, FranjaHorariaService (Strategy), ConfiguracionService
└── citas/                      ← Cita, CitaService, AgendamientoService (Facade), CitaController
```

## Arrancar

```bash
./mvnw spring-boot:run
```

- App: `http://localhost:8080`
- Consola H2: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:piedrazuldb`
  - User: `sa` | Password: (vacío)

Al arrancar se crean automáticamente dos médicos de prueba con su disponibilidad.

## Endpoints

### Médicos
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/medicos` | Listar médicos activos |
| POST | `/api/medicos` | Crear médico |

### Pacientes
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/pacientes/documento/{numero}` | Buscar paciente por documento |
| POST | `/api/pacientes` | Crear / actualizar paciente |

### Citas
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/citas?medicoId=1&fecha=2026-03-27` | **HU-01** Listar citas |
| POST | `/api/citas/agendador` | **HU-02** Crear cita (agendador) |
| POST | `/api/citas/paciente` | **HU-03** Crear cita (paciente) |
| GET | `/api/citas/franjas/1?fecha=2026-03-27` | Ver franjas disponibles |
| GET | `/api/citas/{id}` | Detalle de una cita |

### Configuración
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/configuracion` | Listar disponibilidades |
| POST | `/api/configuracion/disponibilidad` | Configurar médico (**HU-04**) |
| POST | `/api/configuracion/sistema` | Configurar ventana de semanas |

## Ejemplo POST /api/citas/agendador

```json
{
  "paciente": {
    "numeroDocumento": "1061000001",
    "nombres": "Ana",
    "apellidos": "Torres",
    "celular": "3101234567",
    "genero": "MUJER"
  },
  "medicoId": 1,
  "fechaHora": "2026-03-25T08:00:00"
}
```

## Ejecutar pruebas

```bash
./mvnw test
```
