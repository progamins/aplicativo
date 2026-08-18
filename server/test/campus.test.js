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
    username: "estudiante_campus",
    password: "Segura123",
    fullName: "Estudiante Campus",
  });
  token = reg.body.accessToken;
});

after(async () => {
  await store.close();
});

describe("PATCH /api/auth/me (perfil de identificación)", () => {
  it("actualiza correo, dirección y teléfono (200)", async () => {
    const res = await request(app)
      .patch("/api/auth/me")
      .set("Authorization", `Bearer ${token}`)
      .send({ email: "estudiante@iestp.edu.pe", direccion: "Av. Principal 123", telefono: "999888777" });
    assert.equal(res.status, 200);
    assert.equal(res.body.user.email, "estudiante@iestp.edu.pe");
    assert.equal(res.body.user.direccion, "Av. Principal 123");
    assert.equal(res.body.user.telefono, "999888777");
  });

  it("GET /me devuelve el perfil guardado", async () => {
    const res = await request(app)
      .get("/api/auth/me")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.equal(res.body.user.email, "estudiante@iestp.edu.pe");
  });

  it("rechaza correo inválido (400)", async () => {
    const res = await request(app)
      .patch("/api/auth/me")
      .set("Authorization", `Bearer ${token}`)
      .send({ email: "no-es-un-correo" });
    assert.equal(res.status, 400);
  });

  it("rechaza sin token (401)", async () => {
    const res = await request(app).patch("/api/auth/me").send({ email: "x@y.pe" });
    assert.equal(res.status, 401);
  });
});

describe("GET /api/pagos", () => {
  it("lista los pagos demo con sus ubicaciones", async () => {
    const res = await request(app)
      .get("/api/pagos")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.ok(res.body.pagos.length >= 1);
    assert.ok(Array.isArray(res.body.ubicaciones));
    assert.ok(res.body.pagos.every((p) => p.concepto && p.estado));
  });
});

describe("GET /api/horarios", () => {
  it("lista los horarios demo ordenados por día", async () => {
    const res = await request(app)
      .get("/api/horarios")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.ok(res.body.horarios.length >= 1);
    assert.ok(res.body.horarios.every((h) => h.dia && h.hora_inicio && h.curso));
  });
});

describe("GET /api/cursos", () => {
  it("lista los cursos demo", async () => {
    const res = await request(app)
      .get("/api/cursos")
      .set("Authorization", `Bearer ${token}`);
    assert.equal(res.status, 200);
    assert.ok(res.body.cursos.length >= 1);
    assert.ok(res.body.cursos.every((c) => c.nombre && c.codigo));
  });
});
