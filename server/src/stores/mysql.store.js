import mysql from "mysql2/promise";

/**
 * Store MySQL configurable para autenticar contra una base de datos existente,
 * por ejemplo `listado_usuarios` del sistema académico (iestp):
 *   DB_TABLE=listado_usuarios  DB_USERNAME_COL=email  DB_PASSWORD_COL=password
 * Los hashes de PHP `password_hash()` (bcrypt) son verificables con bcryptjs.
 */
export function createMysqlStore(cfg) {
  if (!cfg.database) {
    throw new Error("DB_DRIVER=mysql requiere DB_NAME (base de datos)");
  }

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

  return {
    async init() {
      // Tabla de refresh tokens (se crea si no existe; no toca el resto de la BD).
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
  };
}
