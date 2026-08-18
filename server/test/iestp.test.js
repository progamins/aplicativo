process.env.NODE_ENV = "test";
process.env.DB_DRIVER = "iestp";
process.env.DB_NAME = "aplicativo";
process.env.JWT_SECRET = "test-secret";

import assert from "node:assert/strict";
import { describe, it } from "node:test";
import bcrypt from "bcryptjs";

import { verifyPassword } from "../src/passwords.js";
import {
  buildPagosMatch,
  createIestpStore,
  mapAsistencia,
  mapCurso,
  mapJustificacion,
  mapPago,
  normalizeAsistenciaEstado,
  normalizeJustificacionEstado,
  toDbJustificacionEstado,
} from "../src/stores/iestp.store.js";

const { store } = await import("../src/db.js");

describe("verifyPassword (bcrypt + legacy texto plano del sistema iestp)", () => {
  it("verifica hashes bcrypt", () => {
    const hash = bcrypt.hashSync("Segura123", 4);
    assert.equal(verifyPassword("Segura123", hash), true);
    assert.equal(verifyPassword("Otra123", hash), false);
  });

  it("verifica credenciales legacy en texto plano (clave generada por iestp)", () => {
    assert.equal(verifyPassword("72345678ROSAS", "72345678ROSAS"), true);
    assert.equal(verifyPassword("incorrecta", "72345678ROSAS"), false);
  });

  it("rechaza valores vacíos o nulos", () => {
    assert.equal(verifyPassword("x", null), false);
    assert.equal(verifyPassword("x", ""), false);
  });
});

describe("normalización de estados (sistema iestp → app)", () => {
  it("asistencia: Puntual/Presente → presente", () => {
    assert.equal(normalizeAsistenciaEstado("Puntual"), "presente");
    assert.equal(normalizeAsistenciaEstado("Presente"), "presente");
  });

  it("asistencia: Tardanza/Falta/Justificado", () => {
    assert.equal(normalizeAsistenciaEstado("Tardanza"), "tardanza");
    assert.equal(normalizeAsistenciaEstado("Falta"), "falta");
    assert.equal(normalizeAsistenciaEstado("Justificado"), "justificada");
  });

  it("justificación: Pendiente/Aceptada/Rechazada", () => {
    assert.equal(normalizeJustificacionEstado("Pendiente"), "pendiente");
    assert.equal(normalizeJustificacionEstado("Aceptada"), "aceptada");
    assert.equal(normalizeJustificacionEstado("Rechazada"), "rechazada");
    assert.equal(normalizeJustificacionEstado("aprobada"), "aceptada");
  });

  it("toDbJustificacionEstado: app → sistema iestp", () => {
    assert.equal(toDbJustificacionEstado("pendiente"), "Pendiente");
    assert.equal(toDbJustificacionEstado("aceptada"), "Aceptada");
    assert.equal(toDbJustificacionEstado("aprobada"), "Aceptada");
    assert.equal(toDbJustificacionEstado("rechazada"), "Rechazada");
  });
});

describe("mappers de filas iestp → shape de la API", () => {
  it("mapPago: recibo presente → pagado, sin recibo → pendiente", () => {
    const pagado = mapPago({
      id: 101,
      concepto: "Pensión Marzo",
      importe: "120.00",
      fecha: "2026-03-20 00:00:00",
      ubicacion: "Contabilidad",
      numero_recibo: "R-0001",
    });
    assert.deepEqual(pagado, {
      id: 101,
      concepto: "Pensión Marzo",
      monto: 120,
      estado: "pagado",
      fecha: "2026-03-20",
      ubicacion: "Contabilidad",
    });

    const pendiente = mapPago({
      id: 102,
      concepto: "Pensión Abril",
      importe: 120,
      fecha: "2026-04-20",
      ubicacion: "",
      numero_recibo: null,
    });
    assert.equal(pendiente.estado, "pendiente");
    assert.equal(pendiente.monto, 120);
  });

  it("mapAsistencia: estado normalizado y fecha sin hora", () => {
    const a = mapAsistencia({ id: 7, fecha: "2026-04-01 08:15:00", estado: "Tardanza", curso: "" });
    assert.deepEqual(a, { id: 7, fecha: "2026-04-01", estado: "tardanza", curso: "" });
  });

  it("fechas: mysql2 devuelve Date de JS → se formatea a YYYY-MM-DD", () => {
    const pago = mapPago({
      id: 1,
      concepto: "Pensión",
      importe: 120,
      fecha: new Date("2026-04-20T00:00:00.000Z"),
      ubicacion: "Caja",
      numero_recibo: null,
    });
    assert.equal(pago.fecha, "2026-04-20");
    assert.equal(pago.estado, "pendiente");

    const asistencia = mapAsistencia({
      id: 2,
      fecha: new Date("2026-08-10T12:20:00.000Z"),
      estado: "Puntual",
      curso: "",
    });
    assert.equal(asistencia.fecha, "2026-08-10");
  });

  it("mapJustificacion: estados del sistema → minúsculas de la app", () => {
    const j = mapJustificacion({ id: 5, motivo: "Cita médica", fecha: "2026-04-02", estado: "Aceptada" });
    assert.deepEqual(j, { id: 5, motivo: "Cita médica", detalle: "Cita médica", fecha: "2026-04-02", estado: "aceptada" });
  });

  it("mapCurso: unidades didácticas → cursos", () => {
    const c = mapCurso({ id: 12, nombre: "Desarrollo de Software II", codigo: "UD-12", docente: "", creditos: 0, estado: "en_curso" });
    assert.equal(c.nombre, "Desarrollo de Software II");
    assert.equal(c.codigo, "UD-12");
  });
});

describe("buildPagosMatch (aislamiento de pagos por alumno)", () => {
  it("exige TODOS los tokens del nombre (evita pagos ajenos con apellidos parecidos)", () => {
    const { where, params } = buildPagosMatch("ROSAS ALBINEZ EDWIN RAUL");
    // Parámetros: nombre exacto + un LIKE por token (en formato %token%).
    assert.equal(params.length, 5);
    assert.ok(params.includes("%rosas%"));
    assert.ok(params.includes("%albinez%"));
    assert.ok(params.includes("%edwin%"));
    assert.ok(params.includes("%raul%"));
    // La cláusula exige AND entre todos los tokens.
    assert.match(where, /\(LOWER\(nombres_apellidos\) = LOWER\(\?\) OR \(LOWER\(nombres_apellidos\) LIKE \? AND LOWER\(nombres_apellidos\) LIKE \? AND LOWER\(nombres_apellidos\) LIKE \? AND LOWER\(nombres_apellidos\) LIKE \?\)\)/);
  });

  it("un alumno NO coincide con pagos de otro que comparte apellidos", () => {
    // "ROSAS ALBINEZ CARLOS RAUL" tiene ROSAS y RAUL, pero le faltan EDWIN
    // → el WHERE exige AND de todos los tokens, así que los pagos de uno
    //    no pueden satisfacer la cláusula del otro.
    const a = buildPagosMatch("ROSAS ALBINEZ EDWIN RAUL");
    const b = buildPagosMatch("ROSAS ALBINEZ CARLOS RAUL");
    assert.ok(a.params.includes("%edwin%"));
    assert.ok(!b.params.includes("%edwin%"));
    assert.ok(b.params.includes("%carlos%"));
    assert.ok(!a.params.includes("%carlos%"));
    // Ambos exigen la misma cantidad de tokens (4 AND): nadie ve pagos
    // que no contengan su nombre completo.
    assert.equal(a.params.length, 5);
    assert.equal(b.params.length, 5);
  });

  it("sin nombre devuelve sin coincidencias", () => {
    const { where, params } = buildPagosMatch("   ");
    assert.equal(where, "1 = 0");
    assert.deepEqual(params, []);
  });
});

describe("store iestp (selección por DB_DRIVER=iestp)", () => {
  it("db.js selecciona el store iestp", () => {
    assert.equal(typeof store.init, "function");
    assert.equal(typeof store.listPagos, "function");
    assert.equal(typeof store.listCursos, "function");
  });

  it("el registro no está disponible en modo iestp", () => {
    assert.throws(
      () => store.createUser({ username: "x", passwordHash: "hash", fullName: "X" }),
      /Registro no disponible en modo iestp/
    );
  });

  it("createIestpStore exige base de datos configurada", () => {
    assert.throws(
      () => createIestpStore({ database: "" }),
      /DB_DRIVER=iestp requiere DB_NAME/
    );
  });
});
