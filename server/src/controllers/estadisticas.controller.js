import { store } from "../db.js";

export async function getEstadisticas(req, res) {
  const [justificaciones, asistencias] = await Promise.all([
    store.listJustificaciones(req.user.id),
    store.listAsistencias(req.user.id),
  ]);

  res.json({
    estadisticas: {
      justificaciones: justificaciones.length,
      pendientes: justificaciones.filter((j) => j.estado === "pendiente").length,
      totalAsistencias: asistencias.length,
      presentes: asistencias.filter((a) => a.estado === "presente").length,
    },
  });
}
