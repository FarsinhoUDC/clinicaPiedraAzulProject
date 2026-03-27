# Piedrazul — Sistema de Reserva de Citas Médicas

> Proyecto de curso — Ingeniería de Software III · Universidad del Cauca · 2026.1

Sistema web de agendamiento de citas médicas para la clínica Piedrazul. Permite a agendadores registrar citas en nombre de pacientes, a pacientes autogestionar sus citas y al administrador configurar la disponibilidad de los médicos.

---

## Estructura del repositorio

```
/
├── piedrazul-backend/   ← API REST en Spring Boot (Monolito Modular)
└── Frontend/            ← SPA en Angular 17
```

---

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.2.3 · Spring Data JPA · Spring Security Crypto |
| Base de datos (dev) | H2 en memoria |
| Base de datos (prod) | PostgreSQL |
| Frontend | Angular 17 · TypeScript · RxJS |
| Estilos | CSS puro (sin frameworks de UI) |
| Pruebas backend | JUnit 5 · Mockito |
| Pruebas frontend | Jasmine · Karma |

---

## Prerrequisitos

- Java 17+
- Maven 3.8+
- Node.js 18+ y npm 9+
- Angular CLI 17: `npm install -g @angular/cli`

---

## Instalación y ejecución

### Backend

```bash
cd piedrazul-backend
./mvnw spring-boot:run
```

La API quedará disponible en `http://localhost:8080`.

**Consola H2** (base de datos en memoria, solo desarrollo):
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:piedrazuldb`
- Usuario: `sa` · Contraseña: *(vacía)*

### Frontend

```bash
cd Frontend
npm install
ng serve
```

La aplicación quedará disponible en `http://localhost:4200`.

---

## Usuarios de prueba

Al arrancar el backend se crean automáticamente los siguientes usuarios:

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@piedrazul.com` | `admin1234` |
| Médico (agendador) | `carlos.gomez@piedrazul.com` | `1234` |
| Médico (agendador) | `laura.martinez@piedrazul.com` | `1234` |

Los pacientes se registran directamente desde la aplicación web.

---

## Arquitectura del backend

El backend implementa una arquitectura de **Monolito Modular**. Cada módulo es autónomo con sus propias capas de dominio, aplicación e infraestructura.

```
piedrazul-backend/src/main/java/com/piedrazul/
│
├── shared/                      ← Elementos transversales
│   ├── CorsConfig.java          ← Configuración CORS para Angular
│   ├── SecurityConfig.java      ← Bean de BCryptPasswordEncoder
│   ├── DataInitializer.java     ← Datos iniciales de desarrollo
│   ├── exception/               ← BusinessException, ResourceNotFoundException
│   │   └── GlobalExceptionHandler.java
│   └── response/
│       └── ApiResponse.java     ← Wrapper genérico de respuesta
│
├── sesion/                      ← Autenticación y gestión de usuarios
│   ├── domain/
│   │   ├── Usuario.java         ← Entidad base (herencia JOINED)
│   │   └── RolUsuario.java      ← MEDICO | PACIENTE | ADMIN
│   ├── application/
│   │   └── SesionService.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   └── UsuarioResponse.java
│   └── infrastructure/
│       ├── persistence/UsuarioRepository.java
│       └── web/SesionController.java
│
├── medicos/                     ← Gestión del catálogo de médicos
│   ├── domain/Medico.java       ← Extiende Usuario (campos: especialidad)
│   ├── application/MedicoService.java
│   ├── dto/
│   └── infrastructure/
│
├── pacientes/                   ← Gestión de pacientes
│   ├── domain/
│   │   ├── Paciente.java        ← Extiende Usuario (documento, celular, género)
│   │   └── Genero.java          ← HOMBRE | MUJER | OTRO
│   ├── application/PacienteService.java
│   ├── dto/
│   └── infrastructure/
│
├── configuracion/               ← HU-04: Parámetros del sistema
│   ├── domain/
│   │   ├── DisponibilidadMedico.java   ← Días, franja horaria, intervalo
│   │   └── ConfiguracionSistema.java   ← Ventana de semanas habilitadas
│   ├── application/
│   │   ├── ConfiguracionService.java
│   │   └── FranjaHorariaService.java   ← Patrón Strategy
│   ├── dto/
│   └── infrastructure/
│
└── citas/                       ← HU-01 y HU-02: Gestión de citas
    ├── domain/
    │   ├── Cita.java
    │   └── OrigenCita.java      ← AGENDADOR | PACIENTE
    ├── application/
    │   ├── CitaService.java
    │   └── AgendamientoService.java    ← Patrón Facade
    ├── dto/
    └── infrastructure/
```

### Modelo de herencia JPA

```
usuarios (tabla base)
 ├── medicos   (usuario_id FK) ← rol MEDICO
 └── pacientes (usuario_id FK) ← rol PACIENTE

 Los usuarios con rol ADMIN se almacenan solo en la tabla usuarios.
```

---

## Arquitectura del frontend

El frontend implementa una **SPA modular** con Angular 17 standalone components.

```
Frontend/src/app/
│
├── core/
│   ├── api/
│   │   └── api-response.model.ts
│   ├── components/
│   │   └── header/              ← Barra superior
│   ├── factories/
│   │   ├── appointment-summary.factory.ts  ← Patrón Factory
│   │   └── audit-log.factory.ts
│   ├── guards/
│   │   └── auth.guard.ts        ← authGuard · agendadorGuard · pacienteGuard · adminGuard
│   ├── models/                  ← Interfaces TypeScript del dominio
│   ├── services/
│   │   ├── appointment-api.service.ts
│   │   ├── configuration-api.service.ts
│   │   ├── doctor-api.service.ts
│   │   ├── patient-api.service.ts
│   │   ├── session.service.ts   ← Gestión de sesión en localStorage
│   │   └── ui-mappers.service.ts
│   ├── state/
│   │   └── booking-wizard.store.ts   ← Patrón Observer (BehaviorSubject)
│   ├── strategies/
│   │   └── appointment-creation.strategy.ts  ← Patrón Strategy
│   └── utils/
│
└── features/
    ├── admin/
    │   └── availability-config/    ← HU-04: Configuración de disponibilidad
    ├── agendador/
    │   ├── appointment-search/     ← HU-01: Búsqueda de citas
    │   ├── new-appointment-form/   ← HU-02: Creación de citas
    │   └── dashboard/
    ├── authentication/
    │   ├── login/
    │   └── register/
    └── patient/
        ├── patient-portal/         ← HU-03: Autoagendamiento del paciente
        └── patient-registration/
```

---

## Control de acceso por rol

| Rol | Rutas habilitadas | Redirige si no tiene permiso |
|---|---|---|
| `MEDICO` (agendador) | `/agendador/consulta` · `/agendador/nuevaCita` | `/paciente/portal` o `/inicio` |
| `PACIENTE` | `/paciente/portal` | `/agendador/consulta` o `/inicio` |
| `ADMIN` | `/admin/disponibilidad` | `/agendador/consulta` o `/inicio` |

La protección se implementa con route guards funcionales en `auth.guard.ts`.

---

## Endpoints de la API

### Sesión
| Método | URL | Descripción |
|---|---|---|
| POST | `/api/sesion/login` | Iniciar sesión (retorna rol para redirección) |
| GET | `/api/sesion/usuario/{id}` | Consultar datos de usuario |

### Médicos
| Método | URL | Descripción |
|---|---|---|
| GET | `/api/medicos` | Listar médicos activos |
| POST | `/api/medicos` | Crear médico |

### Pacientes
| Método | URL | Descripción |
|---|---|---|
| GET | `/api/pacientes/documento/{numero}` | Buscar paciente por documento |
| POST | `/api/pacientes` | Crear o actualizar paciente |

### Citas
| Método | URL | Descripción |
|---|---|---|
| GET | `/api/citas?medicoId=1&fecha=2026-03-27` | **HU-01** Listar citas por médico y fecha |
| POST | `/api/citas/agendador` | **HU-02** Crear cita desde agendador |
| POST | `/api/citas/paciente` | **HU-03** Crear cita desde paciente |
| GET | `/api/citas/franjas/{medicoId}?fecha=2026-03-27` | Obtener franjas horarias disponibles |
| GET | `/api/citas/{id}` | Detalle de una cita |

### Configuración
| Método | URL | Descripción |
|---|---|---|
| GET | `/api/configuracion` | Listar disponibilidades configuradas |
| POST | `/api/configuracion/disponibilidad` | **HU-04** Configurar disponibilidad de médico |
| POST | `/api/configuracion/sistema` | Configurar ventana de semanas habilitadas |

---

## Pruebas

### Backend

```bash
cd piedrazul-backend
./mvnw test
```

| Clase de prueba | Cobertura |
|---|---|
| `CitaServiceTest` | Guardar cita válida · horario ocupado · listar por médico/fecha |
| `PacienteServiceTest` | Crear/actualizar · buscar por documento · excepción si no existe |
| `FranjaHorariaServiceTest` | Día hábil · día no hábil · hora ocupada · sin configuración |
| `SesionServiceTest` | Login válido · credenciales incorrectas · usuario inactivo |

### Frontend

```bash
cd Frontend
ng test
```

Pruebas en: `SessionService` · `UiMappersService` · `AppointmentSummaryFactory` · `SlotCalculatorUtil` · `LoginComponent` · `RegisterComponent` · `CustomValidators`

---

##  Patrones de diseño implementados

| Patrón | Tipo | Dónde |
|---|---|---|

| **Facade** | Estructural | `AgendamientoService` — orquesta múltiples servicios para crear una cita |
| **Strategy** | Comportamiento | `FranjaHorariaService` (backend) · `AppointmentCreationStrategy` (frontend) |
| **Factory** | Creacional | `AppointmentSummaryFactory` · `AuditLogFactory` (frontend) |
| **Observer** | Comportamiento | `BookingWizardStore` con `BehaviorSubject` (frontend) |

---

## Cambiar a PostgreSQL (producción)

En `piedrazul-backend/src/main/resources/application.properties`, comenta la sección H2 y descomenta la sección PostgreSQL:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/piedrazuldb
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
```

---

## Requisitos funcionales implementados

| HU | Descripción | Estado |
|---|---|---|
| HU-01 | Agendador lista citas de un médico por fecha |  Funcional |
| HU-02 | Agendador crea cita para paciente que contactó por WhatsApp |  Funcional |
| HU-03 | Paciente agenda su cita directamente desde la web |  Funcional |
| HU-04 | Administrador configura disponibilidad de médicos |  Funcional |

---

## Equipo de desarrollo

| Nombre | 
|---|
| [Juan Pablo Medina Bolanios] |
| [Javier Solano] |
| [Cristian Javier Ortega] |
| [Edwin Ordonez Chacon] |

> Universidad del Cauca · Facultad de Ingeniería Electrónica y Telecomunicaciones  
> Programa de Ingeniería de Sistemas · Ingeniería de Software III · 2026.1
