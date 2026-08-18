import { z } from "zod";

import { store } from "../db.js";

const estadoSchema = z.object({
  estado: z.enum(["aprobada", "rechazada"], "Estado inválido (usa aprobada o rechazada)"),
});

export async function listAllJustificaciones(_req, res) {
  const rows = await store.listAllJustificaciones();
  res.json({ justificaciones: rows });
}

export async function updateJustificacionEstado(req, res) {
  const id = Number(req.params.id);
  if (!Number.isInteger(id) || id <= 0) {
    return res.status(400).json({ error: "Id inválido" });
  }

  const parsed = estadoSchema.safeParse(req.body);
  if (!parsed.success) {
    return res.status(400).json({ error: parsed.error.issues[0].message });
  }

  const row = await store.setJustificacionEstado(id, parsed.data.estado);
  if (!row) {
    return res.status(404).json({ error: "Justificación no encontrada" });
  }

  res.json({ justificacion: row });
}
