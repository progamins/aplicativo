import { mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { DatabaseSync } from "node:sqlite";

export function createSqliteStore(dbPath) {
  const path = dbPath === ":memory:" ? ":memory:" : resolve(dbPath);
  if (path !== ":memory:") mkdirSync(dirname(path), { recursive: true });

  const db = new DatabaseSync(path);

  db.exec(`
    PRAGMA journal_mode = WAL;

    CREATE TABLE IF NOT EXISTS users (
      id            INTEGER PRIMARY KEY AUTOINCREMENT,
      username      TEXT NOT NULL UNIQUE,
      password_hash TEXT NOT NULL,
      full_name     TEXT NOT NULL DEFAULT '',
      created_at    TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE TABLE IF NOT EXISTS refresh_tokens (
      id         INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id    INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
      family_id  TEXT NOT NULL,
      token_hash TEXT NOT NULL UNIQUE,
      expires_at INTEGER NOT NULL,
      revoked_at INTEGER,
      created_at TEXT NOT NULL DEFAULT (datetime('now'))
    );

    CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user ON refresh_tokens(user_id);
  `);

  const toPublicUser = (row) =>
    row
      ? { id: row.id, username: row.username, fullName: row.full_name, createdAt: row.created_at }
      : null;

  return {
    close() {
      db.close();
    },

    // ---- users ----
    findUserByUsername(username) {
      return db.prepare("SELECT * FROM users WHERE username = ?").get(username) ?? null;
    },

    findUserById(id) {
      return (
        db
          .prepare("SELECT id, username, full_name, created_at FROM users WHERE id = ?")
          .get(id) ?? null
      );
    },

    createUser({ username, passwordHash, fullName }) {
      const result = db
        .prepare("INSERT INTO users (username, password_hash, full_name) VALUES (?, ?, ?)")
        .run(username, passwordHash, fullName);
      return this.findUserById(result.lastInsertRowid);
    },

    toPublicUser,

    // ---- refresh tokens ----
    saveRefreshToken({ userId, familyId, tokenHash, expiresAt }) {
      db.prepare(
        "INSERT INTO refresh_tokens (user_id, family_id, token_hash, expires_at) VALUES (?, ?, ?, ?)"
      ).run(userId, familyId, tokenHash, expiresAt);
    },

    findRefreshToken(tokenHash) {
      return db.prepare("SELECT * FROM refresh_tokens WHERE token_hash = ?").get(tokenHash) ?? null;
    },

    revokeRefreshToken(tokenHash) {
      db.prepare("UPDATE refresh_tokens SET revoked_at = ? WHERE token_hash = ?").run(
        Date.now(),
        tokenHash
      );
    },

    revokeRefreshTokenFamily(familyId) {
      db.prepare("UPDATE refresh_tokens SET revoked_at = ? WHERE family_id = ?").run(
        Date.now(),
        familyId
      );
    },
  };
}
