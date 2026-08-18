# syntax=docker/dockerfile:1

# ---------- Stage 1: dependencias ----------
FROM node:24-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev && npm cache clean --force

# ---------- Stage 2: imagen final ----------
FROM node:24-alpine
ENV NODE_ENV=production
WORKDIR /app

# Usuario sin privilegios (buena práctica de seguridad)
RUN addgroup -S app && adduser -S app -G app

COPY --from=deps --chown=app:app /app/node_modules ./node_modules
COPY --chown=app:app src ./src
COPY --chown=app:app package.json ./

# Directorio de datos de SQLite con permisos para el usuario no root
RUN mkdir -p /app/data && chown app:app /app/data

USER app
EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1:3000/api/health >/dev/null 2>&1 || exit 1

CMD ["node", "src/index.js"]
