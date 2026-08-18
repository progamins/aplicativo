import { timingSafeEqual } from "node:crypto";

import bcrypt from "bcryptjs";

function safeEqual(a, b) {
  const ba = Buffer.from(String(a));
  const bb = Buffer.from(String(b));
  if (ba.length !== bb.length) return false;
  return timingSafeEqual(ba, bb);
}

/**
 * Verifica una contraseña contra el valor almacenado.
 *
 * - Si el valor almacenado es un hash bcrypt (prefijo `$2`), se compara con bcrypt.
 * - De lo contrario se trata de una credencial legacy en texto plano: el sistema
 *   iestp (github.com/progamins/iestp) genera `usuario`/`clave` en texto plano al
 *   importar estudiantes desde Excel. La comparación es de tiempo constante.
 */
export function verifyPassword(password, stored) {
  if (!stored) return false;
  if (stored.startsWith("$2")) return bcrypt.compareSync(password, stored);
  return safeEqual(password, stored);
}
