import { config } from "./config.js";
import { createMysqlStore } from "./stores/mysql.store.js";
import { createSqliteStore } from "./stores/sqlite.store.js";

/**
 * Almacén activo (interfaz única):
 *   findUserByUsername / findUserById / createUser / toPublicUser
 *   saveRefreshToken / findRefreshToken / revokeRefreshToken / revokeRefreshTokenFamily / close
 * La elección sqlite|mysql se hace por DB_DRIVER.
 */
export const store =
  config.dbDriver === "mysql"
    ? createMysqlStore(config.mysql)
    : createSqliteStore(config.dbPath);

export async function initStore() {
  if (typeof store.init === "function") await store.init();
}
