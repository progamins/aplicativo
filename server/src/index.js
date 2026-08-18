import { createApp } from "./app.js";
import { config } from "./config.js";

const app = createApp();

app.listen(config.port, config.host, () => {
  console.log(`🚀 API corriendo en http://${config.host}:${config.port}`);
  console.log(`   Health check: http://localhost:${config.port}/api/health`);
  console.log(`   Registro:     POST http://localhost:${config.port}/api/auth/register`);
  console.log(`   Login:        POST http://localhost:${config.port}/api/auth/login`);
});
