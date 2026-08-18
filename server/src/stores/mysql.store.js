import mysql from "mysql2/promise";

/**
 * Store MySQL configurable para autenticar contra una base de datos existente,
 * por ejemplo `listado_usuarios` del sistema académico (iestp):
 *   DB_TABLE=listado_usuarios  DB_USERNAME_COL=email  DB_PASSWORD_COL=password
 * Los hashes de PHP `password_hash()` (bcrypt) son verificables con bcryptjs.
 * Las tablas de justificaciones/asistencias son propias de la app.
 */
export function createMysqlStore(cfg, { adminUsernames = [] } = {}) {
  if (!cfg.database) {
    throw new Error("DB_DRIVER=mysql requiere DB_NAME (base de datos)");
  }

  const isAdmin = (username) => adminUsernames.includes(username);

  let pool;

  const q = (sql) => {
    if (!pool) {
      pool = mysql.createPool({
        host: cfg.host,
        port: cfg.port,
        user: cfg.user,
        password: cfg.password,
        database: cfg.database,
        waitForConnections: true,
        connectionLimit: 5,
      });
    }
    return pool;
  };

  const refreshTable = cfg.refreshTable.replace(/[^a-zA-Z0-9_]/g, "");
  const clean = (name) => name.replace(/[^a-zA-Z0-9_]/g, "");
  const justTable = clean(cfg.justificacionesTable || "justificaciones");
  const asistTable = clean(cfg.asistenciasTable || "asistencias");
  const profileTable = clean(cfg.profileTable || "user_profiles");
  const pagosTable = clean(cfg.pagosTable || "pagos");
  const horariosTable = clean(cfg.horariosTable || "horarios");
  const cursosTable = clean(cfg.cursosTable || "cursos");

  return {
    async init() {
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${refreshTable}\` (
          id INT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          family_id VARCHAR(64) NOT NULL,
          token_hash VARCHAR(64) NOT NULL UNIQUE,
          expires_at BIGINT NOT NULL,
          revoked_at BIGINT NULL,
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          INDEX idx_refresh_user (user_id)
        )
      `);
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${justTable}\` (
          id INT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          motivo VARCHAR(100) NOT NULL,
          detalle TEXT NOT NULL,
          fecha VARCHAR(10) NOT NULL,
          estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          INDEX idx_just_user (user_id)
        )
      `);
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${asistTable}\` (
          id INT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          fecha VARCHAR(10) NOT NULL,
          estado VARCHAR(20) NOT NULL,
          curso VARCHAR(100) NOT NULL DEFAULT '',
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          INDEX idx_asist_user (user_id)
        )
      `);
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${profileTable}\` (
          user_id BIGINT PRIMARY KEY,
          email VARCHAR(120) NOT NULL DEFAULT '',
          direccion VARCHAR(200) NOT NULL DEFAULT '',
          telefono VARCHAR(30) NOT NULL DEFAULT ''
        )
      `);
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${pagosTable}\` (
          id INT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          concepto VARCHAR(100) NOT NULL,
          monto DECIMAL(10,2) NOT NULL DEFAULT 0,
          estado VARCHAR(20) NOT NULL DEFAULT 'pendiente',
          fecha VARCHAR(10) NOT NULL,
          ubicacion VARCHAR(100) NOT NULL DEFAULT '',
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          INDEX idx_pagos_user (user_id)
        )
      `);
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${horariosTable}\` (
          id INT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          dia VARCHAR(20) NOT NULL,
          hora_inicio VARCHAR(5) NOT NULL,
          hora_fin VARCHAR(5) NOT NULL,
          curso VARCHAR(100) NOT NULL,
          aula VARCHAR(30) NOT NULL DEFAULT '',
          docente VARCHAR(100) NOT NULL DEFAULT '',
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          INDEX idx_horarios_user (user_id)
        )
      `);
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${cursosTable}\` (
          id INT AUTO_INCREMENT PRIMARY KEY,
          user_id BIGINT NOT NULL,
          nombre VARCHAR(100) NOT NULL,
          codigo VARCHAR(20) NOT NULL DEFAULT '',
          docente VARCHAR(100) NOT NULL DEFAULT '',
          creditos INT NOT NULL DEFAULT 0,
          estado VARCHAR(20) NOT NULL DEFAULT 'en_curso',
          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
          INDEX idx_cursos_user (user_id)
        )
      `);
    },

    async close() {
      if (pool) await pool.end();
    },

    // ---- users ----
    async findUserByUsername(username) {
      const [rows] = await q().query(
        `SELECT \`${cfg.idColumn}\` AS id, \`${cfg.usernameColumn}\` AS username, \`${cfg.passwordColumn}\` AS password_hash ${
          cfg.fullNameColumn ? `, \`${cfg.fullNameColumn}\` AS full_name` : ""
        } FROM \`${cfg.table}\` WHERE \`${cfg.usernameColumn}\` = ? LIMIT 1`,
        [username]
      );
      return rows[0] ?? null;
    },

    async findUserById(id) {
      const [rows] = await q().query(
        `SELECT \`${cfg.idColumn}\` AS id, \`${cfg.usernameColumn}\` AS username ${
          cfg.fullNameColumn ? `, \`${cfg.fullNameColumn}\` AS full_name` : ""
        } FROM \`${cfg.table}\` WHERE \`${cfg.idColumn}\` = ? LIMIT 1`,
        [id]
      );
      return rows[0] ?? null;
    },

    createUser() {
      // En modo MySQL externo (p. ej. iestp) el registro está deshabilitado:
      // los usuarios los gestiona el sistema académico.
      throw new Error("Registro no disponible en modo MySQL externo");
    },

    toPublicUser(row) {
      return row
        ? {
            id: row.id,
            username: row.username,
            fullName: row.full_name ?? "",
            // En modo MySQL externo el rol se calcula por configuración
            // (ADMIN_USERNAMES) sin modificar la base del instituto.
            role: isAdmin(row.username) ? "admin" : "estudiante",
            createdAt: null,
          }
        : null;
    },

    // ---- refresh tokens ----
    async saveRefreshToken({ userId, familyId, tokenHash, expiresAt }) {
      await q().query(
        `INSERT INTO \`${refreshTable}\` (user_id, family_id, token_hash, expires_at) VALUES (?, ?, ?, ?)`,
        [userId, familyId, tokenHash, expiresAt]
      );
    },

    async findRefreshToken(tokenHash) {
      const [rows] = await q().query(
        `SELECT * FROM \`${refreshTable}\` WHERE token_hash = ? LIMIT 1`,
        [tokenHash]
      );
      return rows[0] ?? null;
    },

    async revokeRefreshToken(tokenHash) {
      await q().query(`UPDATE \`${refreshTable}\` SET revoked_at = ? WHERE token_hash = ?`, [
        Date.now(),
        tokenHash,
      ]);
    },

    async revokeRefreshTokenFamily(familyId) {
      await q().query(`UPDATE \`${refreshTable}\` SET revoked_at = ? WHERE family_id = ?`, [
        Date.now(),
        familyId,
      ]);
    },

    // ---- justificaciones ----
    async listJustificaciones(userId) {
      const [rows] = await q().query(
        `SELECT * FROM \`${justTable}\` WHERE user_id = ? ORDER BY fecha DESC, id DESC`,
        [userId]
      );
      return rows;
    },

    async createJustificacion({ userId, motivo, fecha, detalle }) {
      const [result] = await q().query(
        `INSERT INTO \`${justTable}\` (user_id, motivo, fecha, detalle) VALUES (?, ?, ?, ?)`,
        [userId, motivo, fecha, detalle]
      );
      const [rows] = await q().query(`SELECT * FROM \`${justTable}\` WHERE id = ?`, [
        result.insertId,
      ]);
      return rows[0] ?? null;
    },

    // ---- admin ----
    async listAllJustificaciones() {
      const userTable = clean(cfg.table);
      const idCol = clean(cfg.idColumn);
      const usernameCol = clean(cfg.usernameColumn);
      const fullNameCol = cfg.fullNameColumn ? clean(cfg.fullNameColumn) : null;
      const [rows] = await q().query(
        `SELECT j.*, u.\`${usernameCol}\` AS username ${
          fullNameCol ? `, u.\`${fullNameCol}\` AS fullName` : ""
        }
         FROM \`${justTable}\` j
         JOIN \`${userTable}\` u ON u.\`${idCol}\` = j.user_id
         ORDER BY j.created_at DESC, j.id DESC`
      );
      return rows;
    },

    async setJustificacionEstado(id, estado) {
      const [result] = await q().query(
        `UPDATE \`${justTable}\` SET estado = ? WHERE id = ?`,
        [estado, id]
      );
      if (result.affectedRows === 0) return null;
      const [rows] = await q().query(`SELECT * FROM \`${justTable}\` WHERE id = ?`, [id]);
      return rows[0] ?? null;
    },

    // ---- asistencias ----
    async listAsistencias(userId) {
      const [rows] = await q().query(
        `SELECT * FROM \`${asistTable}\` WHERE user_id = ? ORDER BY fecha DESC, id DESC`,
        [userId]
      );
      return rows;
    },

    // ---- perfil (campus) ----
    async getProfile(userId) {
      const [rows] = await q().query(
        `SELECT email, direccion, telefono FROM \`${profileTable}\` WHERE user_id = ?`,
        [userId]
      );
      const r = rows[0];
      return {
        email: r?.email ?? "",
        direccion: r?.direccion ?? "",
        telefono: r?.telefono ?? "",
      };
    },

    async upsertProfile(userId, { email, direccion, telefono }) {
      await q().query(
        `INSERT INTO \`${profileTable}\` (user_id, email, direccion, telefono) VALUES (?, ?, ?, ?)
         ON DUPLICATE KEY UPDATE
           email = VALUES(email),
           direccion = VALUES(direccion),
           telefono = VALUES(telefono)`,
        [userId, email, direccion, telefono]
      );
      return this.getProfile(userId);
    },

    // ---- pagos / horarios / cursos (campus) ----
    async listPagos(userId) {
      const [rows] = await q().query(
        `SELECT * FROM \`${pagosTable}\` WHERE user_id = ? ORDER BY fecha DESC, id DESC`,
        [userId]
      );
      const ubicaciones = [...new Set(rows.map((r) => r.ubicacion).filter(Boolean))];
      return { pagos: rows, ubicaciones };
    },

    async listHorarios(userId) {
      const [rows] = await q().query(
        `SELECT * FROM \`${horariosTable}\` WHERE user_id = ?
         ORDER BY FIELD(dia, 'Lunes','Martes','Miércoles','Jueves','Viernes','Sábado','Domingo'), hora_inicio`,
        [userId]
      );
      return rows;
    },

    async listCursos(userId) {
      const [rows] = await q().query(
        `SELECT * FROM \`${cursosTable}\` WHERE user_id = ? ORDER BY id`,
        [userId]
      );
      return rows;
    },
  };
}
