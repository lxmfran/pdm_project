@echo off
REM ===========================================================================
REM  backup_completo.bat  -  Copia de seguridad COMPLETA semanal  (RNF-4)
REM  Proyecto Desarrollo Alumni  -  BD: desarrollo_alumni (XAMPP / MariaDB)
REM
REM  - Vuelca toda la base de datos con mysqldump (incluye datos y estructura).
REM  - Cifra el volcado con AES-256 (RNF-4: "encriptada").
REM  - Lo guarda de forma REDUNDANTE en dos ubicaciones distintas.
REM  - Rota (purga) las copias con mas de RETENCION dias.
REM
REM  Programar: cada DOMINGO (ver programar_tareas.bat).
REM ===========================================================================
setlocal EnableDelayedExpansion

REM --- CONFIGURACION (ajusta estas rutas a tu instalacion) -------------------
set XAMPP=C:\xampp
set DB=desarrollo_alumni
set DBUSER=root
REM  Si tu MySQL tiene contrasena, pon:  set PASSOPT=--password=TU_CLAVE
set PASSOPT=
set OPENSSL=%XAMPP%\apache\bin\openssl.exe
set ENCKEY=ClaveCopiaAlumni2026
set PRIMARIO=C:\backups_alumni\completas
set SECUNDARIO=D:\backups_alumni_redundante\completas
set RETENCION=60
REM ---------------------------------------------------------------------------

if not exist "%PRIMARIO%"   mkdir "%PRIMARIO%"
if not exist "%SECUNDARIO%" mkdir "%SECUNDARIO%"

for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set STAMP=%%I
set SQLFILE=%PRIMARIO%\alumni_completa_%STAMP%.sql
set ENCFILE=%PRIMARIO%\alumni_completa_%STAMP%.sql.enc

echo [%date% %time%] Iniciando copia COMPLETA de %DB%...

REM --- 1. Volcado logico completo (--flush-logs marca el corte de binlog) -----
"%XAMPP%\mysql\bin\mysqldump.exe" -u %DBUSER% %PASSOPT% ^
  --single-transaction --routines --triggers --events --flush-logs ^
  --default-character-set=utf8mb4 %DB% > "%SQLFILE%"

if %ERRORLEVEL% NEQ 0 (
  echo ERROR: fallo mysqldump. Comprueba que XAMPP/MySQL esta arrancado.
  exit /b 1
)

REM --- 2. Cifrado AES-256 del volcado ----------------------------------------
"%OPENSSL%" enc -aes-256-cbc -salt -pbkdf2 -in "%SQLFILE%" -out "%ENCFILE%" -pass pass:%ENCKEY%
if %ERRORLEVEL% NEQ 0 (
  echo ERROR: fallo el cifrado con OpenSSL.
  exit /b 1
)
REM  Se borra el .sql en claro: solo se conserva la version cifrada.
del "%SQLFILE%"

REM --- 3. Copia redundante en la ubicacion secundaria ------------------------
copy /Y "%ENCFILE%" "%SECUNDARIO%\" >nul

REM --- 4. Rotacion: elimina copias mas antiguas que RETENCION dias -----------
forfiles /p "%PRIMARIO%"   /m *.enc /d -%RETENCION% /c "cmd /c del @path" 2>nul
forfiles /p "%SECUNDARIO%" /m *.enc /d -%RETENCION% /c "cmd /c del @path" 2>nul

echo [%date% %time%] Copia COMPLETA finalizada: %ENCFILE%
echo Redundancia en: %SECUNDARIO%
endlocal
exit /b 0
