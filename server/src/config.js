import "dotenv/config";

export const config = {
  port: Number(process.env.PORT || 3000),
  host: process.env.HOST || "0.0.0.0",
  jwtSecret: process.env.JWT_SECRET || "dev-only-secret-change-me",
  jwtExpiresIn: process.env.JWT_EXPIRES_IN || "24h",
  dbPath: process.env.DB_PATH || "./data/app.db",
  bcryptRounds: Number(process.env.BCRYPT_ROUNDS || 10),
};
