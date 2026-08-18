<!--
  ╔══════════════════════════════════════════════════════════════════╗
  ║  APLICATIVO LOGIN — Android (Kotlin) + API REST                  ║
  ║  App Android + backend Node.js/Express con SQLite/MySQL + Docker ║
  ╚══════════════════════════════════════════════════════════════════╝
-->

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=170&section=header&color=0:1E1B4B,45:6D28D9,80:0E7490,100:0891B2" width="100%" alt=""/>
</p>

<div align="center">
  <h1>📱 Aplicativo Login · Android + API REST</h1>
  <p>
    Aplicación Android en <b>Kotlin + Jetpack Compose</b> que autentica usuarios contra una
    <b>API REST propia</b> (Node.js + Express) con <b>JWT + refresh tokens</b>,
    lista para <b>Docker</b> y compatible con la base de datos del sistema académico.
  </p>
  <p>
    <img src="https://img.shields.io/badge/Android-SDK_34-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android SDK 34"/>
    <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.0"/>
    <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
    <img src="https://img.shields.io/badge/Node.js-24-339933?style=flat-square&logo=nodedotjs&logoColor=white" alt="Node.js 24"/>
    <img src="https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker"/>
    <img src="https://img.shields.io/badge/BD-SQLite_%7C_MySQL-003B57?style=flat-square" alt="SQLite / MySQL"/>
    <img src="https://img.shields.io/badge/Auth-JWT_%2B_refresh_tokens-F59E0B?style=flat-square" alt="JWT + refresh tokens"/>
    <img src="https://img.shields.io/badge/Tests-15_pasando-22C55E?style=flat-square" alt="Tests"/>
  </p>
</div>

---

## ✨ ¿Qué es?

Sistema de autenticación completo de extremo a extremo con **arquitectura moderna** y **seguridad profesional**:

- **App Android** en **Kotlin + Jetpack Compose (Material 3)**, arquitectura **MVVM** (ViewModel + StateFlow), networking con **Retrofit + OkHttp** con **renovación automática del token** (refresh en 401).
- **API REST** en **Node.js + Express** con **JWT de corta duración + refresh tokens rotativos**, contraseñas con **bcrypt**, **rate limiting**, validación **Zod**, logging estructurado con **pino** y consultas **parametrizadas**.
- **Docker listo**: imagen multi-stage con usuario no root, `docker-compose` con healthcheck y perfil MySQL opcional.
- **Compatible con el sistema académico (iestp)**: la API puede autenticar contra la tabla `listado_usuarios` de MySQL (los hashes bcrypt de PHP se verifican con bcryptjs).

## 🏗️ Arquitectura

```text
┌────────────────────────┐        HTTP / JSON         ┌────────────────────────────┐
│   App Android          │  ────────────────────────► │   API REST (server/)       │
│   Kotlin · Compose     │   access token (Bearer)    │   Node.js · Express        │
│   Retrofit · MVVM      │  ◄──────────────────────── │   SQLite / MySQL · pino    │
│   refresh automático   │   refresh tokens rotativos │   JWT · bcrypt · zod       │
└────────────────────────┘                            └────────────────────────────┘
```

## 🚀 Correr la API en local

Requisitos: **Node.js ≥ 22.5** (usa `node:sqlite` integrado, sin dependencias nativas).

```bash
cd server
npm install
npm start        # o: npm run dev (recarga automática)
npm test         # 15 tests de integración
```

La API queda en `http://localhost:3000` (escucha en `0.0.0.0`).

## 🐳 Docker

```bash
cd aplicativo-java          # raíz del repo
cp .env.example .env        # edita JWT_SECRET
docker compose up -d        # API en http://localhost:3000
docker compose ps           # ver estado (healthcheck incluido)
```

Con **MySQL** (perfil opcional, para el sistema académico):

```bash
docker compose --profile mysql up -d
# ajusta DB_DRIVER=mysql y DB_* en .env, e importa el esquema de iestp
```

La imagen corre con **usuario no root** (`node`), multi-stage (`npm ci --omit=dev`) y expone `HEALTHCHECK` sobre `/api/health`.

## 🏫 Compatibilidad con el sistema académico (iestp)

El sistema de gestión académica usa **PHP + MySQL** con la tabla `listado_usuarios` (`email`, `password`, `role_id`), donde las contraseñas son hashes **bcrypt** generados por `password_hash()` de PHP — **bcryptjs los verifica directamente**.

Para que la API autentique a los usuarios del instituto:

```bash
# server/.env (o .env de compose)
DB_DRIVER=mysql
DB_HOST=localhost          # o "mysql" si usas compose
DB_PORT=3306
DB_USER=root
DB_PASSWORD=...
DB_NAME=iestp
DB_TABLE=listado_usuarios
DB_USERNAME_COL=email
DB_PASSWORD_COL=password
ENABLE_REGISTER=false      # los usuarios los gestiona el sistema académico
```

Con `ENABLE_REGISTER=false` el registro queda deshabilitado (403) y la API solo **autentica** contra la BD existente, sin modificarla (solo crea su tabla de refresh tokens).

## 📱 Compilar y ejecutar la app Android

1. Abre el proyecto en **Android Studio**.
2. Con la API corriendo, ejecuta la app:
   - **Emulador**: `http://10.0.2.2:3000/` (ya configurada por defecto en `app/build.gradle.kts`).
   - **Dispositivo físico**: cambia `API_BASE_URL` por la IP local de tu PC, ej. `http://192.168.1.10:3000/`.
3. Regístrate y entra. La app **renueva el token automáticamente** y valida la sesión al abrir.

## 📚 Endpoints de la API

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/health` | Estado del servicio (versión + driver BD) | — |
| `POST` | `/api/auth/register` | Crear cuenta → `{ accessToken, refreshToken, user }` | — |
| `POST` | `/api/auth/login` | Iniciar sesión → `{ accessToken, refreshToken, user }` | — |
| `POST` | `/api/auth/refresh` | Rotar refresh token → nuevo par de tokens | refresh token |
| `POST` | `/api/auth/logout` | Revocar refresh token (204) | refresh token |
| `GET` | `/api/auth/me` | Perfil del usuario autenticado | `Bearer <accessToken>` |
| `GET` | `/api/justificaciones` | Justificaciones del usuario | `Bearer` |
| `POST` | `/api/justificaciones` | Crear justificación `{ motivo, fecha, detalle? }` | `Bearer` |
| `GET` | `/api/asistencias` | Historial de asistencias | `Bearer` |
| `GET` | `/api/estadisticas` | Resumen (justificaciones, pendientes, asistencias) | `Bearer` |
| `GET` | `/api/admin/justificaciones` | **Opcional (futura web):** todas las justificaciones con datos del estudiante | `Bearer` + rol admin |
| `PATCH` | `/api/admin/justificaciones/:id` | **Opcional (futura web):** aprobar/rechazar `{ "estado": "aprobada" | "rechazada" }` | `Bearer` + rol admin |

## 🎓 Centrada en el estudiante

La app Android es **100% para estudiantes**: inicio con resumen, justificaciones, asistencias y perfil — sin nada de administración.

El backend conserva endpoints de administración (`/api/admin/*`, protegidos con rol `admin` declarado vía `ADMIN_USERNAMES`) **opcionales**, pensados para un futuro panel web del instituto. La app no los usa ni los muestra.

## 🔐 Seguridad

- **Refresh tokens rotativos** con detección de **reutilización** (si un token ya rotado se usa de nuevo, se revoca toda la familia por posible robo). Guardados **hasheados (SHA-256)** en BD.
- **Access token de corta duración** (`15m` por defecto), firmado con `issuer`/`audience`, secreto por entorno con **abortado en producción** si no está definido.
- **bcrypt** para contraseñas + **igualación de tiempos** contra un hash ficticio (anti enumeración de usuarios).
- **Rate limiting**: 5 intentos de login por usuario+IP cada 15 min.
- **Consultas parametrizadas** (prepared statements) — sin inyección SQL.
- **Helmet** (CSP, HSTS, X-Content-Type-Options…) + **CORS configurable** + límite de cuerpo de 32 KB.
- **Logging estructurado con pino** y redacción de cabeceras sensibles (Authorization).
- **Graceful shutdown** (SIGTERM/SIGINT cierra servidor y BD).

## 🛠️ Tech Stack

| Capa | Tecnologías |
|---|---|
| **App Android** | Kotlin 2.0 · Jetpack Compose (Material 3) · Retrofit 2 · OkHttp 4 · kotlinx-serialization · ViewModel + StateFlow · Tabs de estudiante (Inicio, Justificaciones, Asistencias, Perfil) |
| **API** | Node.js 24 · Express 4 · SQLite (`node:sqlite`) / MySQL (`mysql2`) · bcryptjs · jsonwebtoken · express-rate-limit · zod · helmet · pino · compression |
| **Tests** | `node:test` + supertest (30 tests: auth, académico y admin) |
| **Ops** | Docker multi-stage no-root · docker-compose (perfil MySQL) · Gradle 8.7 + AGP 8.5 (version catalog) |

## 📂 Estructura

```text
aplicativo-java/
├── app/                                # App Android (Kotlin + Compose)
│   └── src/main/java/com/example/login/
│       ├── MainActivity.kt · App.kt
│       ├── data/
│       │   ├── SessionManager.kt       # access + refresh token persistente
│       │   ├── model/Models.kt         # DTOs (kotlinx-serialization)
│       │   └── remote/                 # Retrofit + interceptor de refresh
│       └── ui/                         # AuthViewModel + Login/Register/Home/Justificaciones/Asistencias/Perfil
├── server/                             # API REST
│   ├── src/
│   │   ├── index.js · app.js · config.js · db.js · tokens.js · logger.js
│   │   ├── controllers/ · routes/ · middleware/   # incluye admin.routes.js
│   │   └── stores/                     # sqlite.store.js · mysql.store.js
│   ├── test/                           # auth, academic y admin (30 tests)
│   ├── Dockerfile · .dockerignore
│   └── package.json
├── docker-compose.yml                  # API + perfil MySQL (iestp)
├── .env.example                        # variables para compose
└── gradle/libs.versions.toml
```

## 🗺️ Roadmap

### ✅ Hecho
- [x] API REST con JWT + refresh tokens rotativos y detección de reutilización
- [x] App en Kotlin + Compose + Retrofit (MVVM) con refresh automático
- [x] Rate limiting, validación Zod, logging pino, graceful shutdown
- [x] Adaptador **MySQL** compatible con `listado_usuarios` de iestp
- [x] Docker multi-stage + compose con perfil MySQL
- [x] 15 tests de integración

### 🔜 Próximos pasos
- [ ] Pantalla de perfil con `fullName` en la app
- [ ] Tests de la app Android (JVM + instrumentados)
- [ ] CI con GitHub Actions (test + build + docker)
- [ ] Vincular roles de iestp (`role_id`) en la respuesta de login

---

<p align="center"><i>Hecho con 💜 desde Perú · Progamins</i></p>

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=120&section=footer&color=0:1E1B4B,45:6D28D9,80:0E7490,100:0891B2" width="100%" alt=""/>
</p>
