===============================================================================
 COPIAS DE SEGURIDAD  -  Proyecto Desarrollo Alumni  (RNF-4)
===============================================================================

RNF-4 exige:
  - Una copia de seguridad INCREMENTAL diaria a las 03:00.
  - Una copia de seguridad COMPLETA semanal cada domingo.
  - Almacenamiento REDUNDANTE y CIFRADO en un servidor/disco secundario.

Esta carpeta contiene la implementacion para Windows + XAMPP:

  backup_completo.bat      Copia completa (mysqldump) cifrada con AES-256.
  backup_incremental.bat   Copia incremental basada en los logs binarios.
  programar_tareas.bat     Registra ambas tareas en el Programador de Windows.
  restaurar_copia.bat      Restaura una copia completa cifrada.


-------------------------------------------------------------------------------
 1. CONFIGURACION PREVIA
-------------------------------------------------------------------------------

a) Revisa las variables al principio de cada .bat y ajustalas a tu equipo:

     XAMPP        ruta de XAMPP            (por defecto C:\xampp)
     ENCKEY       clave de cifrado AES     (CAMBIALA y guardala a buen recaudo)
     PRIMARIO     carpeta de copias        (p.ej. C:\backups_alumni\...)
     SECUNDARIO   copia redundante         (p.ej. otro disco D:\..., NAS, unidad
                                            de red \\servidor\backups\..., etc.)

   La redundancia real se consigue apuntando SECUNDARIO a un disco fisico
   distinto o a una unidad de red separada del servidor de base de datos.

b) Para que la copia INCREMENTAL funcione hay que activar el log binario.
   Edita  C:\xampp\mysql\bin\my.ini , y en la seccion [mysqld] anade:

     log-bin=mysql-bin
     binlog-format=ROW
     expire_logs_days=14

   Guarda el fichero y reinicia MySQL desde el panel de control de XAMPP.


-------------------------------------------------------------------------------
 2. PROGRAMACION AUTOMATICA
-------------------------------------------------------------------------------

   Haz clic derecho en  programar_tareas.bat  -> "Ejecutar como administrador".

   Quedan registradas dos tareas:
     - "Alumni - Backup Incremental"  -> todos los dias 03:00
     - "Alumni - Backup Completo"     -> domingos 03:30

   Comprobar:   schtasks /query /tn "Alumni - Backup*"
   Prueba:      schtasks /run   /tn "Alumni - Backup Completo"


-------------------------------------------------------------------------------
 3. ESTRATEGIA DE RESTAURACION
-------------------------------------------------------------------------------

   - Para volver al ultimo estado semanal:
       restaurar_copia.bat "C:\backups_alumni\completas\alumni_completa_XXXX.sql.enc"

   - Para una recuperacion a un punto posterior (point-in-time): tras restaurar
     la copia completa, se aplican los logs binarios de las copias incrementales
     posteriores con la herramienta  mysqlbinlog  de MySQL/MariaDB. Ejemplo:

       openssl enc -d -aes-256-cbc -pbkdf2 -in alumni_incremental_XXXX.tar.enc ^
               -out inc.tar -pass pass:TU_CLAVE
       tar -xf inc.tar
       mysqlbinlog mysql-bin.000123 | mysql -u root desarrollo_alumni


-------------------------------------------------------------------------------
 4. CIFRADO
-------------------------------------------------------------------------------

   Todas las copias se cifran con AES-256-CBC (OpenSSL incluido en XAMPP).
   La clave esta en la variable ENCKEY de cada script: cambiala y NO la
   guardes junto a las copias. Sin esa clave las copias no se pueden restaurar.

===============================================================================
