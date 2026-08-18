import jwt from "jsonwebtoken";

import { config } from "../config.js";
import { findUserById, toPublicUser } from "../db.js";

export function requireAuth(req, res, next) {
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;

  if (!token) {
    return res.status(401).json({ error: "Token requerido" });
  }

  try {
    const payload = jwt.verify(token, config.jwtSecret);
    const user = findUserById(payload.sub);
    if (!user) {
      return res.status(401).json({ error: "Usuario no encontrado" });
    }
    req.user = toPublicUser(user);
    next();
  } catch {
    return res.status(401).json({ error: "Token inválido o expirado" });
  }
}
