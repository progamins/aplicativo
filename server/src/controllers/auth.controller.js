import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import { z } from "zod";

import { config } from "../config.js";
import { store } from "../db.js";
import { verifyPassword } from "../passwords.js";
import {
  expiryTimestamp,
  generateFamilyId,
  generateRefreshToken,
  hashToken,
} from "../tokens.js";

// Hash ficticio para igualar tiempos de respuesta cuando el usuario no existe
// (evita enumeración de usuarios por timing).
const DUMMY_HASH = bcrypt.hashSync("timing-equalizer-dummy", 10);

const usernameField = z
  .string()
  .trim()
  .min(3, "El usuario debe tener al menos 3 caracteres")
  .max(50, "El usuario no puede superar 50 caracteres");

// Política de contraseñas solo en registro (login acepta credenciales legacy).
const registerSchema = z.object({
  username: usernameField,
  password: z
    .string()
    .min(8, "La contraseña debe tener al menos 8 caracteres")
    .max(100, "La contraseña no puede superar 100 caracteres")
    .regex(/[a-zA-Z]/, "La contraseña debe incluir al menos una letra")
    .regex(/[0-9]/, "La contraseña debe incluir al menos un número"),
  fullName: z
    .string()
    .trim()
    .max(100, "El nombre no puede superar 100 caracteres")
    .optional()
    .default(""),
});

const loginSchema = z.object({
  username: usernameField,
  password: z.string().min(1, "La contraseña es obligatoria").max(100),
});

const refreshSchema = z.object({
  refreshToken: z.string().min(10, "Token de refresco inválido").max(200),
});

// Campos del perfil de identificación editables desde la app (Campus Virtual).
const updateProfileSchema = z.object({
  email: z
    .union([z.literal(""), z.string().trim().max(120).email("Correo electrónico inválido")])
    .optional(),
  direccion: z.string().trim().max(200, "La dirección no puede superar 200 caracteres").optional(),
  telefono: z.string().trim().max(30, "El teléfono no puede superar 30 caracteres").optional(),
});

function signAccessToken(user) {
  return jwt.sign({ sub: user.id, username: user.username }, config.jwtSecret, {
    expiresIn: config.accessTokenTtl,
    issuer: "aplicativo-java-api",
    audience: "aplicativo-java-app",
  });
}

async function issueTokens(user) {
  const accessToken = signAccessToken(user);
  const refreshToken = generateRefreshToken();
  await store.saveRefreshToken({
    userId: user.id,
    familyId: generateFamilyId(),
    tokenHash: hashToken(refreshToken),
    expiresAt: expiryTimestamp(config.refreshTokenTtlSeconds),
  });
  return { accessToken, refreshToken, user };
}

export async function register(req, res) {
  if (!config.enableRegister) {
    return res.status(403).json({ error: "El registro está deshabilitado" });
  }

  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const { username, password, fullName } = parsed.data;

  if (await store.findUserByUsername(username)) {
    return res.status(409).json({ error: "El usuario ya existe" });
  }

  const passwordHash = await bcrypt.hash(password, config.bcryptRounds);
  const user = store.toPublicUser(store.createUser({ username, passwordHash, fullName }));

  return res.status(201).json(await issueTokens(user));
}

export async function login(req, res) {
  const parsed = loginSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const { username, password } = parsed.data;
  const row = await store.findUserByUsername(username);

  // Compara siempre contra un hash (real o ficticio) para igualar tiempos.
  // verifyPassword soporta hashes bcrypt y credenciales legacy en texto plano
  // (las que genera el sistema iestp para los estudiantes).
  const valid = verifyPassword(password, row?.password_hash ?? DUMMY_HASH);

  if (!valid) {
    return res.status(401).json({ error: "Usuario o contraseña incorrectos" });
  }

  const user = store.toPublicUser(row);
  return res.json(await issueTokens(user));
}

export async function refresh(req, res) {
  const parsed = refreshSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: "Token de refresco inválido" });
  }

  const tokenHash = hashToken(parsed.data.refreshToken);
  const record = await store.findRefreshToken(tokenHash);

  if (!record) {
    return res.status(401).json({ error: "Token de refresco inválido" });
  }

  if (record.revoked_at) {
    // Reutilizar un token ya rotado indica posible robo: se revoca toda la familia.
    await store.revokeRefreshTokenFamily(record.family_id);
    return res.status(401).json({ error: "Sesión revocada" });
  }

  if (record.expires_at < Date.now()) {
    await store.revokeRefreshToken(tokenHash);
    return res.status(401).json({ error: "Sesión expirada" });
  }

  const user = store.toPublicUser(await store.findUserById(record.user_id));
  if (!user) {
    return res.status(401).json({ error: "Usuario no encontrado" });
  }

  // Rotación: el token usado se invalida y se emite uno nuevo de la misma familia.
  await store.revokeRefreshToken(tokenHash);

  const accessToken = signAccessToken(user);
  const refreshToken = generateRefreshToken();
  await store.saveRefreshToken({
    userId: user.id,
    familyId: record.family_id,
    tokenHash: hashToken(refreshToken),
    expiresAt: expiryTimestamp(config.refreshTokenTtlSeconds),
  });

  return res.json({ accessToken, refreshToken, user });
}

export async function logout(req, res) {
  const parsed = refreshSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: "Token de refresco inválido" });
  }
  await store.revokeRefreshToken(hashToken(parsed.data.refreshToken));
  return res.status(204).end();
}

export async function me(req, res) {
  const profile = await store.getProfile(req.user.id);
  return res.json({ user: { ...req.user, ...profile } });
}

export async function updateProfile(req, res) {
  const parsed = updateProfileSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const { email = "", direccion = "", telefono = "" } = parsed.data;
  const profile = await store.upsertProfile(req.user.id, { email, direccion, telefono });
  return res.json({ user: { ...req.user, ...profile } });
}
