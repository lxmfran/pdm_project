@echo off
REM ===========================================================================
REM  programar_tareas.bat  -  Programacion automatica de copias  (RNF-4)
REM  Proyecto Desarrollo Alumni
REM
REM  Registra en el Programador de tareas de Windows:
REM    - Copia INCREMENTAL: todos los dias a las 03:00.
REM    - Copia COMPLETA   : cada domingo a las 03:30.
REM
REM  IMPORTANTE: ejecuta este .bat como ADMINISTRADOR
REM  (clic derecho -> "Ejecutar como administrador").
REM ===========================================================================
setlocal

set CARPETA=%~dp0
set INCREMENTAL=%CARPETA%backup_incremental.bat
set COMPLETO=%CARPETA%backup_completo.bat

echo Registrando tarea: copia INCREMENTAL diaria (03:00)...
schtasks /create /f /tn "Alumni - Backup Incremental" ^
  /tr "\"%INCREMENTAL%\"" /sc daily /st 03:00 /rl HIGHEST

echo Registrando tarea: copia COMPLETA semanal (domingos 03:30)...
schtasks /create /f /tn "Alumni - Backup Completo" ^
  /tr "\"%COMPLETO%\"" /sc weekly /d SUN /st 03:30 /rl HIGHEST

echo.
echo Tareas registradas. Puedes verlas con:  schtasks /query /tn "Alumni - Backup*"
echo Para ejecutar una prueba manual:        schtasks /run /tn "Alumni - Backup Completo"
echo.
pause
endlocal
