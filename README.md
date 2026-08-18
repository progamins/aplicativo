<!--
  ╔══════════════════════════════════════════════════════════════════╗
  ║  APLICATIVO — Login Android + SQL Server                         ║
  ║  App Android de autenticación con conexión directa a SQL Server  ║
  ╚══════════════════════════════════════════════════════════════════╝
-->

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=170&section=header&color=0:1E1B4B,45:6D28D9,80:0E7490,100:0891B2" width="100%" alt=""/>
</p>

<div align="center">
  <h1>📱 Aplicativo Login · Android + SQL Server</h1>
  <p>
    Aplicación Android en <b>Java</b> que autentica usuarios conectándose
    <b>directamente a una base de datos SQL Server</b> mediante JDBC (jTDS).
  </p>
  <p>
    <img src="https://img.shields.io/badge/Android-SDK_34-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android SDK 34"/>
    <img src="https://img.shields.io/badge/Java-8-B07219?style=flat-square&logo=openjdk&logoColor=white" alt="Java 8"/>
    <img src="https://img.shields.io/badge/SQL_Server-jTDS_1.3.1-CC2927?style=flat-square&logo=microsoftsqlserver&logoColor=white" alt="SQL Server"/>
    <img src="https://img.shields.io/badge/Estado-Prototipo_educativo-8B5CF6?style=flat-square" alt="Prototipo"/>
  </p>
</div>

---

## ✨ ¿Qué es?

Proyecto educativo que demuestra cómo una app Android puede conectarse **directamente a SQL Server** sin API intermedia: valida el usuario y la contraseña contra la tabla `Usuarios` de la base de datos `Login` usando el driver **jTDS** (incluye también el driver oficial **MS SQL JDBC**).

- **Problema que explora:** autenticación móvil contra SQL Server vía JDBC puro.
- **Para quién:** estudiantes y desarrolladores que quieren ver la conexión Android ↔ SQL Server de extremo a extremo.

## 🎯 Funcionalidades

| | |
|---|---|
| 🔐 **Login** | Validación de usuario y contraseña contra la tabla `Usuarios` de SQL Server |
| 📝 **Registro** | Pantalla de registro de nuevos usuarios |
| 🏠 **Pantalla principal** | Vista posterior al login correcto |
| 🔌 **Conexión JDBC directa** | Drivers jTDS 1.3.1 y MS SQL JDBC 12.6.3 incluidos en `app/libs` |

## 🛠️ Tech Stack

| Categoría | Tecnología |
|---|---|
| **Lenguaje** | Java 8 (source/target compatibility) |
| **Plataforma** | Android — SDK 34 (minSdk 32) |
| **Build** | Gradle (Kotlin DSL) · Android Gradle Plugin |
| **Base de datos** | SQL Server vía jTDS 1.3.1 / MS SQL JDBC 12.6.3 |
| **UI** | Material Components, ConstraintLayout, AppCompat |

## 🚀 Ejecutar

1. Abre el proyecto en **Android Studio** (deja que Gradle sincronice).
2. Configura la conexión en `app/src/main/java/com/example/login/connetion/ConnetionBD.java`:

   ```java
   private String ip = "192.168.1.6:50531";   // IP:puerto del SQL Server
   private String usuario = "edwin";
   private String password = "1234";
   private String basedatos = "Login";
   ```

3. La base de datos debe tener una tabla `Usuarios` con columnas `NombreUsuario` y `Clave`.
4. Ejecuta en un dispositivo/emulador con **acceso de red** al SQL Server (misma red local o IP alcanzable).

## 📂 Estructura

```text
aplicativo-java/
├── app/
│   ├── libs/                            # Drivers: jtds-1.3.1.jar, mssql-jdbc-12.6.3.jre11.jar
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/example/login/
│       │   ├── LoginActivity.java        # Login contra SQL Server (activity principal)
│       │   ├── RegistrarActivity.java    # Registro de usuarios
│       │   ├── MainActivity.java         # Pantalla principal
│       │   └── connetion/ConnetionBD.java # Conexión JDBC (jTDS)
│       └── res/                          # Layouts, drawables, temas
├── build.gradle.kts · settings.gradle.kts
└── gradle/                              # Wrapper de Gradle
```

## ⚠️ Seguridad — lee esto

Este es un **prototipo educativo** y **no debe usarse en producción** tal como está:

- 🔴 **Credenciales hardcodeadas** en `ConnetionBD.java` (IP, usuario y contraseña).
- 🔴 **Consulta SQL concatenada** (`SELECT * FROM Usuarios WHERE NombreUsuario = '...' AND Clave = '...'`) — vulnerable a **inyección SQL**.
- 🔴 **Contraseñas en texto plano** en la base de datos.
- 🔴 `StrictMode.ThreadPolicy.permitAll()` — desactiva las buenas prácticas de red para permitir JDBC en el hilo principal.

**Recomendaciones:** usa `PreparedStatement`, hashea las contraseñas (bcrypt), externaliza las credenciales y, para producción, sustituye el JDBC directo por una **API intermedia (REST)** que exponga la autenticación de forma segura.

## 🗺️ Roadmap

- [x] Login contra SQL Server vía jTDS
- [x] Pantallas de registro y principal
- [ ] Consultas con `PreparedStatement` (anti inyección SQL)
- [ ] Credenciales en configuración / variables de entorno
- [ ] Hash de contraseñas
- [ ] Migración a API REST intermedia

---

<p align="center"><i>Hecho con 💜 desde Perú · Progamins</i></p>

<p align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=120&section=footer&color=0:1E1B4B,45:6D28D9,80:0E7490,100:0891B2" width="100%" alt=""/>
</p>
