import jwt from "jsonwebtoken";

import { config } from "../config.js";
import { store } from "../db.js";

export async function requireAuth(req, res, next) {
  const header = req.headers.authorization || "";
  const token = header.startsWith("Bearer ") ? header.slice(7) : null;

  if (!token) {
    return res.status(401).json({ error: "Token requerido" });
  }

  try {
    const payload = jwt.verify(token, config.jwtSecret, {
      issuer: "aplicativo-java-api",
      audience: "aplicativo-java-app",
    });
    const user = store.toPublicUser(await store.findUserById(payload.sub));
    if (!user) {
      return res.status(401).json({ error: "Usuario no encontrado" });
    }
    req.user = user;
    next();
  } catch {
    return res.status(401).json({ error: "Token inválido o expirado" });
  }
}

export function requireAdmin(req, res, next) {
  if (req.user?.role !== "admin") {
    return res.status(403).json({ error: "Acceso restringido a administradores" });
  }
  next();
}
