<!--
  ╔══════════════════════════════════════════════════════════════════╗
  ║  APLICATIVO LOGIN — Android (Kotlin) + API REST                  ║
  ║  App Android moderna con backend Node.js + Express + SQLite      ║
  ╚══════════════════════════════════════════════════════════════════╝
-->

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=170&section=header&color=0:1E1B4B,45:6D28D9,80:0E7490,100:0891B2" width="100%" alt=""/>
</p>

<div align="center">
  <h1>📱 Aplicativo Login · Android + API REST</h1>
  <p>
    Aplicación Android en <b>Kotlin + Jetpack Compose</b> que autentica usuarios contra una
    <b>API REST propia</b> (Node.js + Express + SQLite) con <b>JWT</b>.
  </p>
  <p>
    <img src="https://img.shields.io/badge/Android-SDK_34-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android SDK 34"/>
    <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin 2.0"/>
    <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
    <img src="https://img.shields.io/badge/Node.js-24-339933?style=flat-square&logo=nodedotjs&logoColor=white" alt="Node.js 24"/>
    <img src="https://img.shields.io/badge/Express-4-000000?style=flat-square&logo=express&logoColor=white" alt="Express"/>
    <img src="https://img.shields.io/badge/Base_de_datos-SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white" alt="SQLite"/>
    <img src="https://img.shields.io/badge/Auth-JWT_%2B_bcrypt-F59E0B?style=flat-square" alt="JWT + bcrypt"/>
  </p>
</div>

---

## ✨ ¿Qué es?

Sistema de autenticación completo de extremo a extremo con **arquitectura moderna**:

- **App Android** escrita en **Kotlin** con **Jetpack Compose (Material 3)**, arquitectura **MVVM** (ViewModel + StateFlow), networking con **Retrofit + OkHttp** y sesión persistente con JWT.
- **API REST** en **Node.js + Express + SQLite** que reemplaza la antigua conexión JDBC directa a SQL Server: contraseñas con **bcrypt**, sesiones con **JWT**, **rate limiting**, validación con **Zod** y consultas **parametrizadas**.

> 🆕 **v2.0**: el proyecto pasó de una app Android con JDBC directo (credenciales hardcodeadas e inyección SQL) a una arquitectura cliente-servidor segura y moderna.

## 🏗️ Arquitectura

```text
┌────────────────────────┐        HTTP / JSON         ┌─────────────────────────┐
│   App Android          │  ────────────────────────► │   API REST (server/)    │
│   Kotlin · Compose     │                            │   Node.js · Express     │
│   Retrofit · MVVM      │  ◄──────────────────────── │   SQLite · JWT · bcrypt │
└────────────────────────┘      token JWT (Bearer)    └─────────────────────────┘
```

## 🚀 Correr la API en local

Requisitos: **Node.js ≥ 22.5** (usa `node:sqlite` integrado, sin dependencias nativas).

```bash
cd server
npm install
npm start        # o: npm run dev (recarga automática)
```

La API queda en `http://localhost:3000` (escucha en `0.0.0.0`, accesible desde tu teléfono en la misma red).

Prueba rápida:

```bash
# Registro
curl -X POST http://localhost:3000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"edwin","password":"secreto123","fullName":"Edwin"}'

# Login (devuelve el token JWT)
curl -X POST http://localhost:3000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"edwin","password":"secreto123"}'
```

Configuración por variables de entorno (copia `server/.env.example` → `server/.env`):

| Variable | Descripción |
|---|---|
| `PORT` | Puerto (por defecto `3000`) |
| `HOST` | Interfaz de escucha (por defecto `0.0.0.0`) |
| `JWT_SECRET` | Secreto para firmar tokens — **cámbialo en producción** |
| `JWT_EXPIRES_IN` | Vigencia del token (por defecto `24h`) |
| `DB_PATH` | Ruta del archivo SQLite (por defecto `./data/app.db`) |
| `BCRYPT_ROUNDS` | Coste del hash (por defecto `10`) |

## 📱 Compilar y ejecutar la app Android

1. Abre el proyecto en **Android Studio** (Giraffe o superior; usa el wrapper incluido).
2. Con la API corriendo, ejecuta la app:
   - **Emulador**: la URL `http://10.0.2.2:3000/` ya apunta al `localhost` de tu PC (configurada por defecto en `app/build.gradle.kts`).
   - **Dispositivo físico**: cambia `API_BASE_URL` en `app/build.gradle.kts` por la IP local de tu PC en la misma red, ej. `http://192.168.1.10:3000/`.
3. Regístrate y entra. 🎉

## 📚 Endpoints de la API

| Método | Ruta | Descripción | Auth |
|---|---|---|---|
| `GET` | `/api/health` | Estado del servicio | — |
| `POST` | `/api/auth/register` | Crear cuenta → `{ token, user }` | — |
| `POST` | `/api/auth/login` | Iniciar sesión → `{ token, user }` | — |
| `GET` | `/api/auth/me` | Perfil del usuario autenticado | `Bearer <token>` |

## 🔐 Seguridad

- **bcrypt** para hashear contraseñas (nunca se guardan en texto plano).
- **JWT** firmado con secreto de entorno, expiración configurable.
- **Rate limiting** en login y registro (anti fuerza bruta).
- **Consultas parametrizadas** (`node:sqlite` prepared statements) — sin inyección SQL.
- **Validación de entrada** con Zod.
- **Helmet + CORS** en Express.
- El token JWT viaja en el header `Authorization: Bearer …`.

## 🛠️ Tech Stack

| Capa | Tecnologías |
|---|---|
| **App Android** | Kotlin 2.0 · Jetpack Compose (Material 3) · Retrofit 2 · OkHttp 4 · kotlinx-serialization · ViewModel + StateFlow |
| **API** | Node.js 24 · Express 4 · node:sqlite (SQLite) · bcryptjs · jsonwebtoken · express-rate-limit · zod · helmet |
| **Build** | Gradle 8.7 (Kotlin DSL) · AGP 8.5 · version catalog (`libs.versions.toml`) |

## 📂 Estructura

```text
aplicativo-java/
├── app/                                # App Android (Kotlin + Compose)
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/login/
│           ├── MainActivity.kt         # Navegación login/registro/home
│           ├── App.kt                  # Application (inicializa sesión)
│           ├── data/
│           │   ├── SessionManager.kt   # Token JWT persistente
│           │   ├── model/Models.kt     # DTOs (kotlinx-serialization)
│           │   └── remote/             # Retrofit: ApiService + ApiClient
│           └── ui/
│               ├── AuthViewModel.kt    # MVVM: StateFlow + corrutinas
│               ├── LoginScreen.kt · RegisterScreen.kt · HomeScreen.kt
│               └── theme/Theme.kt      # Material 3 dark
├── server/                             # API REST (Node.js + Express)
│   ├── src/
│   │   ├── index.js · app.js · config.js · db.js
│   │   ├── routes/auth.routes.js
│   │   ├── controllers/auth.controller.js
│   │   └── middleware/auth.js          # Verificación JWT
│   ├── .env.example                    # Plantilla de configuración
│   └── package.json
├── build.gradle.kts · settings.gradle.kts
└── gradle/libs.versions.toml
```

## 🗺️ Roadmap

### ✅ v2.0 — Hecho
- [x] API REST propia (Express + SQLite + JWT + bcrypt + rate limiting)
- [x] App migrada de Java + JDBC directo a **Kotlin + Compose + Retrofit**
- [x] Arquitectura MVVM (ViewModel + StateFlow + corrutinas)
- [x] Sesión persistente con JWT
- [x] Validación de entrada y consultas parametrizadas

### 🔜 Próximos pasos
- [ ] `GET /api/auth/me` en la app (validar sesión al iniciar)
- [ ] Pantalla de perfil con `fullName`
- [ ] Tests automatizados (API y app)
- [ ] Migración de SQLite a MySQL/SQL Server según entorno
- [ ] CI con GitHub Actions

---

<p align="center"><i>Hecho con 💜 desde Perú · Progamins</i></p>

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=120&section=footer&color=0:1E1B4B,45:6D28D9,80:0E7490,100:0891B2" width="100%" alt=""/>
</p>
