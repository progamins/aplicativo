import { config } from "./config.js";
import { createIestpStore } from "./stores/iestp.store.js";
import { createMysqlStore } from "./stores/mysql.store.js";
import { createSqliteStore } from "./stores/sqlite.store.js";

/**
 * Almacén activo (interfaz única):
 *   findUserByUsername / findUserById / createUser / toPublicUser
 *   saveRefreshToken / findRefreshToken / revokeRefreshToken / revokeRefreshTokenFamily / close
 * La elección se hace por DB_DRIVER:
 *   - sqlite (default): datos demo locales
 *   - mysql: autentica contra una tabla externa (p. ej. listado_usuarios del iestp)
 *   - iestp: integración completa con las tablas reales del sistema iestp
 *            (estudiantes/pagos/asistencias/justificaciones/unidades_didacticas)
 */
export const store =
  config.dbDriver === "mysql"
    ? createMysqlStore(config.mysql, { adminUsernames: config.adminUsernames })
    : config.dbDriver === "iestp"
      ? createIestpStore(config.iestp, { adminUsernames: config.adminUsernames })
      : createSqliteStore(config.dbPath, { adminUsernames: config.adminUsernames });

export async function initStore() {
  if (typeof store.init === "function") await store.init();
}
