# Piedrazul Backend — Sistema de Reserva de Citas Médicas

Spring Boot · Monolito Modular · JWT · H2 (dev) / PostgreSQL (prod)

## Arquitectura

```
com.piedrazul
├── shared/                     ← Utilitarios transversales
│   ├── config/                 ← SecurityConfig, JwtConfig, JwtAuthFilter, DataInitializer
│   ├── exception/              ← GlobalExceptionHandler, BusinessException, ResourceNotFoundException
│   └── response/               ← ApiResponse<T>
│
├── usuarios/                   ← Autenticación y roles
│   ├── domain/                 ← Usuario, Rol (enum)
│   ├── application/            ← AuthService, UsuarioService (UserDetailsService)
│   ├── dto/                    ← LoginRequest, LoginResponse, RegistroUsuarioRequest
│   └── infrastructure/
│       ├── persistence/        ← UsuarioRepository
│       └── web/                ← AuthController
│
├── pacientes/                  ← Gestión de pacientes
│   ├── domain/                 ← Paciente, Genero (enum)
│   ├── application/            ← PacienteService
│   ├── dto/                    ← PacienteRequest, PacienteResponse
│   └── infrastructure/
│       ├── persistence/        ← PacienteRepository
│       └── web/                ← PacienteController
│
├── medicos/                    ← Catálogo de médicos/terapistas
│   ├── domain/                 ← Medico
│   ├── application/            ← MedicoService
│   ├── dto/                    ← MedicoRequest, MedicoResponse
│   └── infrastructure/
│       ├── persistence/        ← MedicoRepository
│       └── web/                ← MedicoController
│
├── configuracion/              ← HU-04: Parámetros del sistema
│   ├── domain/                 ← DisponibilidadMedico, ConfiguracionSistema
│   ├── application/            ← ConfiguracionService, FranjaHorariaService (Strategy)
│   ├── dto/                    ← DisponibilidadMedicoRequest/Response, FranjaHorariaResponse
│   └── infrastructure/
│       ├── persistence/        ← DisponibilidadMedicoRepository, ConfiguracionSistemaRepository
│       └── web/                ← ConfiguracionController
│
└── citas/                      ← HU-01 y HU-02: Gestión de citas
    ├── domain/                 ← Cita, OrigenCita (enum)
    ├── application/
    │   ├── CitaService         ← CRUD + validación de duplicados
    │   └── AgendamientoService ← FACADE: orquesta el caso de uso completo
    ├── dto/                    ← CrearCitaRequest, CitaResponse, BuscarCitasRequest
    └── infrastructure/
        ├── persistence/        ← CitaRepository
        └── web/                ← CitaController
```

## Patrones de diseño aplicados

| Patrón | Dónde |
|--------|-------|
| **Repository** | `*Repository` — abstrae el acceso a datos |
| **Service Layer** | `*Service` — toda la lógica de negocio separada del controlador |
| **DTO** | `*Request` / `*Response` — no se exponen entidades en la API |
| **Facade** | `AgendamientoService` — orquesta múltiples servicios |
| **Strategy** | `FranjaHorariaService` — genera franjas según reglas del médico |
| **Factory (Builder)** | Lombok `@Builder` en todas las entidades y DTOs |

## Prerrequisitos

- Java 17
- Maven 3.8+

## Ejecutar en desarrollo

```bash
./mvnw spring-boot:run
```

La app arranca en `http://localhost:8080` con base de datos H2 en memoria.

Consola H2 disponible en: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:piedrazuldb`
- User: `sa` | Password: *(vacío)*

## Credenciales de prueba (cargadas automáticamente)

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN |
| `agendador1` | `agendador123` | AGENDADOR |
| `paciente1` | `paciente123` | PACIENTE |

## Endpoints principales

### Auth
| Método | URL | Descripción |
|--------|-----|-------------|
| POST | `/api/auth/login` | Obtener JWT |
| POST | `/api/auth/registro` | Registrar usuario |

### Citas (HU-01, HU-02, HU-03)
| Método | URL | Roles | Descripción |
|--------|-----|-------|-------------|
| GET | `/api/citas?medicoId=1&fecha=2026-03-27` | AGENDADOR, ADMIN | Listar citas |
| POST | `/api/citas/agendador` | AGENDADOR, ADMIN | Crear cita (agendador) |
| POST | `/api/citas/paciente` | PACIENTE, ADMIN | Crear cita (paciente) |
| GET | `/api/citas/franjas/{medicoId}?fecha=2026-03-27` | Público | Ver franjas disponibles |

### Médicos
| Método | URL | Roles | Descripción |
|--------|-----|-------|-------------|
| GET | `/api/medicos` | Público | Listar médicos activos |
| POST | `/api/medicos` | ADMIN | Crear médico |

### Configuración (HU-04)
| Método | URL | Roles | Descripción |
|--------|-----|-------|-------------|
| GET | `/api/configuracion` | ADMIN | Listar disponibilidades |
| POST | `/api/configuracion/disponibilidad` | ADMIN | Configurar médico |
| POST | `/api/configuracion/sistema` | ADMIN | Configurar ventana de semanas |

## Ejecutar pruebas unitarias

```bash
./mvnw test
```

## Pasar a PostgreSQL (producción)

En `application.properties`, comentar la sección H2 y descomentar la sección PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/piedrazuldb
spring.datasource.username=postgres
spring.datasource.password=secret
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```
