import { Router } from "express";

import { listAsistencias } from "../controllers/asistencias.controller.js";
import { getEstadisticas } from "../controllers/estadisticas.controller.js";
import {
  createJustificacion,
  listJustificaciones,
} from "../controllers/justificaciones.controller.js";
import { requireAuth } from "../middleware/auth.js";

const router = Router();

// Todas las rutas académicas requieren sesión válida (Bearer access token).
router.use(requireAuth);

router.get("/justificaciones", listJustificaciones);
router.post("/justificaciones", createJustificacion);
router.get("/asistencias", listAsistencias);
router.get("/estadisticas", getEstadisticas);

export default router;
