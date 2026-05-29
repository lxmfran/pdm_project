# Despliegue del proyecto Desarrollo Alumni y guia de la estructura del proyecto

## Requisitos necesarios

Antes de empezar, hace falta tener instalado:

* XAMPP (usando MySQL/MariaDB y Apache).
* Apache Tomcat 9 o superior.
* JDK 8 o superior.
* Maven para generar el archivo `.war`.

---

## 1. Configuración de la base de datos

La aplicación ya no trabaja con datos en memoria, así que toda la información se guarda en la base de datos `desarrollo_alumni`.

### Pasos:

1. Iniciar MySQL desde el panel de control de XAMPP.

2. Importar la base de datos usando el archivo `desarrollo_alumni.sql`.

Se puede hacer desde phpMyAdmin (pestaña *Importar*) o desde consola:

```bash
mysql -u root < desarrollo_alumni.sql
```

3. Después hay que ejecutar el script `password_reset.sql`.

Esto es necesario porque las contraseñas del script original eran solo de prueba y no funcionan correctamente para iniciar sesión.

```bash
mysql -u root desarrollo_alumni < password_reset.sql
```

Al ejecutarlo, todos los usuarios tendrán la contraseña:

```text
Loyola2026!
```

Algunos usuarios de ejemplo son:

* `mgarcia` → alumni
* `afernandez` → PDI
* `lmarin` → PTGAS
* `phidalgo` → administrador

4. Si MySQL tiene otro usuario o contraseña distinta de la configuración por defecto, hay que modificar el archivo:

```text
src/main/resources/db.properties
```

---

## 2. Compilación y despliegue de la aplicación

Para generar el `.war`, ejecutar:

```bash
mvn clean package
```

Después de compilar, se generará el archivo:

```text
target/alumni.war
```

Ese archivo hay que copiarlo dentro de la carpeta `webapps` de Tomcat.

Las librerías necesarias (MySQL Connector, jBCrypt y org.json) ya van incluidas dentro del WAR, así que no hace falta añadir dependencias manualmente.

---

## 3. Configuración HTTPS y TLS 1.3

La aplicación está configurada para funcionar obligatoriamente mediante HTTPS.

Si no se configura correctamente el conector seguro en Tomcat, la aplicación redirigirá automáticamente y no cargará usando HTTP normal.

### Pasos para configurarlo:

1. Ejecutar:

```text
config/generar_keystore.bat
```

Esto genera el certificado y el archivo `alumni-keystore.p12`.

2. Copiar el archivo generado dentro de la carpeta `conf` de Tomcat.

3. Configurar los conectores en:

```text
conf/server.xml
```

Tomando como referencia el archivo:

```text
config/server-xml-conectores.txt
```

Ahí se configura el puerto HTTPS 8443 con TLS 1.3 y la redirección desde HTTP.

4. Reiniciar Tomcat y acceder desde:

```text
https://localhost:8443/alumni/
```

### Nota

Si se quiere probar rápidamente sin HTTPS, se puede comentar temporalmente el bloque:

```xml
<security-constraint>
```

del archivo `WEB-INF/web.xml`.

Para la entrega final debe mantenerse activado.

---

## 4. Sistema de copias de seguridad

Dentro de la carpeta `backup/` están incluidos varios scripts para gestionar las copias de seguridad:

* `backup_completo.bat` → copia completa cifrada.
* `backup_incremental.bat` → copia incremental usando logs binarios.
* `programar_tareas.bat` → automatiza las tareas en Windows.
* `restaurar_copia.bat` → restaura una copia de seguridad.

Para terminar de configurarlo, revisar:

```text
backup/README_COPIAS_SEGURIDAD.txt
```

Ahí también se explica cómo activar el log binario en `my.ini`, necesario para las copias incrementales.




# GUÍA DE LA ESTRUCTURA DEL PROYECTO


FRONTEND(ANDROID STUDIO): alumni_p/Alumni_android

BACKEND (PROYECTO MAVEN CON .WAR):alumni_p/alumni

BASE DE DATOS (TODO LO REFERENTE A ESTA) : DB_alumni






