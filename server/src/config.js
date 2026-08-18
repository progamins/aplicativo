import "dotenv/config";

const bool = (v, def) => (v === undefined ? def : v === "true" || v === "1");

export const config = {
  nodeEnv: process.env.NODE_ENV || "development",
  port: Number(process.env.PORT || 3000),
  host: process.env.HOST || "0.0.0.0",

  logLevel: process.env.LOG_LEVEL || (process.env.NODE_ENV === "test" ? "silent" : "info"),

  jwtSecret: process.env.JWT_SECRET || "dev-only-secret-change-me",
  accessTokenTtl: process.env.ACCESS_TOKEN_TTL || "15m",
  refreshTokenTtlSeconds: Number(process.env.REFRESH_TOKEN_TTL_DAYS || 7) * 24 * 60 * 60,

  bcryptRounds: Number(process.env.BCRYPT_ROUNDS || 10),

  corsOrigin: process.env.CORS_ORIGIN || "*",

  dbDriver: process.env.DB_DRIVER || "sqlite",
  dbPath: process.env.DB_PATH || "./data/app.db",

  // Configuración MySQL (modo compatible con el sistema académico iestp)
  mysql: {
    host: process.env.DB_HOST || "localhost",
    port: Number(process.env.DB_PORT || 3306),
    user: process.env.DB_USER || "",
    password: process.env.DB_PASSWORD || "",
    database: process.env.DB_NAME || "",
    table: process.env.DB_TABLE || "listado_usuarios",
    idColumn: process.env.DB_ID_COL || "id",
    usernameColumn: process.env.DB_USERNAME_COL || "email",
    passwordColumn: process.env.DB_PASSWORD_COL || "password",
    fullNameColumn: process.env.DB_FULLNAME_COL || null,
    refreshTable: process.env.DB_REFRESH_TABLE || "refresh_tokens",
    justificacionesTable: process.env.DB_JUSTIFICACIONES_TABLE || "justificaciones",
    asistenciasTable: process.env.DB_ASISTENCIAS_TABLE || "asistencias",
  },

  enableRegister: bool(process.env.ENABLE_REGISTER, true),
};

if (config.nodeEnv === "production" && config.jwtSecret === "dev-only-secret-change-me") {
  console.error("❌ JWT_SECRET no está definido. Abortando en producción.");
  process.exit(1);
}
