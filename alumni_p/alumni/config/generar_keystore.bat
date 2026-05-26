@echo off
REM ===========================================================================
REM  generar_keystore.bat  -  Proyecto Desarrollo Alumni  (RNF-7)
REM
REM  Genera un certificado TLS autofirmado y su almacen de claves PKCS12
REM  para el conector HTTPS de Tomcat. Tambien lo exporta como .crt para que
REM  la app Android pueda confiar en el (network_security_config).
REM
REM  El certificado se emite valido para:
REM     localhost   (acceso desde el navegador del PC)
REM     127.0.0.1   (idem por IP local)
REM     10.0.2.2    (IP especial con la que el emulador de Android Studio
REM                  alcanza el localhost del PC anfitrion)
REM
REM  Requisito: 'keytool' viene incluido con el JDK. Si no esta en el PATH,
REM  usa la ruta completa, por ejemplo:
REM     "C:\Program Files\Java\jdk-11\bin\keytool.exe"
REM ===========================================================================

set KEYSTORE=alumni-keystore.p12
set CERT=alumni_cert.crt
set ALIAS=alumni
set STOREPASS=CambiaEstaClave2026

echo.
echo === 1/2  Generando keystore PKCS12 (%KEYSTORE%) ===

REM Si ya existia un keystore anterior se borra para evitar el aviso del alias
if exist %KEYSTORE% del /Q %KEYSTORE%

keytool -genkeypair ^
  -alias %ALIAS% ^
  -keyalg RSA ^
  -keysize 2048 ^
  -sigalg SHA256withRSA ^
  -validity 825 ^
  -storetype PKCS12 ^
  -keystore %KEYSTORE% ^
  -storepass %STOREPASS% ^
  -keypass %STOREPASS% ^
  -dname "CN=localhost, OU=Proyecto Alumni, O=Universidad Loyola Andalucia, L=Sevilla, C=ES" ^
  -ext "SAN=dns:localhost,ip:127.0.0.1,ip:10.0.2.2"

if %ERRORLEVEL% NEQ 0 (
  echo.
  echo ERROR: no se pudo generar el keystore. Revisa que 'keytool' este disponible.
  pause
  exit /b 1
)

echo.
echo === 2/2  Exportando certificado publico (%CERT%) ===

if exist %CERT% del /Q %CERT%

keytool -exportcert ^
  -alias %ALIAS% ^
  -keystore %KEYSTORE% ^
  -storepass %STOREPASS% ^
  -storetype PKCS12 ^
  -rfc ^
  -file %CERT%

if %ERRORLEVEL% NEQ 0 (
  echo ERROR: no se pudo exportar el certificado.
  pause
  exit /b 1
)

echo.
echo Listo. Se han generado:
echo   - %KEYSTORE%   (para Tomcat: copia a la carpeta 'conf')
echo   - %CERT%       (para Android: copia a Alumni_android\app\src\main\res\raw\)
echo.
echo Contrasena del keystore: %STOREPASS%   (CAMBIALA en produccion)
echo.
pause
