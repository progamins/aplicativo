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

before(async () => {
  await initStore();
  app = createApp();
});

after(async () => {
  await store.close();
});

describe("GET /api/health", () => {
  it("responde ok con el driver de BD", async () => {
    const res = await request(app).get("/api/health");
    assert.equal(res.status, 200);
    assert.equal(res.body.status, "ok");
    assert.equal(res.body.db, "sqlite");
  });
});

describe("POST /api/auth/register", () => {
  it("registra y devuelve accessToken + refreshToken + user", async () => {
    const res = await request(app).post("/api/auth/register").send({
      username: "ana",
      password: "Segura123",
      fullName: "Ana Prueba",
    });
    assert.equal(res.status, 201);
    assert.ok(res.body.accessToken);
    assert.ok(res.body.refreshToken);
    assert.equal(res.body.user.username, "ana");
    assert.equal(res.body.user.fullName, "Ana Prueba");
  });

  it("rechaza usuario duplicado (409)", async () => {
    const res = await request(app).post("/api/auth/register").send({
      username: "ana",
      password: "Segura123",
    });
    assert.equal(res.status, 409);
  });

  it("rechaza contraseña débil (400)", async () => {
    const res = await request(app).post("/api/auth/register").send({
      username: "luis",
      password: "abc",
    });
    assert.equal(res.status, 400);
    assert.match(res.body.error, /8 caracteres/);
  });

  it("rechaza usuario corto (400)", async () => {
    const res = await request(app).post("/api/auth/register").send({
      username: "ab",
      password: "Segura123",
    });
    assert.equal(res.status, 400);
  });
});

describe("POST /api/auth/login", () => {
  it("loguea y devuelve los dos tokens", async () => {
    const res = await request(app).post("/api/auth/login").send({
      username: "ana",
      password: "Segura123",
    });
    assert.equal(res.status, 200);
    assert.ok(res.body.accessToken);
    assert.ok(res.body.refreshToken);
  });

  it("rechaza contraseña incorrecta (401)", async () => {
    const res = await request(app).post("/api/auth/login").send({
      username: "ana",
      password: "Incorrecta1",
    });
    assert.equal(res.status, 401);
  });

  it("rechaza usuario inexistente (401)", async () => {
    const res = await request(app).post("/api/auth/login").send({
      username: "noexiste",
      password: "Segura123",
    });
    assert.equal(res.status, 401);
  });
});

describe("GET /api/auth/me", () => {
  it("devuelve el perfil con token válido", async () => {
    const login = await request(app).post("/api/auth/login").send({
      username: "ana",
      password: "Segura123",
    });
    const res = await request(app)
      .get("/api/auth/me")
      .set("Authorization", `Bearer ${login.body.accessToken}`);
    assert.equal(res.status, 200);
    assert.equal(res.body.user.username, "ana");
  });

  it("rechaza sin token (401)", async () => {
    const res = await request(app).get("/api/auth/me");
    assert.equal(res.status, 401);
  });
});

describe("POST /api/auth/refresh (rotación y detección de reutilización)", () => {
  async function login() {
    const res = await request(app).post("/api/auth/login").send({
      username: "ana",
      password: "Segura123",
    });
    return res.body;
  }

  it("rota el token y permite encadenar refrescos", async () => {
    const { refreshToken: a } = await login();
    const r1 = await request(app).post("/api/auth/refresh").send({ refreshToken: a });
    assert.equal(r1.status, 200);
    assert.ok(r1.body.accessToken);
    assert.ok(r1.body.refreshToken);

    const r2 = await request(app)
      .post("/api/auth/refresh")
      .send({ refreshToken: r1.body.refreshToken });
    assert.equal(r2.status, 200);
  });

  it("detecta reutilización de un token ya rotado y revoca la familia", async () => {
    const { refreshToken: a } = await login();
    const r1 = await request(app).post("/api/auth/refresh").send({ refreshToken: a });
    assert.equal(r1.status, 200);
    const b = r1.body.refreshToken;

    // Reutilizar A (ya rotado) → debe revocar toda la familia.
    const reuse = await request(app).post("/api/auth/refresh").send({ refreshToken: a });
    assert.equal(reuse.status, 401);

    // B (de la misma familia) también queda revocado.
    const after = await request(app).post("/api/auth/refresh").send({ refreshToken: b });
    assert.equal(after.status, 401);
  });

  it("rechaza token inválido (401)", async () => {
    const res = await request(app)
      .post("/api/auth/refresh")
      .send({ refreshToken: "token-que-no-existe" });
    assert.equal(res.status, 401);
  });
});

describe("POST /api/auth/logout", () => {
  it("revoca el refresh token", async () => {
    // Usuario nuevo: el limiter de login es por usuario+IP (5 intentos/15 min).
    await request(app).post("/api/auth/register").send({
      username: "caro",
      password: "Segura123",
    });
    const login = await request(app).post("/api/auth/login").send({
      username: "caro",
      password: "Segura123",
    });
    const refreshToken = login.body.refreshToken;

    const out = await request(app).post("/api/auth/logout").send({ refreshToken });
    assert.equal(out.status, 204);

    const after = await request(app).post("/api/auth/refresh").send({ refreshToken });
    assert.equal(after.status, 401);
  });
});

describe("Rate limiting", () => {
  it("bloquea tras 5 intentos de login fallidos (429)", async () => {
    let last;
    for (let i = 0; i < 6; i++) {
      last = await request(app).post("/api/auth/login").send({
        username: "brute",
        password: "Incorrecta1",
      });
    }
    assert.equal(last.status, 429);
    assert.match(last.body.error, /Demasiados intentos/);
  });
});
