import { store } from "../db.js";

export async function listAsistencias(req, res) {
  const rows = await store.listAsistencias(req.user.id);
  res.json({ asistencias: rows });
}
