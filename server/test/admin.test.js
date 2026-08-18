process.env.NODE_ENV = "test";
process.env.DB_PATH = ":memory:";
process.env.BCRYPT_ROUNDS = "4";
process.env.JWT_SECRET = "test-secret";
process.env.ACCESS_TOKEN_TTL = "15m";
process.env.ADMIN_USERNAMES = "admin";

import assert from "node:assert/strict";
import { after, before, describe, it } from "node:test";
import request from "supertest";

const { createApp } = await import("../src/app.js");
const { initStore, store } = await import("../src/db.js");

let app;
let adminToken;
let studentToken;
let justificacionId;

before(async () => {
  await initStore();
  app = createApp();

  const admin = await request(app).post("/api/auth/register").send({
    username: "admin",
    password: "Segura123",
    fullName: "Administrador",
  });
  adminToken = admin.body.accessToken;
  assert.equal(admin.body.user.role, "admin");

  const student = await request(app).post("/api/auth/register").send({
    username: "estudiante2",
    password: "Segura123",
    fullName: "Estudiante Dos",
  });
  studentToken = student.body.accessToken;
  assert.equal(student.body.user.role, "estudiante");

  const created = await request(app)
    .post("/api/justificaciones")
    .set("Authorization", `Bearer ${studentToken}`)
    .send({ motivo: "Familiar enfermo", fecha: "2026-08-21" });
  justificacionId = created.body.justificacion.id;
});

after(async () => {
  await store.close();
});

describe("Panel de administración", () => {
  it("rechaza sin token (401)", async () => {
    const res = await request(app).get("/api/admin/justificaciones");
    assert.equal(res.status, 401);
  });

  it("rechaza a un estudiante (403)", async () => {
    const res = await request(app)
      .get("/api/admin/justificaciones")
      .set("Authorization", `Bearer ${studentToken}`);
    assert.equal(res.status, 403);
  });

  it("lista todas las justificaciones con datos del estudiante (200)", async () => {
    const res = await request(app)
      .get("/api/admin/justificaciones")
      .set("Authorization", `Bearer ${adminToken}`);
    assert.equal(res.status, 200);
    const row = res.body.justificaciones.find((j) => j.id === justificacionId);
    assert.ok(row, "la justificación del estudiante debe aparecer");
    assert.equal(row.username, "estudiante2");
    assert.equal(row.fullName, "Estudiante Dos");
    assert.equal(row.estado, "pendiente");
  });

  it("aprueba una justificación (200)", async () => {
    const res = await request(app)
      .patch(`/api/admin/justificaciones/${justificacionId}`)
      .set("Authorization", `Bearer ${adminToken}`)
      .send({ estado: "aprobada" });
    assert.equal(res.status, 200);
    assert.equal(res.body.justificacion.estado, "aprobada");
  });

  it("el estudiante ve el nuevo estado en su listado", async () => {
    const res = await request(app)
      .get("/api/justificaciones")
      .set("Authorization", `Bearer ${studentToken}`);
    const row = res.body.justificaciones.find((j) => j.id === justificacionId);
    assert.equal(row.estado, "aprobada");
  });

  it("rechaza estado inválido (400)", async () => {
    const res = await request(app)
      .patch(`/api/admin/justificaciones/${justificacionId}`)
      .set("Authorization", `Bearer ${adminToken}`)
      .send({ estado: "talvez" });
    assert.equal(res.status, 400);
  });

  it("rechaza id inexistente (404)", async () => {
    const res = await request(app)
      .patch("/api/admin/justificaciones/99999")
      .set("Authorization", `Bearer ${adminToken}`)
      .send({ estado: "rechazada" });
    assert.equal(res.status, 404);
  });

  it("un estudiante no puede aprobar (403)", async () => {
    const res = await request(app)
      .patch(`/api/admin/justificaciones/${justificacionId}`)
      .set("Authorization", `Bearer ${studentToken}`)
      .send({ estado: "aprobada" });
    assert.equal(res.status, 403);
  });
});
