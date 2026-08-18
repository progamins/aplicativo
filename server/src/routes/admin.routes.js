import { Router } from "express";

import {
  listAllJustificaciones,
  updateJustificacionEstado,
} from "../controllers/admin.controller.js";
import { requireAdmin, requireAuth } from "../middleware/auth.js";

const router = Router();

// Panel de administración: sesión válida + rol admin.
router.use(requireAuth, requireAdmin);

router.get("/justificaciones", listAllJustificaciones);
router.patch("/justificaciones/:id", updateJustificacionEstado);

export default router;
