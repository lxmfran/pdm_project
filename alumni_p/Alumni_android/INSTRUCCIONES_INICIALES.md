# Alumni Android — primeros pasos

Esta app se conecta por HTTPS al backend Tomcat del proyecto `alumni/`.
Antes de probarla por primera vez hay que generar y "presentarle" el
certificado del backend. Después es ya solo dar al *Play* de Android Studio.

## 1. Generar el certificado (una sola vez)

El certificado anterior valía sólo para `localhost`/`127.0.0.1`. Para que el
emulador (que llega por `10.0.2.2`) confíe en él, hay que regenerarlo. El
script ya está actualizado.

1. Detén Tomcat desde el panel de XAMPP.
2. Abre `cmd` y ejecuta:
   ```
   cd /d C:\Users\LXMFRAN\Desktop\pdm_project-main\pdm_project-main\alumni_p\alumni\config
   generar_keystore.bat
   ```
   Genera dos ficheros:
   - `alumni-keystore.p12`  → para Tomcat
   - `alumni_cert.crt`      → para la app Android

3. **Copia** `alumni-keystore.p12` a `C:\xampp\tomcat\conf\` (sustituyendo el
   anterior si existe). Reinicia Tomcat.

4. **Re-confiar el certificado en Windows**: como el `.crt` ha cambiado,
   vuelve a hacer doble clic en `alumni_cert.crt` → *Instalar certificado*
   → *Usuario actual* → *Entidades de certificación raíz de confianza*.
   Cierra Chrome y vuelve a abrirlo para que coja el nuevo.

## 2. Colocar el certificado en la app Android

Copia `alumni_cert.crt` (el que acaba de generarse) a:

```
Alumni_android\app\src\main\res\raw\alumni_cert.crt
```

(reemplaza el `readme.txt` que hay ahí). Es lo que permite a la app confiar
en el certificado autofirmado del backend (`network_security_config.xml`).

## 3. Sincronizar Gradle y arrancar

1. Abre Android Studio sobre `Alumni_android/`. Acepta el aviso de
   *"Gradle files have changed"* — sincronizará y descargará Retrofit, OkHttp
   y el conversor Gson.
2. Comprueba que el backend está arriba (`https://localhost:8443/alumni/`
   debería cargar la web sin avisos).
3. Pulsa *Run app* en el emulador.

## 4. Probar el login

La pantalla viene precargada con `mgarcia` / `Loyola2026!`. También hay cuatro
botones de acceso rápido (Alumni / PDI / PTGAS / Admin). Tras un login correcto
debe llevarte a la pantalla "Home" con tu nombre y tu rol.

## Solución de problemas

- **`SSLHandshakeException` / "trust anchor not found"** → falta el `.crt` en
  `res/raw/` o no se ha sincronizado Gradle. Vuelve al paso 2.
- **`ECONNREFUSED`** → Tomcat no está arrancado o el conector HTTPS no está
  configurado en `server.xml`.
- **"Usuario o contraseña incorrectos"** → no se ha ejecutado
  `password_reset.sql` después del `desarrollo_alumni.sql`.

Cuando confirmes que el login funciona, seguimos con las siguientes pantallas
(directorio, perfil, eventos, etc.).
