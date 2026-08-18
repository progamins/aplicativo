import { store } from "../db.js";

export async function listHorarios(req, res) {
  const horarios = await store.listHorarios(req.user.id);
  res.json({ horarios });
}
