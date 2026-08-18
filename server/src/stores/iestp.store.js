import mysql from "mysql2/promise";

/**
 * Store MySQL para integrarse con la base de datos REAL del sistema académico
 * iestp (https://github.com/progamins/iestp), que es la fuente de verdad del
 * instituto. Se activa con DB_DRIVER=iestp.
 *
 * Mapeo de tablas del sistema iestp (ver iestp-local/):
 *   - estudiantes       → login del alumno (usuario/clave), ficha de identificación
 *   - pagos             → pagos del alumno (clave: nombres_apellidos, el sistema
 *                         registra pagos por nombre, no por DNI)
 *   - asistencias       → asistencias del alumno (clave: dni_estudiante)
 *   - estado_asistencia → estados de asistencia (Puntual/Tardanza/Falta/…)
 *   - justificaciones   → justificaciones (el panel web las acepta/rechaza y la
 *                         app refleja el Estado actualizado)
 *   - unidades_didacticas → cursos del programa del alumno
 *   - programas_estudio → nombres de programa
 *
 * La API crea únicamente su tabla propia de refresh tokens; NO modifica las
 * tablas del instituto (salvo las operaciones de negocio: crear justificación,
 * editar perfil, cambiar estado en panel admin).
 */

const clean = (name) => name.replace(/[^a-zA-Z0-9_]/g, "");

// mysql2 devuelve las columnas DATE/DATETIME como objetos Date de JS;
// normaliza a cadena YYYY-MM-DD (también acepta cadenas ya formateadas).
const fmtDate = (value) => {
  if (!value) return "";
  if (value instanceof Date) {
    // toISOString usa UTC: las fechas puras (DATE) no tienen zona horaria,
    // así que no hay corrimiento que aplicar.
    return value.toISOString().slice(0, 10);
  }
  return String(value).slice(0, 10);
};

// ── Normalizadores de estados (los valores del sistema iestp → valores de la app) ──

export function normalizeAsistenciaEstado(raw) {
  const s = String(raw ?? "")
    .trim()
    .toLowerCase();
  const map = {
    puntual: "presente",
    presente: "presente",
    tarde: "tardanza",
    tardanza: "tardanza",
    falta: "falta",
    ausente: "falta",
    justificado: "justificada",
    justificada: "justificada",
  };
  return map[s] ?? (s || "presente");
}

export function normalizeJustificacionEstado(raw) {
  const s = String(raw ?? "")
    .trim()
    .toLowerCase();
  if (s.startsWith("acept") || s === "aprobada") return "aceptada";
  if (s.startsWith("rechaz")) return "rechazada";
  return "pendiente";
}

// Estado de la app (pendiente/aceptada/rechazada/aprobada) → Estado del sistema iestp.
export function toDbJustificacionEstado(appEstado) {
  const s = String(appEstado ?? "").toLowerCase();
  if (s === "aceptada" || s === "aprobada") return "Aceptada";
  if (s === "rechazada") return "Rechazada";
  return "Pendiente";
}

// ── Mappers fila BD iestp → shape de la API/app (exportados para tests) ──

export function mapPago(row) {
  // El sistema iestp no guarda un estado de pago: un pago con número de recibo
  // emitido se considera pagado; en caso contrario queda pendiente.
  return {
    id: Number(row.id),
    concepto: row.concepto ?? "",
    monto: Number(row.importe ?? 0),
    estado: row.numero_recibo ? "pagado" : "pendiente",
    fecha: fmtDate(row.fecha),
    ubicacion: row.ubicacion ?? "",
  };
}

export function mapAsistencia(row) {
  return {
    id: Number(row.id),
    fecha: fmtDate(row.fecha),
    estado: normalizeAsistenciaEstado(row.estado),
    curso: row.curso ?? "",
  };
}

export function mapJustificacion(row) {
  return {
    id: Number(row.id),
    motivo: row.motivo ?? "",
    detalle: row.detalle ?? row.motivo ?? "",
    fecha: fmtDate(row.fecha),
    estado: normalizeJustificacionEstado(row.estado),
  };
}

export function mapCurso(row) {
  return {
    id: Number(row.id),
    nombre: row.nombre ?? "",
    codigo: row.codigo ?? "",
    docente: row.docente ?? "",
    creditos: Number(row.creditos ?? 0),
    estado: row.estado ?? "en_curso",
  };
}

/**
 * Construye la cláusula WHERE para aislar los pagos de UN estudiante.
 *
 * El sistema iestp registra los pagos por `nombres_apellidos` (no por DNI), así
 * que la coincidencia debe ser estricta: se exige que TODOS los tokens del
 * nombre del estudiante aparezcan en el registro, además de la coincidencia
 * exacta. Esto evita que alumnos con apellidos/nombres parecidos vean pagos
 * ajenos (el cruce por "primer nombre + último apellido" filtraba datos).
 */
export function buildPagosMatch(nombre) {
  const raw = String(nombre ?? "").trim();
  const tokens = raw.toLowerCase().split(/\s+/).filter(Boolean);
  if (tokens.length === 0) return { where: "1 = 0", params: [] };
  const conds = tokens.map(() => "LOWER(nombres_apellidos) LIKE ?");
  return {
    where: `(LOWER(nombres_apellidos) = LOWER(?) OR (${conds.join(" AND ")}))`,
    params: [raw, ...tokens.map((t) => `%${t}%`)],
  };
}

// ── Factory del store ──

export function createIestpStore(cfg, { adminUsernames = [] } = {}) {
  if (!cfg.database) {
    throw new Error("DB_DRIVER=iestp requiere DB_NAME (base de datos del sistema iestp)");
  }

  const isAdmin = (username) => adminUsernames.includes(username);

  let pool;
  const q = () => {
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

  const tables = {
    estudiantes: clean(cfg.estudiantesTable || "estudiantes"),
    pagos: clean(cfg.pagosTable || "pagos"),
    asistencias: clean(cfg.asistenciasTable || "asistencias"),
    estadoAsistencia: clean(cfg.estadoAsistenciaTable || "estado_asistencia"),
    justificaciones: clean(cfg.justificacionesTable || "justificaciones"),
    unidades: clean(cfg.unidadesTable || "unidades_didacticas"),
    programas: clean(cfg.programasTable || "programas_estudio"),
    refresh: clean(cfg.refreshTable || "refresh_tokens"),
  };
  const justTipoId = cfg.justificacionTipoId ? Number(cfg.justificacionTipoId) : null;

  const estudianteCols = `
    id,
    usuario AS username,
    clave AS password_hash,
    nombre AS full_name,
    dni,
    email,
    celular AS telefono,
    direccion,
    programa,
    programa_id
  `;

  async function findEstudianteBy(field, value) {
    const [rows] = await q().query(
      `SELECT ${estudianteCols} FROM \`${tables.estudiantes}\` WHERE \`${clean(field)}\` = ? LIMIT 1`,
      [value]
    );
    return rows[0] ?? null;
  }

  function toPublicUser(row) {
    return row
      ? {
          id: Number(row.id),
          username: row.username,
          fullName: row.full_name ?? "",
          // En modo iestp el rol se calcula por configuración (ADMIN_USERNAMES)
          // sin modificar la base del instituto.
          role: isAdmin(row.username) ? "admin" : "estudiante",
          createdAt: null,
          // Datos propios del alumno (ficha de identificación de la app).
          dni: row.dni ?? "",
          programa: row.programa ?? "",
        }
      : null;
  }

  return {
    async init() {
      await q().query(`
        CREATE TABLE IF NOT EXISTS \`${tables.refresh}\` (
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

    // ---- users (estudiantes del sistema iestp) ----
    findUserByUsername(username) {
      return findEstudianteBy("usuario", username);
    },

    findUserById(id) {
      return findEstudianteBy("id", id);
    },

    createUser() {
      // Los estudiantes los importa el sistema web iestp (Excel) o el admin;
      // el registro desde la app no está disponible en este modo.
      throw new Error("Registro no disponible en modo iestp: los estudiantes se gestionan en el sistema web del instituto");
    },

    toPublicUser,

    // ---- refresh tokens (tabla propia de la API) ----
    async saveRefreshToken({ userId, familyId, tokenHash, expiresAt }) {
      await q().query(
        `INSERT INTO \`${tables.refresh}\` (user_id, family_id, token_hash, expires_at) VALUES (?, ?, ?, ?)`,
        [userId, familyId, tokenHash, expiresAt]
      );
    },

    async findRefreshToken(tokenHash) {
      const [rows] = await q().query(
        `SELECT * FROM \`${tables.refresh}\` WHERE token_hash = ? LIMIT 1`,
        [tokenHash]
      );
      return rows[0] ?? null;
    },

    async revokeRefreshToken(tokenHash) {
      await q().query(`UPDATE \`${tables.refresh}\` SET revoked_at = ? WHERE token_hash = ?`, [
        Date.now(),
        tokenHash,
      ]);
    },

    async revokeRefreshTokenFamily(familyId) {
      await q().query(`UPDATE \`${tables.refresh}\` SET revoked_at = ? WHERE family_id = ?`, [
        Date.now(),
        familyId,
      ]);
    },

    // ---- perfil (Identificación): lee/escribe la fila del estudiante ----
    async getProfile(userId) {
      const [rows] = await q().query(
        `SELECT email, celular AS telefono, direccion FROM \`${tables.estudiantes}\` WHERE id = ?`,
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
        `UPDATE \`${tables.estudiantes}\` SET email = ?, celular = ?, direccion = ? WHERE id = ?`,
        [email, telefono, direccion, userId]
      );
      return this.getProfile(userId);
    },

    // ---- justificaciones (tabla compartida con el panel web iestp) ----
    async listJustificaciones(userId) {
      const estudiante = await findEstudianteBy("id", userId);
      if (!estudiante?.dni) return [];

      const [rows] = await q().query(
        `SELECT JustificacionID AS id,
                MotivoEstudiante AS motivo,
                MotivoEstudiante AS detalle,
                Fecha_Justificacion AS fecha,
                Estado AS estado
         FROM \`${tables.justificaciones}\`
         WHERE dni_estudiante = ?
         ORDER BY Fecha_Justificacion DESC, JustificacionID DESC`,
        [estudiante.dni]
      );
      return rows.map(mapJustificacion);
    },

    async createJustificacion({ userId, motivo, fecha, detalle }) {
      const estudiante = await findEstudianteBy("id", userId);
      if (!estudiante?.dni) {
        throw new Error("Estudiante sin DNI registrado");
      }

      const cols = [
        "dni_estudiante",
        "Fecha_Justificacion",
        "MotivoEstudiante",
        "Estado",
        "Fecha_Inicio",
        "Fecha_Fin",
      ];
      const values = [estudiante.dni, fecha, motivo, "Pendiente", fecha, fecha];
      if (justTipoId) {
        cols.push("TipoJustificacionID");
        values.push(justTipoId);
      }

      const [result] = await q().query(
        `INSERT INTO \`${tables.justificaciones}\` (${cols.join(", ")})
         VALUES (${cols.map(() => "?").join(", ")})`,
        values
      );
      return mapJustificacion({
        id: result.insertId,
        motivo,
        detalle,
        fecha,
        estado: "Pendiente",
      });
    },

    // ---- admin (panel web): todas las justificaciones con el nombre del alumno ----
    async listAllJustificaciones() {
      const [rows] = await q().query(
        `SELECT j.JustificacionID AS id,
                j.dni_estudiante,
                e.nombre AS fullName,
                e.usuario AS username,
                j.Fecha_Justificacion AS fecha,
                j.MotivoEstudiante AS motivo,
                j.MotivoResolucion AS motivo_resolucion,
                j.FechaRevision AS fecha_revision,
                j.Estado AS estado
         FROM \`${tables.justificaciones}\` j
         LEFT JOIN \`${tables.estudiantes}\` e ON j.dni_estudiante = e.dni
         ORDER BY j.Fecha_Justificacion DESC, j.JustificacionID DESC`
      );
      return rows.map((r) => ({ ...r, estado: normalizeJustificacionEstado(r.estado) }));
    },

    async setJustificacionEstado(id, estado) {
      const [result] = await q().query(
        `UPDATE \`${tables.justificaciones}\`
         SET Estado = ?, FechaRevision = NOW()
         WHERE JustificacionID = ?`,
        [toDbJustificacionEstado(estado), id]
      );
      if (result.affectedRows === 0) return null;

      const [rows] = await q().query(
        `SELECT JustificacionID AS id,
                MotivoEstudiante AS motivo,
                Fecha_Justificacion AS fecha,
                Estado AS estado
         FROM \`${tables.justificaciones}\`
         WHERE JustificacionID = ? LIMIT 1`,
        [id]
      );
      return rows[0] ? mapJustificacion(rows[0]) : null;
    },

    // ---- asistencias (por DNI del estudiante) ----
    async listAsistencias(userId) {
      const estudiante = await findEstudianteBy("id", userId);
      if (!estudiante?.dni) return [];

      const [rows] = await q().query(
        `SELECT a.id,
                a.fecha_hora AS fecha,
                ea.estado AS estado,
                '' AS curso
         FROM \`${tables.asistencias}\` a
         JOIN \`${tables.estadoAsistencia}\` ea ON a.estado_id = ea.estado_id
         WHERE a.dni_estudiante = ?
         ORDER BY a.fecha_hora DESC`,
        [estudiante.dni]
      );
      return rows.map(mapAsistencia);
    },

    // ---- pagos (el sistema iestp registra pagos por nombre del alumno) ----
    // Aislamiento estricto por alumno: todos los tokens del nombre deben
    // aparecer en el registro (ver buildPagosMatch).
    async listPagos(userId) {
      const estudiante = await findEstudianteBy("id", userId);
      const nombre = estudiante?.full_name ?? "";
      if (!nombre) return { pagos: [], ubicaciones: [] };

      const { where, params } = buildPagosMatch(nombre);
      const [rows] = await q().query(
        `SELECT numero_orden AS id,
                concepto,
                importe,
                fecha,
                carrera AS ubicacion,
                numero_recibo
         FROM \`${tables.pagos}\`
         WHERE ${where}
         ORDER BY fecha DESC, numero_orden DESC`,
        params
      );

      const pagos = rows.map(mapPago);
      const ubicaciones = [...new Set(pagos.map((p) => p.ubicacion).filter(Boolean))];
      return { pagos, ubicaciones };
    },

    // Horarios: en el sistema iestp se suben como ARCHIVOS por programa
    // (tabla horarios: horario_id, archivo). No hay filas estructuradas que la
    // app pueda renderizar; se devuelve vacío y la app muestra su estado vacío.
    async listHorarios() {
      return [];
    },

    // ---- cursos (unidades didácticas del programa del alumno) ----
    async listCursos(userId) {
      const estudiante = await findEstudianteBy("id", userId);
      if (!estudiante?.programa_id) return [];

      const [rows] = await q().query(
        `SELECT unidad_id AS id,
                nombre_unidad AS nombre,
                CONCAT('UD-', unidad_id) AS codigo,
                '' AS docente,
                0 AS creditos,
                'en_curso' AS estado
         FROM \`${tables.unidades}\`
         WHERE programa_id = ?
         ORDER BY unidad_id`,
        [estudiante.programa_id]
      );
      return rows.map(mapCurso);
    },
  };
}
