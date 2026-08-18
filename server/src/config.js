import "dotenv/config";

const bool = (v, def) => (v === undefined ? def : v === "true" || v === "1");

const dbDriver = process.env.DB_DRIVER || "sqlite";

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

  // Usuarios administradores (separados por coma). En SQLite se marcan con
  // rol 'admin' al arrancar; en modo MySQL se calcula sin tocar la BD externa.
  adminUsernames: (process.env.ADMIN_USERNAMES || "")
    .split(",")
    .map((s) => s.trim())
    .filter(Boolean),

  dbDriver,
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
    profileTable: process.env.DB_PROFILE_TABLE || "user_profiles",
    pagosTable: process.env.DB_PAGOS_TABLE || "pagos",
    horariosTable: process.env.DB_HORARIOS_TABLE || "horarios",
    cursosTable: process.env.DB_CURSOS_TABLE || "cursos",
  },

  // Modo iestp (DB_DRIVER=iestp): integración con la base de datos real del
  // sistema académico del instituto (https://github.com/progamins/iestp).
  // Las tablas son las del sistema iestp; la API solo crea su tabla de refresh
  // tokens y respeta la fuente de verdad del instituto.
  iestp: {
    host: process.env.DB_HOST || "localhost",
    port: Number(process.env.DB_PORT || 3306),
    user: process.env.DB_USER || "",
    password: process.env.DB_PASSWORD || "",
    database: process.env.DB_NAME || "",
    refreshTable: process.env.DB_REFRESH_TABLE || "refresh_tokens",
    estudiantesTable: process.env.IESTP_ESTUDIANTES_TABLE || "estudiantes",
    pagosTable: process.env.IESTP_PAGOS_TABLE || "pagos",
    asistenciasTable: process.env.IESTP_ASISTENCIAS_TABLE || "asistencias",
    estadoAsistenciaTable: process.env.IESTP_ESTADO_ASISTENCIA_TABLE || "estado_asistencia",
    justificacionesTable: process.env.IESTP_JUSTIFICACIONES_TABLE || "justificaciones",
    // Tipo por defecto al crear justificaciones desde la app (columna
    // TipoJustificacionID del sistema iestp). Vacío = se omite la columna.
    justificacionTipoId: process.env.IESTP_JUSTIFICACION_TIPO_ID || null,
    unidadesTable: process.env.IESTP_UNIDADES_TABLE || "unidades_didacticas",
    programasTable: process.env.IESTP_PROGRAMAS_TABLE || "programas_estudio",
  },

  // En modo iestp el registro está deshabilitado por defecto: los estudiantes
  // los gestiona el sistema web del instituto, no la app.
  enableRegister: bool(process.env.ENABLE_REGISTER, dbDriver !== "iestp"),
};

if (config.nodeEnv === "production" && config.jwtSecret === "dev-only-secret-change-me") {
  console.error("❌ JWT_SECRET no está definido. Abortando en producción.");
  process.exit(1);
}
