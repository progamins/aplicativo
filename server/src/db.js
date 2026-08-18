import { mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { DatabaseSync } from "node:sqlite";

import { config } from "./config.js";

mkdirSync(dirname(resolve(config.dbPath)), { recursive: true });

export const db = new DatabaseSync(resolve(config.dbPath));

db.exec(`
  PRAGMA journal_mode = WAL;

  CREATE TABLE IF NOT EXISTS users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    username      TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name     TEXT NOT NULL DEFAULT '',
    created_at    TEXT NOT NULL DEFAULT (datetime('now'))
  );
`);

export function findUserByUsername(username) {
  return db.prepare("SELECT * FROM users WHERE username = ?").get(username) ?? null;
}

export function findUserById(id) {
  return (
    db
      .prepare("SELECT id, username, full_name, created_at FROM users WHERE id = ?")
      .get(id) ?? null
  );
}

export function createUser({ username, passwordHash, fullName }) {
  const result = db
    .prepare("INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)")
    .run(username, passwordHash, fullName);
  return findUserById(result.lastInsertRowid);
}

/** Convierte una fila de la BD al DTO público (sin hash ni campos internos). */
export function toPublicUser(user) {
  if (!user) return null;
  return {
    id: user.id,
    username: user.username,
    fullName: user.full_name,
    createdAt: user.created_at,
  };
}
