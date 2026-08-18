import { store } from "../db.js";

export async function listCursos(req, res) {
  const cursos = await store.listCursos(req.user.id);
  res.json({ cursos });
}
