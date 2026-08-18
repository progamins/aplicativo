import bcrypt from "bcryptjs";
import jwt from "jsonwebtoken";
import { z } from "zod";

import { config } from "../config.js";
import { createUser, findUserByUsername, toPublicUser } from "../db.js";

const credentialsSchema = z.object({
  username: z
    .string()
    .trim()
    .min(3, "El usuario debe tener al menos 3 caracteres")
    .max(50, "El usuario no puede superar 50 caracteres"),
  password: z
    .string()
    .min(6, "La contraseña debe tener al menos 6 caracteres")
    .max(100, "La contraseña no puede superar 100 caracteres"),
});

const registerSchema = credentialsSchema.extend({
  fullName: z.string().trim().max(100, "El nombre no puede superar 100 caracteres").optional().default(""),
});

function signToken(user) {
  return jwt.sign({ sub: user.id, username: user.username }, config.jwtSecret, {
    expiresIn: config.jwtExpiresIn,
  });
}

export async function register(req, res) {
  const parsed = registerSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const { username, password, fullName } = parsed.data;

  if (findUserByUsername(username)) {
    return res.status(409).json({ error: "El usuario ya existe" });
  }

  const passwordHash = await bcrypt.hash(password, config.bcryptRounds);
  const user = toPublicUser(createUser({ username, passwordHash, fullName }));

  return res.status(201).json({ token: signToken(user), user });
}

export async function login(req, res) {
  const parsed = credentialsSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const { username, password } = parsed.data;
  const row = findUserByUsername(username);

  if (!row || !(await bcrypt.compare(password, row.password_hash))) {
    return res.status(401).json({ error: "Usuario o contraseña incorrectos" });
  }

  const user = toPublicUser(row);
  return res.json({ token: signToken(user), user });
}

export function me(req, res) {
  return res.json({ user: req.user });
}
