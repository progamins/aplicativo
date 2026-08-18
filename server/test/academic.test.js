process.env.NODE_ENV = "test";
process.env.DB_PATH = ":memory:";
process.env.BCRYPT_ROUNDS = "4";
process.env.JWT_SECRET = "test-secret";
process.env.ACCESS_TOKEN_TTL = "15m";

import assert from "node:assert/strict";
import { after, before, describe, it } from "node:test";
import request from "supertest";

const { createApp } = await import("../src/app.js");
const { initStore, store } = await import("../src/db.js");

let app;
let token;

before(async () => {
  await initStore();
  app = createApp();

  const reg = await request(app).post("/api/auth/register").send({
    username: "estudiante",
    password: "Segura123",
    fullName: "Estudiante Demo",
  });
  token = reg.body.accessToken;
});

after(async () => {
  await store.close();
});

describe("Protección de rutas académicas", () => {
  it("rechaza sin token (401)", async () => {
    const res = await request(app).get("/api/justificaciones");
    assert.equal(res.status, 401);
  });
});

describe("POST /api/justificaciones", () => {
  it("crea una justificación (201)", async () => {
    const res = await request(app)
      .post("/api/justificaciones")
      .set("Authorization", `Bearer ${token}`)
      .send({ motivo: "Consulta médica", fecha: "2026-08-20", detalle: "Cita con el médico" });
    assert.equal(res.status, 201);
    assert.equal(res.body.justificacion.motivo, "Consulta médica");
    assert.equal(res.body.justificacion.estado, "pendiente");
  });

  it("rechaza fecha inválida (400)", async () => {
    const res = await request(app)
      .post("/api/justificaciones")
      .set("Authorization", `Bearer ${token}`)
      .send({ motivo: "Trámite", fecha: "20/08/2026" });
    assert.equal(res.status, 400);
  });

  it("rechaza motivo corto (400)", async () => {
    const res = await request(app)
      .post("/api/justificaciones")
      .set("Authorization", `Bearer ${token}`)
      .send({ motivo: "ab", fecha: "2026-08-20" });
    assert.equal(res.status, 400);
  });
});

describe("GET /api/justificaciones", () => {
  it("lista solo las del usuario autenticado", async () => {
    const res = await request(app)
      .get("/api/justificaciones")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.ok(Array.isArray(res.body.justificaciones));
    assert.ok(res.body.justificaciones.length >= 1);
  });
});

describe("GET /api/asistencias", () => {
  it("lista las asistencias (con datos demo al registrarse)", async () => {
    const res = await request(app)
      .get("/api/asistencias")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.ok(res.body.asistencias.length >= 3);
    assert.ok(res.body.asistencias.every((a) => a.estado));
  });
});

describe("GET /api/estadisticas", () => {
  it("calcula las estadísticas del usuario", async () => {
    const res = await request(app)
      .get("/api/estadisticas")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.equal(res.body.estadisticas.justificaciones, 1);
    assert.equal(res.body.estadisticas.pendientes, 1);
    assert.equal(res.body.estadisticas.totalAsistencias, 3);
    assert.ok(res.body.estadisticas.presentes >= 2);
  });
});
