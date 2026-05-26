# Despliegue - Proyecto Desarrollo Alumni

Guía de puesta en marcha tras implementar los requisitos no funcionales
RNF-1, RNF-4, RNF-6, RNF-7 y RNF-9.

## Requisitos previos

- **XAMPP** con MySQL/MariaDB y Apache (para OpenSSL).
- **Apache Tomcat 9 o superior** (soporta TLS 1.3).
- **JDK 8 o superior** y **Maven** para compilar el `.war`.

---

## 1. Base de datos (RNF-9)

La aplicación ya **no usa datos en memoria**: toda la persistencia es relacional
sobre la base de datos `desarrollo_alumni`.

1. Arranca **MySQL** desde el panel de control de XAMPP.
2. Importa el esquema y los datos. En phpMyAdmin (pestaña *Importar*) o por consola:

   ```
   mysql -u root < desarrollo_alumni.sql
   ```

3. **Importante:** los hashes de contraseña del script original son simulados.
   Ejecuta después `password_reset.sql` para poder iniciar sesión:

   ```
   mysql -u root desarrollo_alumni < password_reset.sql
   ```

   Tras ejecutarlo, **todos** los usuarios tienen la contraseña `Loyola2026!`.
   Usuarios de ejemplo: `mgarcia` (alumni), `afernandez` (PDI),
   `lmarin` (PTGAS), `phidalgo` (admin).

4. Si tu MySQL no usa el usuario `root` sin contraseña, edita
   `src/main/resources/db.properties` con tus credenciales.

---

## 2. Compilación y despliegue

```
mvn clean package
```

Genera `target/alumni.war`. Cópialo a la carpeta `webapps` de Tomcat.
Las dependencias (conector MySQL, jBCrypt, org.json) se empaquetan dentro del WAR.

---

## 3. HTTPS / TLS 1.3 (RNF-7)

La aplicación **obliga** a usar HTTPS (restricción `CONFIDENTIAL` en `web.xml`).
Sin el conector seguro configurado, Tomcat intentará redirigir y la app no
cargará por HTTP.

1. Genera el certificado y el keystore ejecutando `config/generar_keystore.bat`.
2. Copia el `alumni-keystore.p12` resultante a la carpeta `conf` de Tomcat.
3. Configura los conectores de `conf/server.xml` siguiendo
   `config/server-xml-conectores.txt` (conector HTTPS 8443 con TLS 1.3 y
   redirección/cierre del puerto HTTP).
4. Reinicia Tomcat y entra en `https://localhost:8443/alumni/`.

> Para una prueba rápida sin HTTPS, puedes comentar temporalmente el bloque
> `<security-constraint>` de `WEB-INF/web.xml`. En entrega final debe ir activo.

---

## 4. Copias de seguridad (RNF-4)

Carpeta `backup/`:

- `backup_completo.bat` — copia completa cifrada (semanal).
- `backup_incremental.bat` — copia incremental por logs binarios (diaria).
- `programar_tareas.bat` — programa ambas en el Programador de tareas de Windows.
- `restaurar_copia.bat` — restauración.

Consulta `backup/README_COPIAS_SEGURIDAD.txt` para la configuración (incluye
cómo activar el log binario en `my.ini`, necesario para la copia incremental).

---

## 5. PWA y accesibilidad (RNF-1, RNF-6)

- **PWA:** la aplicación incluye `manifest.json`, `service-worker.js`, iconos y
  página `offline.html`. Es instalable en Android/iOS y navegadores modernos.
  *El Service Worker requiere HTTPS para activarse* (otro motivo para el punto 3).
- **Accesibilidad WCAG 2.1 AA:** enlace "saltar al contenido", foco visible,
  objetivos táctiles de 44 px, contraste de texto reforzado y respeto a
  `prefers-reduced-motion`.
- Para auditar con **Lighthouse**: Chrome DevTools → pestaña *Lighthouse* →
  modo *Mobile* → categorías *Rendimiento*, *Accesibilidad* y *PWA*.

---

## Resumen de cambios de esta entrega

| Requisito | Estado | Implementación |
|-----------|--------|----------------|
| RNF-9 Persistencia | Hecho | 7 DAO reescritos con JDBC sobre `desarrollo_alumni` |
| RNF-7 HTTPS/TLS 1.3 | Hecho | `web.xml` + configuración de conectores y keystore |
| RNF-4 Copias de seguridad | Hecho | Scripts de backup completo/incremental cifrados |
| RNF-1 PWA | Hecho | Manifiesto, Service Worker, iconos, modo offline |
| RNF-6 Accesibilidad AA | Hecho | Mejoras de contraste, foco, teclado y semántica |

> **Dependencia de XAMPP:** la aplicación necesita MySQL arrancado. Si la base de
> datos no está disponible, los servicios responderán con error 500 indicando
> que no se pudo conectar (comportamiento esperado).
