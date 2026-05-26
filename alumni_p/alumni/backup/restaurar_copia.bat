@echo off
REM ===========================================================================
REM  restaurar_copia.bat  -  Restauracion de una copia COMPLETA cifrada (RNF-4)
REM  Proyecto Desarrollo Alumni
REM
REM  Uso:   restaurar_copia.bat  "ruta\a\alumni_completa_AAAAMMDD_HHMMSS.sql.enc"
REM
REM  Descifra la copia y la importa en la base de datos desarrollo_alumni.
REM  ATENCION: sobrescribe el contenido actual de la base de datos.
REM ===========================================================================
setlocal

set XAMPP=C:\xampp
set DB=desarrollo_alumni
set DBUSER=root
set PASSOPT=
set OPENSSL=%XAMPP%\apache\bin\openssl.exe
set ENCKEY=ClaveCopiaAlumni2026

if "%~1"=="" (
  echo Uso: restaurar_copia.bat "ruta\al\fichero.sql.enc"
  exit /b 1
)
if not exist "%~1" (
  echo ERROR: no existe el fichero %~1
  exit /b 1
)

set TMPSQL=%TEMP%\alumni_restore_%RANDOM%.sql

echo Descifrando copia...
"%OPENSSL%" enc -d -aes-256-cbc -pbkdf2 -in "%~1" -out "%TMPSQL%" -pass pass:%ENCKEY%
if %ERRORLEVEL% NEQ 0 (
  echo ERROR: no se pudo descifrar (clave incorrecta o fichero danado).
  exit /b 1
)

echo Importando en la base de datos %DB%...
"%XAMPP%\mysql\bin\mysql.exe" -u %DBUSER% %PASSOPT% %DB% < "%TMPSQL%"
set RES=%ERRORLEVEL%
del "%TMPSQL%"

if %RES% NEQ 0 (
  echo ERROR: fallo la importacion.
  exit /b 1
)
echo Restauracion completada correctamente.
endlocal
exit /b 0
