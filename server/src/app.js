import { randomUUID } from "node:crypto";

import compression from "compression";
import cors from "cors";
import express from "express";
import helmet from "helmet";
import pinoHttp from "pino-http";

import { config } from "./config.js";
import { logger } from "./logger.js";
import academicRoutes from "./routes/academic.routes.js";
import adminRoutes from "./routes/admin.routes.js";
import authRoutes from "./routes/auth.routes.js";

export function createApp() {
  const app = express();

  app.use(
    pinoHttp({
      logger,
      genReqId: (req, res) => req.headers["x-request-id"] || randomUUID(),
    })
  );
  app.use(helmet());
  app.use(
    cors({
      origin: config.corsOrigin === "*" ? "*" : config.corsOrigin.split(",").map((s) => s.trim()),
    })
  );
  app.use(compression());
  app.use(express.json({ limit: "32kb" }));

  app.get("/api/health", (_req, res) => {
    res.json({
      status: "ok",
      service: "aplicativo-java-api",
      version: "3.1.0",
      db: config.dbDriver,
      time: new Date().toISOString(),
    });
  });

  app.use("/api/auth", authRoutes);
  app.use("/api", academicRoutes);
  app.use("/api/admin", adminRoutes);

  app.use((req, res) => {
    res.status(404).json({ error: `Ruta no encontrada: ${req.method} ${req.path}` });
  });

  // eslint-disable-next-line no-unused-vars
  app.use((err, req, res, _next) => {
    req.log.error({ err }, "error no controlado");
    if (err.type === "entity.parse.failed") {
      return res.status(400).json({ error: "JSON inválido en el cuerpo de la petición" });
    }
    res.status(500).json({ error: "Error interno del servidor" });
  });

  return app;
}
