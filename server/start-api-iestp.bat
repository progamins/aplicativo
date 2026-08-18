@echo off
rem Arranca la API de aplicativo-java en modo iestp (integración con la BD del sistema web)
cd /d %~dp0
set DB_DRIVER=iestp
set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_USER=root
set DB_PASSWORD=
set DB_NAME=iestp
set PORT=3000
start /min "api-iestp" node src/index.js
