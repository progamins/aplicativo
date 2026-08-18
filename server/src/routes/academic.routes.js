import { Router } from "express";

import { listAsistencias } from "../controllers/asistencias.controller.js";
import { listCursos } from "../controllers/cursos.controller.js";
import { getEstadisticas } from "../controllers/estadisticas.controller.js";
import { listHorarios } from "../controllers/horarios.controller.js";
import {
  createJustificacion,
  listJustificaciones,
} from "../controllers/justificaciones.controller.js";
import { listPagos } from "../controllers/pagos.controller.js";
import { requireAuth } from "../middleware/auth.js";

const router = Router();

// Todas las rutas académicas requieren sesión válida (Bearer access token).
router.use(requireAuth);

router.get("/justificaciones", listJustificaciones);
router.post("/justificaciones", createJustificacion);
router.get("/asistencias", listAsistencias);
router.get("/estadisticas", getEstadisticas);
router.get("/pagos", listPagos);
router.get("/horarios", listHorarios);
router.get("/cursos", listCursos);

export default router;
