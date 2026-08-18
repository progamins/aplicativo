import { createApp } from "./app.js";
import { config } from "./config.js";
import { initStore, store } from "./db.js";
import { logger } from "./logger.js";

await initStore();

const app = createApp();
const server = app.listen(config.port, config.host, () => {
  logger.info(
    { port: config.port, host: config.host, db: config.dbDriver, env: config.nodeEnv },
    "API lista"
  );
});

let shuttingDown = false;
async function shutdown(signal) {
  if (shuttingDown) return;
  shuttingDown = true;
  logger.info({ signal }, "apagando servidor…");

  const force = setTimeout(() => {
    logger.error("apagado forzado");
    process.exit(1);
  }, 10_000);
  force.unref();

  server.close(async () => {
    try {
      await store.close();
      logger.info("apagado correcto");
      process.exit(0);
    } catch (err) {
      logger.error({ err }, "error al cerrar el almacén");
      process.exit(1);
    }
  });
}

process.on("SIGTERM", () => shutdown("SIGTERM"));
process.on("SIGINT", () => shutdown("SIGINT"));
