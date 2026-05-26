@echo off
REM ===========================================================================
REM  backup_incremental.bat  -  Copia de seguridad INCREMENTAL diaria  (RNF-4)
REM  Proyecto Desarrollo Alumni  -  BD: desarrollo_alumni (XAMPP / MariaDB)
REM
REM  La copia incremental se basa en los LOGS BINARIOS de MySQL/MariaDB, que
REM  registran todos los cambios desde la ultima copia completa.
REM
REM  - Fuerza el cierre del log binario actual (FLUSH BINARY LOGS).
REM  - Empaqueta los logs binarios y los cifra con AES-256.
REM  - Los guarda de forma redundante en dos ubicaciones.
REM
REM  REQUISITO: el log binario debe estar activado. Edita el my.ini de XAMPP
REM  (C:\xampp\mysql\bin\my.ini), seccion [mysqld], y anade:
REM        log-bin=mysql-bin
REM        binlog-format=ROW
REM        expire_logs_days=14
REM  ...y reinicia MySQL. (Ver README_COPIAS_SEGURIDAD.txt)
REM
REM  Programar: TODOS LOS DIAS a las 03:00 (ver programar_tareas.bat).
REM ===========================================================================
setlocal EnableDelayedExpansion

REM --- CONFIGURACION ---------------------------------------------------------
set XAMPP=C:\xampp
set DBUSER=root
set PASSOPT=
set DATADIR=%XAMPP%\mysql\data
set OPENSSL=%XAMPP%\apache\bin\openssl.exe
set ENCKEY=ClaveCopiaAlumni2026
set PRIMARIO=C:\backups_alumni\incrementales
set SECUNDARIO=D:\backups_alumni_redundante\incrementales
set RETENCION=30
REM ---------------------------------------------------------------------------

if not exist "%PRIMARIO%"   mkdir "%PRIMARIO%"
if not exist "%SECUNDARIO%" mkdir "%SECUNDARIO%"

for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set STAMP=%%I
set TARFILE=%PRIMARIO%\alumni_incremental_%STAMP%.tar
set ENCFILE=%PRIMARIO%\alumni_incremental_%STAMP%.tar.enc

echo [%date% %time%] Iniciando copia INCREMENTAL...

REM --- 1. Cerrar el log binario actual para asegurar la consistencia ---------
"%XAMPP%\mysql\bin\mysql.exe" -u %DBUSER% %PASSOPT% -e "FLUSH BINARY LOGS;"
if %ERRORLEVEL% NEQ 0 (
  echo ERROR: no se pudo conectar a MySQL o el log binario no esta activado.
  echo Revisa el my.ini (log-bin) - ver README_COPIAS_SEGURIDAD.txt
  exit /b 1
)

REM --- 2. Empaquetar los logs binarios --------------------------------------
REM  Los ficheros de log binario se llaman mysql-bin.000001, mysql-bin.000002...
tar -cf "%TARFILE%" -C "%DATADIR%" --wildcards "mysql-bin.0*" 2>nul
if not exist "%TARFILE%" (
  echo AVISO: no se encontraron logs binarios. Activa log-bin en my.ini.
  exit /b 1
)

REM --- 3. Cifrado AES-256 ----------------------------------------------------
"%OPENSSL%" enc -aes-256-cbc -salt -pbkdf2 -in "%TARFILE%" -out "%ENCFILE%" -pass pass:%ENCKEY%
if %ERRORLEVEL% NEQ 0 (
  echo ERROR: fallo el cifrado con OpenSSL.
  exit /b 1
)
del "%TARFILE%"

REM --- 4. Copia redundante ---------------------------------------------------
copy /Y "%ENCFILE%" "%SECUNDARIO%\" >nul

REM --- 5. Rotacion -----------------------------------------------------------
forfiles /p "%PRIMARIO%"   /m *.enc /d -%RETENCION% /c "cmd /c del @path" 2>nul
forfiles /p "%SECUNDARIO%" /m *.enc /d -%RETENCION% /c "cmd /c del @path" 2>nul

echo [%date% %time%] Copia INCREMENTAL finalizada: %ENCFILE%
endlocal
exit /b 0
