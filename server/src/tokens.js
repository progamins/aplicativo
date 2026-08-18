import { createHash, randomBytes } from "node:crypto";

/** Hash del token guardado en BD (nunca el token en claro). */
export function hashToken(token) {
  return createHash("sha256").update(token).digest("hex");
}

export function generateRefreshToken() {
  return randomBytes(48).toString("base64url");
}

export function generateFamilyId() {
  return randomBytes(16).toString("hex");
}

export function expiryTimestamp(ttlSeconds) {
  return Date.now() + ttlSeconds * 1000;
}
