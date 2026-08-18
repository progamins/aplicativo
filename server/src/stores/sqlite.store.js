import { mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { DatabaseSync } from "node:sqlite";

export function createSqliteStore(dbPath, { adminUsernames = [] } = {}) {
  const path = dbPath === ":memory:" ? ":memory:" : resolve(dbPath);
  if (path !== ":memory:") mkdirSync(dirname(path), { recursive: true });

  const db = new DatabaseSync(path);

  db.exec(`
    PRAGMA journal_mode = WAL;

    CREATE TABLE IF NOT EXISTS users (
      id            INTEGER PRIMARY KEY AUTOINCREMENT,
      username      TEXT NOT NULL UNIQUE,
      password_hash TEXT NOT NULL,
      full_name     TEXT NOT NULL DEFAULT '',
      role          TEXT NOT NULL DEFAULT 'estudiante',
      created_at    TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS refresh_tokens (
      id         INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      family_id  TEXT NOT NULL,
      token_hash TEXT NOT NULL UNIQUE,
      expires_at INTEGER NOT NULL,
      revoked_at INTEGER,
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS justificaciones (
      id         INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      motivo     TEXT NOT NULL,
      detalle    TEXT NOT NULL DEFAULT '',
      fecha      TEXT NOT NULL,
      estado     TEXT NOT NULL DEFAULT 'pendiente',
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS asistencias (
      id         INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      fecha      TEXT NOT NULL,
      estado     TEXT NOT NULL,
      curso      TEXT NOT NULL DEFAULT '',
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS user_profiles (
      user_id   INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
      email     TEXT NOT NULL DEFAULT '',
      direccion TEXT NOT NULL DEFAULT '',
      telefono  TEXT NOT NULL DEFAULT ''
    );

    CREATE TABLE IF NOT EXISTS pagos (
      id         INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      concepto   TEXT NOT NULL,
      monto      REAL NOT NULL DEFAULT 0,
      estado     TEXT NOT NULL DEFAULT 'pendiente',
      fecha      TEXT NOT NULL,
      ubicacion  TEXT NOT NULL DEFAULT '',
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS horarios (
      id          INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id     INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      dia         TEXT NOT NULL,
      hora_inicio TEXT NOT NULL,
      hora_fin    TEXT NOT NULL,
      curso       TEXT NOT NULL,
      aula        TEXT NOT NULL DEFAULT '',
      docente     TEXT NOT NULL DEFAULT '',
      created_at  TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS cursos (
      id         INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      nombre     TEXT NOT NULL,
      codigo     TEXT NOT NULL DEFAULT '',
      docente    TEXT NOT NULL DEFAULT '',
      creditos   INTEGER NOT NULL DEFAULT 0,
      estado     TEXT NOT NULL DEFAULT 'en_curso',
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
    CREATE INDEX IF NOT EXISTS idx_justificaciones_user ON justificaciones(user_id);
    CREATE INDEX IF NOT EXISTS idx_asistencias_user ON asistencias(user_id);
    CREATE INDEX IF NOT EXISTS idx_pagos_user ON pagos(user_id);
    CREATE INDEX IF NOT EXISTS idx_horarios_user ON horarios(user_id);
    CREATE INDEX IF NOT EXISTS idx_cursos_user ON cursos(user_id);
  `);

  // Migración: bases creadas antes de la v3.1 no tienen la columna `role`.
  const userCols = db.prepare("PRAGMA table_info(users)").all();
  if (!userCols.some((c) => c.name === "role")) {
    db.exec("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'estudiante'");
  }

  const isAdmin = (username) =>
    // Fuente de verdad: ADMIN_USERNAMES (funciona también para usuarios que se
    // registran después del arranque). El valor de BD es solo un refuerzo.
    adminUsernames.includes(username) || db.prepare("SELECT role FROM users WHERE username = ?").get(username)?.role === "admin";

  const toPublicUser = (row) =>
    row
      ? {
          id: row.id,
          username: row.username,
          fullName: row.full_name ?? "",
          role: isAdmin(row.username) ? "admin" : "estudiante",
          createdAt: row.created_at,
        }
      : null;

  return {
    close() {
      db.close();
    },

    // ---- users ----
    findUserByUsername(username) {
      return db.prepare("SELECT * FROM users WHERE username = ?").get(username) ?? null;
    },

    findUserById(id) {
      return (
        db
          .prepare("SELECT id, username, full_name, role, created_at FROM users WHERE id = ?")
          .get(id) ?? null
      );
    },

    createUser({ username, passwordHash, fullName }) {
      const result = db
        .prepare("INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)")
        .run(username, passwordHash, fullName);
      const id = result.lastInsertRowid;

      // Datos demo de asistencias para que la app se vea viva.
      const iso = (d) => d.toISOString().slice(0, 10);
      const today = new Date();
      [1, 2, 3].forEach((offset) => {
        const d = new Date(today);
        d.setDate(d.getDate() - offset);
        db.prepare(
          "INSERT INTO asistencias (user_id, fecha, estado, curso) VALUES (?, ?, ?, ?)"
        ).run(id, iso(d), offset === 3 ? "tarde" : "presente", "Desarrollo de Software");
      });

      // Datos demo del campus (pagos, horarios y cursos) para que la app se vea viva.
      // Para usar los datos REALES del instituto: conecta la API a la BD MySQL del
      // sistema iestp con DB_DRIVER=mysql y las variables DB_* (ver README); las
      // tablas pagos/horarios/cursos/user_profiles se crean automáticamente y se
      // pueden cargar desde el sistema existente (pagos: concepto, importe, carrera;
      // horarios: se suben como archivos por programa en el sistema iestp).
      const year = new Date().getFullYear();
      const pago = (concepto, monto, estado, ubicacion, fecha) =>
        db
          .prepare(
            "INSERT INTO pagos (user_id, concepto, monto, estado, fecha, ubicacion) VALUES (?, ?, ?, ?, ?, ?)"
          )
          .run(id, concepto, monto, estado, fecha, ubicacion);
      pago("Matrícula 2026-I", 150, "pagado", "Caja principal", `${year}-03-05`);
      pago("Pensión Marzo", 120, "pagado", "Banco de la Nación", `${year}-03-20`);
      pago("Pensión Abril", 120, "pendiente", "Banco de la Nación", `${year}-04-20`);
      pago("Derecho de examen parcial", 25, "pendiente", "Caja principal", `${year}-04-10`);
      pago("Carné estudiantil", 15, "pagado", "Yape / Plin", `${year}-03-12`);

      const horario = (dia, horaInicio, horaFin, curso, aula, docente) =>
        db
          .prepare(
            "INSERT INTO horarios (user_id, dia, hora_inicio, hora_fin, curso, aula, docente) VALUES (?, ?, ?, ?, ?, ?, ?)"
          )
          .run(id, dia, horaInicio, horaFin, curso, aula, docente);
      horario("Lunes", "08:00", "10:00", "Desarrollo de Software II", "Aula A-101", "Ing. C. Mendoza");
      horario("Lunes", "10:15", "12:15", "Base de Datos II", "Lab L-2", "Lic. R. Chávez");
      horario("Martes", "08:00", "10:00", "Diseño Web", "Lab L-3", "Ing. P. Flores");
      horario("Martes", "14:00", "16:00", "Inglés Técnico II", "Aula A-105", "Mg. L. Torres");
      horario("Miércoles", "10:15", "12:15", "Matemática Aplicada", "Aula A-102", "Lic. J. Ramírez");
      horario("Miércoles", "16:00", "18:00", "Desarrollo de Software II", "Lab L-2", "Ing. C. Mendoza");
      horario("Jueves", "08:00", "10:00", "Base de Datos II", "Lab L-1", "Lic. R. Chávez");
      horario("Jueves", "10:15", "12:15", "Diseño Web", "Lab L-3", "Ing. P. Flores");
      horario("Viernes", "08:00", "10:00", "Matemática Aplicada", "Aula A-102", "Lic. J. Ramírez");
      horario("Viernes", "14:00", "16:00", "Inglés Técnico II", "Aula A-105", "Mg. L. Torres");

      const curso = (nombre, codigo, docente, creditos) =>
        db
          .prepare(
            "INSERT INTO cursos (user_id, nombre, codigo, docente, creditos) VALUES (?, ?, ?, ?, ?)"
          )
          .run(id, nombre, codigo, docente, creditos);
      curso("Desarrollo de Software II", "DS-201", "Ing. C. Mendoza", 4);
      curso("Base de Datos II", "BD-202", "Lic. R. Chávez", 4);
      curso("Diseño Web", "DW-203", "Ing. P. Flores", 3);
      curso("Inglés Técnico II", "IN-204", "Mg. L. Torres", 2);
      curso("Matemática Aplicada", "MA-205", "Lic. J. Ramírez", 3);

      return this.findUserById(id);
    },

    toPublicUser,

    // ---- perfil (campus) ----
    getProfile(userId) {
      const row = db
        .prepare("SELECT email, direccion, telefono FROM user_profiles WHERE user_id = ?")
        .get(userId);
      return {
        email: row?.email ?? "",
        direccion: row?.direccion ?? "",
        telefono: row?.telefono ?? "",
      };
    },

    upsertProfile(userId, { email, direccion, telefono }) {
      db.prepare(
        `INSERT INTO user_profiles (user_id, email, direccion, telefono) VALUES (?, ?, ?, ?)
         ON CONFLICT(user_id) DO UPDATE SET
           email = excluded.email,
           direccion = excluded.direccion,
           telefono = excluded.telefono`
      ).run(userId, email, direccion, telefono);
      return this.getProfile(userId);
    },

    // ---- refresh tokens ----
    saveRefreshToken({ userId, familyId, tokenHash, expiresAt }) {
      db.prepare(
        "INSERT INTO refresh_tokens (user_id, family_id, token_hash, expires_at) VALUES (?, ?, ?, ?)"
      ).run(userId, familyId, tokenHash, expiresAt);
    },

    findRefreshToken(tokenHash) {
      return db.prepare("SELECT * FROM refresh_tokens WHERE token_hash = ?").get(tokenHash) ?? null;
    },

    revokeRefreshToken(tokenHash) {
      db.prepare("UPDATE refresh_tokens SET revoked_at = ? WHERE token_hash = ?").run(
        Date.now(),
        tokenHash
      );
    },

    revokeRefreshTokenFamily(familyId) {
      db.prepare("UPDATE refresh_tokens SET revoked_at = ? WHERE family_id = ?").run(
        Date.now(),
        familyId
      );
    },

    // ---- justificaciones ----
    listJustificaciones(userId) {
      return db
        .prepare(
          "SELECT * FROM justificaciones WHERE user_id = ? ORDER BY fecha DESC, id DESC"
        )
        .all(userId);
    },

    createJustificacion({ userId, motivo, fecha, detalle }) {
      const result = db
        .prepare(
          "INSERT INTO justificaciones (user_id, motivo, fecha, detalle) VALUES (?, ?, ?, ?)"
        )
        .run(userId, motivo, fecha, detalle);
      return db.prepare("SELECT * FROM justificaciones WHERE id = ?").get(result.lastInsertRowid);
    },

    // ---- admin ----
    listAllJustificaciones() {
      return db
        .prepare(
          `SELECT j.id, j.user_id, j.motivo, j.detalle, j.fecha, j.estado, j.created_at,
                  u.username, u.full_name AS fullName
           FROM justificaciones j
           JOIN users u ON u.id = j.user_id
           ORDER BY j.created_at DESC, j.id DESC`
        )
        .all();
    },

    setJustificacionEstado(id, estado) {
      const result = db
        .prepare("UPDATE justificaciones SET estado = ? WHERE id = ?")
        .run(estado, id);
      if (result.changes === 0) return null;
      return db.prepare("SELECT * FROM justificaciones WHERE id = ?").get(id);
    },

    // ---- asistencias ----
    listAsistencias(userId) {
      return db
        .prepare("SELECT * FROM asistencias WHERE user_id = ? ORDER BY fecha DESC, id DESC")
        .all(userId);
    },

    // ---- pagos / horarios / cursos (campus) ----
    listPagos(userId) {
      const rows = db
        .prepare("SELECT * FROM pagos WHERE user_id = ? ORDER BY fecha DESC, id DESC")
        .all(userId);
      const ubicaciones = [...new Set(rows.map((r) => r.ubicacion).filter(Boolean))];
      return { pagos: rows, ubicaciones };
    },

    listHorarios(userId) {
      return db
        .prepare(
          `SELECT * FROM horarios WHERE user_id = ?
           ORDER BY CASE dia
             WHEN 'Lunes' THEN 1 WHEN 'Martes' THEN 2 WHEN 'Miércoles' THEN 3
             WHEN 'Jueves' THEN 4 WHEN 'Viernes' THEN 5 WHEN 'Sábado' THEN 6
             ELSE 7 END, hora_inicio`
        )
        .all(userId);
    },

    listCursos(userId) {
      return db
        .prepare("SELECT * FROM cursos WHERE user_id = ? ORDER BY id")
        .all(userId);
    },
  };
}
