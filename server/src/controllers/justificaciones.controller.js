import { z } from "zod";

import { store } from "../db.js";

const createSchema = z.object({
  motivo: z
    .string()
    .trim()
    .min(3, "El motivo debe tener al menos 3 caracteres")
    .max(100, "El motivo no puede superar 100 caracteres"),
  fecha: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, "Fecha inválida (usa AAAA-MM-DD)")
    .refine((s) => !Number.isNaN(Date.parse(`${s}T00:00:00Z`)), "Fecha inválida"),
  detalle: z
    .string()
    .trim()
    .max(500, "El detalle no puede superar 500 caracteres")
    .optional()
    .default(""),
});

export async function listJustificaciones(req, res) {
  const rows = await store.listJustificaciones(req.user.id);
  res.json({ justificaciones: rows });
}

export async function createJustificacion(req, res) {
  const parsed = createSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const { motivo, fecha, detalle } = parsed.data;
  const row = await store.createJustificacion({ userId: req.user.id, motivo, fecha, detalle });

  res.status(201).json({ justificacion: row });
}
