import { store } from "../db.js";

export async function listPagos(req, res) {
  const { pagos, ubicaciones } = await store.listPagos(req.user.id);
  res.json({ pagos, ubicaciones });
}
