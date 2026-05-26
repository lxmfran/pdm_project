-- =============================================================================
--  Proyecto:    Desarrollo Alumni - Universidad Loyola Andalucía
--  Documento:   Script de creación e inicialización de base de datos
--  SGBD:        MariaDB 10.x / MySQL 8.x  (compatible con XAMPP)
--  Charset:     utf8mb4 (acentos y emojis)
--  Estrategia:  Herencia JOINED (Usuario base + tablas hijas por rol)
--               Evento y Actividad fusionados en `evento` con flag es_actividad
--  Trazabilidad PDS:
--    - RI-1 a RI-11 .... cubiertos por tablas correspondientes
--    - RN-1 a RN-12 .... aplicados con FKs, CHECKs, UNIQUEs y ENUMs
--    - RF-1 a RF-13 .... la BD soporta los servicios del catálogo API
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 0. Preparación
-- -----------------------------------------------------------------------------
DROP DATABASE IF EXISTS desarrollo_alumni;
CREATE DATABASE desarrollo_alumni
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE desarrollo_alumni;

SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
SET time_zone = '+01:00';

-- =============================================================================
-- 1. ESQUEMA (DDL)
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1.1 Usuario (clase base abstracta)
--     RI-3 (parcial: identidad) y soporte de jerarquía
-- -----------------------------------------------------------------------------
CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(80)  NOT NULL,
    apellidos        VARCHAR(120) NOT NULL,
    email            VARCHAR(160) NOT NULL UNIQUE,
    rol              ENUM('ALUMNI','PDI','PTGAS','ADMIN') NOT NULL,
    estado           ENUM('PENDIENTE_ACTIVACION','ACTIVO','SUSPENDIDO','ANONIMIZADO')
                       NOT NULL DEFAULT 'PENDIENTE_ACTIVACION',
    fecha_alta       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_usuario_rol    (rol),
    INDEX idx_usuario_estado (estado)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.2 Credenciales [1..1] por Usuario
--     RI-3 (hash, no contraseña en claro). RN-1.
-- -----------------------------------------------------------------------------
CREATE TABLE credenciales (
    id_usuario           INT PRIMARY KEY,
    usuario_login        VARCHAR(80)  NOT NULL UNIQUE,
    hash_contrasena      VARCHAR(255) NOT NULL,                -- bcrypt
    token_activacion     VARCHAR(255) NULL,                    -- RF-2
    fecha_caducidad_token DATETIME    NULL,
    ultimo_acceso        DATETIME     NULL,                    -- RF-12 inactividad
    intentos_fallidos    TINYINT UNSIGNED NOT NULL DEFAULT 0,
    CONSTRAINT fk_cred_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.3 Alumni (subtipo). RI-1
-- -----------------------------------------------------------------------------
CREATE TABLE alumni (
    id_usuario        INT PRIMARY KEY,
    titulacion        VARCHAR(120) NOT NULL,
    facultad          VARCHAR(120) NOT NULL,
    ano_graduacion    SMALLINT     NOT NULL,
    campus            ENUM('SEVILLA','CORDOBA','GRANADA','ONLINE') NOT NULL,
    ciudad_residencia VARCHAR(80)  NULL,
    telefono          VARCHAR(20)  NULL,
    trabajo_actual    VARCHAR(160) NULL,
    CONSTRAINT fk_alumni_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_alumni_promocion (ano_graduacion),
    INDEX idx_alumni_titulacion (titulacion)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.4 PDI (Personal Docente e Investigador). RI-7
-- -----------------------------------------------------------------------------
CREATE TABLE pdi (
    id_usuario                INT PRIMARY KEY,
    area_trabajo              VARCHAR(120) NOT NULL,
    campus                    ENUM('SEVILLA','CORDOBA','GRANADA','ONLINE') NOT NULL,
    en_proyecto_investigacion BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pdi_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.5 PTGAS (Personal Técnico de Gestión y Apoyo a la Docencia). RI-7
-- -----------------------------------------------------------------------------
CREATE TABLE ptgas (
    id_usuario   INT PRIMARY KEY,
    departamento VARCHAR(120) NOT NULL,
    CONSTRAINT fk_ptgas_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.6 Administrador
-- -----------------------------------------------------------------------------
CREATE TABLE administrador (
    id_usuario INT PRIMARY KEY,
    nivel      ENUM('IT','DPD','GENERAL') NOT NULL DEFAULT 'GENERAL',
    CONSTRAINT fk_admin_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.7 Trabajo (histórico laboral de Alumni). RI-2
-- -----------------------------------------------------------------------------
CREATE TABLE trabajo (
    id_trabajo   INT AUTO_INCREMENT PRIMARY KEY,
    id_alumni    INT NOT NULL,
    descripcion  VARCHAR(200) NOT NULL,
    empresa      VARCHAR(120) NOT NULL,
    ciudad       VARCHAR(80)  NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin    DATE NULL,                          -- NULL = empleo actual
    CONSTRAINT fk_trabajo_alumni FOREIGN KEY (id_alumni)
        REFERENCES alumni(id_usuario) ON DELETE CASCADE,
    CONSTRAINT chk_trabajo_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    INDEX idx_trabajo_alumni (id_alumni)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.8 Hobbie (aficiones del Alumni). RI-2
-- -----------------------------------------------------------------------------
CREATE TABLE hobbie (
    id_hobbie   INT AUTO_INCREMENT PRIMARY KEY,
    id_alumni   INT NOT NULL,
    nombre      VARCHAR(80)  NOT NULL,
    descripcion VARCHAR(200) NULL,
    CONSTRAINT fk_hobbie_alumni FOREIGN KEY (id_alumni)
        REFERENCES alumni(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.9 PreferenciaPrivacidad (campo a campo por Alumni). RI-9, RN-2, RN-3
-- -----------------------------------------------------------------------------
CREATE TABLE preferencia_privacidad (
    id_pref    INT AUTO_INCREMENT PRIMARY KEY,
    id_alumni  INT NOT NULL,
    campo      VARCHAR(60) NOT NULL,   -- p.ej. 'email', 'telefono', 'trabajo_actual'
    es_visible BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_pref_alumni FOREIGN KEY (id_alumni)
        REFERENCES alumni(id_usuario) ON DELETE CASCADE,
    CONSTRAINT uk_pref_alumni_campo UNIQUE (id_alumni, campo)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.10 Evento (incluye Actividad mediante flag es_actividad). RI-4
--      RN-6: solo publicados son visibles. RN-7/8: plazo y aforo.
-- -----------------------------------------------------------------------------
CREATE TABLE evento (
    id_evento               INT AUTO_INCREMENT PRIMARY KEY,
    nombre                  VARCHAR(160) NOT NULL,
    descripcion             TEXT         NULL,
    es_actividad            BOOLEAN      NOT NULL DEFAULT FALSE,
    nivel                   ENUM('BASICO','INTERMEDIO','AVANZADO') NULL, -- actividades
    hobby_relacionado       VARCHAR(80)  NULL,                            -- actividades
    fecha_inicio            DATETIME     NOT NULL,
    fecha_fin               DATETIME     NULL,
    fecha_limite_inscripcion DATETIME    NOT NULL,
    ubicacion               VARCHAR(160) NOT NULL,
    capacidad_maxima        INT          NOT NULL,
    aforo_actual            INT          NOT NULL DEFAULT 0,
    estado                  ENUM('BORRADOR','PUBLICADO','CANCELADO','FINALIZADO')
                              NOT NULL DEFAULT 'BORRADOR',
    id_responsable          INT          NULL,   -- PTGAS/Admin que publica
    fecha_creacion          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evento_responsable FOREIGN KEY (id_responsable)
        REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    CONSTRAINT chk_evento_aforo CHECK (aforo_actual <= capacidad_maxima),
    CONSTRAINT chk_evento_plazas CHECK (capacidad_maxima > 0),
    INDEX idx_evento_estado     (estado),
    INDEX idx_evento_fecha      (fecha_inicio),
    INDEX idx_evento_tipo       (es_actividad)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.11 PropuestaEvento (RF-4, RF-7, RN-5). RI-6
--      Alumni/PDI proponen; PTGAS/Admin aprueba o rechaza.
-- -----------------------------------------------------------------------------
CREATE TABLE propuesta_evento (
    id_propuesta     INT AUTO_INCREMENT PRIMARY KEY,
    titulo           VARCHAR(160) NOT NULL,
    descripcion      TEXT         NOT NULL,
    tipo_recurso     ENUM('EVENTO','ACTIVIDAD') NOT NULL,
    fecha_sugerida   DATE         NOT NULL,
    ubicacion_sugerida VARCHAR(160) NULL,
    capacidad_sugerida INT NULL,
    id_solicitante   INT NOT NULL,    -- Alumni o PDI
    estado           ENUM('PENDIENTE','APROBADA','RECHAZADA') NOT NULL DEFAULT 'PENDIENTE',
    motivo_rechazo   VARCHAR(255) NULL,
    id_evaluador     INT          NULL,  -- PTGAS o Admin
    fecha_envio      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_decision   DATETIME     NULL,
    id_evento_generado INT NULL,         -- se rellena al publicar (RF-8)
    CONSTRAINT fk_prop_solicitante FOREIGN KEY (id_solicitante)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_prop_evaluador FOREIGN KEY (id_evaluador)
        REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    CONSTRAINT fk_prop_evento FOREIGN KEY (id_evento_generado)
        REFERENCES evento(id_evento) ON DELETE SET NULL,
    INDEX idx_prop_estado      (estado),
    INDEX idx_prop_solicitante (id_solicitante)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.12 Inscripcion (RF-10). RI-5. RN-7/8/9
-- -----------------------------------------------------------------------------
CREATE TABLE inscripcion (
    id_inscripcion    INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario        INT NOT NULL,   -- Alumni o PDI
    id_evento         INT NOT NULL,
    fecha_inscripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asistio           BOOLEAN  NOT NULL DEFAULT FALSE,
    cancelada         BOOLEAN  NOT NULL DEFAULT FALSE,
    fecha_cancelacion DATETIME NULL,
    CONSTRAINT fk_insc_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_insc_evento FOREIGN KEY (id_evento)
        REFERENCES evento(id_evento) ON DELETE CASCADE,
    CONSTRAINT uk_insc_usuario_evento UNIQUE (id_usuario, id_evento),  -- RN-9
    INDEX idx_insc_evento (id_evento)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.13 RegistroAuditoria (RN-11, RF-9). RI-8
-- -----------------------------------------------------------------------------
CREATE TABLE registro_auditoria (
    id_registro       INT AUTO_INCREMENT PRIMARY KEY,
    id_actor          INT NOT NULL,            -- normalmente un administrador
    fecha             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion            VARCHAR(120) NOT NULL,
    entidad_afectada  VARCHAR(60)  NOT NULL,
    id_entidad        INT          NULL,
    resultado         ENUM('EXITO','ERROR') NOT NULL,
    ip_origen         VARCHAR(45)  NULL,        -- soporta IPv6
    detalle           TEXT         NULL,
    CONSTRAINT fk_audit_actor FOREIGN KEY (id_actor)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_audit_fecha  (fecha),
    INDEX idx_audit_actor  (id_actor)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.14 Notificacion (RF-12 + resolución de propuestas). RI-11
-- -----------------------------------------------------------------------------
CREATE TABLE notificacion (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_destinatario INT NOT NULL,
    tipo            ENUM('INACTIVIDAD','PROPUESTA_RESUELTA','EVENTO_RECORDATORIO','SISTEMA')
                      NOT NULL,
    asunto          VARCHAR(160) NOT NULL,
    mensaje         TEXT         NOT NULL,
    fecha_envio     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida           BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notif_destinatario FOREIGN KEY (id_destinatario)
        REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    INDEX idx_notif_dest (id_destinatario),
    INDEX idx_notif_tipo (tipo)
) ENGINE=InnoDB;

-- -----------------------------------------------------------------------------
-- 1.15 IntegracionSalesforce (RI-10). [1..1] por Alumni
-- -----------------------------------------------------------------------------
CREATE TABLE integracion_salesforce (
    id_alumni            INT PRIMARY KEY,
    id_externo_sf        VARCHAR(40)  NOT NULL UNIQUE,
    token_acceso         VARCHAR(255) NULL,
    fecha_ultima_sync    DATETIME     NULL,
    estado_conexion      ENUM('OK','ERROR','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
    mensaje_ultimo_error VARCHAR(255) NULL,
    CONSTRAINT fk_sf_alumni FOREIGN KEY (id_alumni)
        REFERENCES alumni(id_usuario) ON DELETE CASCADE
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 2. DATOS DE PRUEBA (DML) - Datos sintéticos con apariencia realista
--    Hash de contraseña reutilizado: bcrypt("Loyola2026!") simulado
--    Hoy de referencia: 2026-05-19
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- 2.1 USUARIOS (base) - 32 registros
--     IDs 1-20 = ALUMNI | 21-26 = PDI | 27-30 = PTGAS | 31-32 = ADMIN
-- -----------------------------------------------------------------------------
INSERT INTO usuario (id_usuario, nombre, apellidos, email, rol, estado, fecha_alta) VALUES
-- ALUMNI
( 1,'María',       'García López',         'maria.garcia@al.uloyola.es',          'ALUMNI','ACTIVO',              '2018-07-15 10:22:00'),
( 2,'Carlos',      'Rodríguez Fernández',  'carlos.rodriguez@al.uloyola.es',      'ALUMNI','ACTIVO',              '2020-07-10 11:05:00'),
( 3,'Lucía',       'Martín Pérez',         'lucia.martin@al.uloyola.es',          'ALUMNI','ACTIVO',              '2022-07-08 09:40:00'),
( 4,'Pablo',       'Sánchez Romero',       'pablo.sanchez@al.uloyola.es',         'ALUMNI','ACTIVO',              '2019-07-12 14:15:00'),
( 5,'Andrea',      'Jiménez Ruiz',         'andrea.jimenez@al.uloyola.es',        'ALUMNI','ACTIVO',              '2021-07-09 12:30:00'),
( 6,'Javier',      'Moreno Torres',        'javier.moreno@al.uloyola.es',         'ALUMNI','ACTIVO',              '2017-07-20 16:00:00'),
( 7,'Sara',        'López Ortega',         'sara.lopez@al.uloyola.es',            'ALUMNI','ACTIVO',              '2020-07-11 10:05:00'),
( 8,'Diego',       'Ramírez Castro',       'diego.ramirez@al.uloyola.es',         'ALUMNI','ACTIVO',              '2021-07-14 09:00:00'),
( 9,'Elena',       'Hernández Vega',       'elena.hernandez@al.uloyola.es',       'ALUMNI','ACTIVO',              '2022-07-13 13:20:00'),
(10,'Miguel Ángel','Domínguez Ríos',       'miguelangel.dominguez@al.uloyola.es', 'ALUMNI','ACTIVO',              '2023-07-15 08:50:00'),
(11,'Cristina',    'Vázquez Navarro',      'cristina.vazquez@al.uloyola.es',      'ALUMNI','ACTIVO',              '2019-07-18 17:45:00'),
(12,'Alejandro',   'Gómez Serrano',        'alejandro.gomez@al.uloyola.es',       'ALUMNI','ACTIVO',              '2016-07-22 11:30:00'),
(13,'Patricia',    'Ortega Molina',        'patricia.ortega@al.uloyola.es',       'ALUMNI','SUSPENDIDO',          '2021-07-16 15:10:00'),  -- baja temporal
(14,'Roberto',     'Castro Mendoza',       'roberto.castro@al.uloyola.es',        'ALUMNI','PENDIENTE_ACTIVACION','2024-07-12 09:20:00'),  -- recién graduado
(15,'Inés',        'Delgado Aguilar',      'ines.delgado@al.uloyola.es',          'ALUMNI','ACTIVO',              '2020-07-17 10:55:00'),
(16,'Daniel',      'Vargas Núñez',         'daniel.vargas@al.uloyola.es',         'ALUMNI','ACTIVO',              '2022-07-19 14:40:00'),
(17,'Marta',       'Iglesias Carmona',     'marta.iglesias@al.uloyola.es',        'ALUMNI','ACTIVO',              '2018-07-21 12:25:00'),
(18,'Hugo',        'Pareja Reina',         'hugo.pareja@al.uloyola.es',           'ALUMNI','ACTIVO',              '2023-07-10 16:30:00'),
(19,'Claudia',     'Soler Benítez',        'claudia.soler@al.uloyola.es',         'ALUMNI','ACTIVO',              '2017-07-25 09:15:00'),
(20,'Anonimizado', 'Anonimizado',          'anonimo_20@al.uloyola.es',            'ALUMNI','ANONIMIZADO',         '2015-07-30 11:00:00'),  -- RN-10 RGPD
-- PDI
(21,'Antonio',     'Fernández Cabello',    'afernandez@uloyola.es',               'PDI',   'ACTIVO',              '2012-09-01 09:00:00'),
(22,'Isabel',      'Torres Camacho',       'itorres@uloyola.es',                  'PDI',   'ACTIVO',              '2014-09-01 09:00:00'),
(23,'Manuel',      'Salinas Quintero',     'msalinas@uloyola.es',                 'PDI',   'ACTIVO',              '2010-09-01 09:00:00'),
(24,'Beatriz',     'Cordero Linares',      'bcordero@uloyola.es',                 'PDI',   'ACTIVO',              '2016-09-01 09:00:00'),
(25,'Rafael',      'Bermúdez Cano',        'rbermudez@uloyola.es',                'PDI',   'ACTIVO',              '2019-02-01 09:00:00'),
(26,'Pilar',       'Estévez Reyes',        'pestevez@uloyola.es',                 'PDI',   'ACTIVO',              '2015-09-01 09:00:00'),
-- PTGAS
(27,'Laura',       'Marín Cabrera',        'lmarin@uloyola.es',                   'PTGAS', 'ACTIVO',              '2018-01-15 09:00:00'),
(28,'Sergio',      'Pinto Lago',           'spinto@uloyola.es',                   'PTGAS', 'ACTIVO',              '2017-06-01 09:00:00'),
(29,'Mónica',      'Ferrer Aliaga',        'mferrer@uloyola.es',                  'PTGAS', 'ACTIVO',              '2013-03-10 09:00:00'),
(30,'Tomás',       'Quiroga Bello',        'tquiroga@uloyola.es',                 'PTGAS', 'ACTIVO',              '2020-11-02 09:00:00'),
-- ADMIN
(31,'Pedro',       'Hidalgo Cruz',         'phidalgo@uloyola.es',                 'ADMIN', 'ACTIVO',              '2015-04-12 09:00:00'),
(32,'Sofía',       'Ávila Méndez',         'savila@uloyola.es',                   'ADMIN', 'ACTIVO',              '2019-09-20 09:00:00');

-- -----------------------------------------------------------------------------
-- 2.2 CREDENCIALES
--     Todos los hashes son bcrypt simulados; no son reutilizables.
-- -----------------------------------------------------------------------------
INSERT INTO credenciales (id_usuario, usuario_login, hash_contrasena, token_activacion, fecha_caducidad_token, ultimo_acceso, intentos_fallidos) VALUES
( 1,'mgarcia',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', NULL, NULL, '2026-05-18 21:15:00', 0),
( 2,'crodriguez',  '$2a$10$X4kF1nQYpC9bL7MhYwR8tuVe.G6dTr8YwR2HsLPnK4tUjkM5oeBVm', NULL, NULL, '2026-05-17 19:42:00', 0),
( 3,'lmartin',     '$2a$10$Yz5jKp9bN2eHcPqL8aRfTu.X7hM3WiBvU1JdSeRtYzKlMnOpQrAuC', NULL, NULL, '2026-05-19 08:30:00', 0),
( 4,'psanchez',    '$2a$10$Aq3vBn8mCpLkH9wRtY7eWu.G2sH5jK6lDfTrYpQvBxNzMcWeSdRfA', NULL, NULL, '2026-04-22 18:10:00', 0),
( 5,'ajimenez',    '$2a$10$Bv7nMq3pLkJh5wCrTy2eAo.H8gK4mN9rEfYsWuVxZbXcQdReFtGuI', NULL, NULL, '2026-05-15 14:25:00', 0),
( 6,'jmoreno',     '$2a$10$Cw8oNr4qMlKi6xDsUz3fBp.I9hL5nO0sFgZtXvYyAcWdReSgGuHvJ', NULL, NULL, '2025-12-03 11:00:00', 0),  -- inactivo > 6 meses
( 7,'slopez',      '$2a$10$Dx9pOs5rNmLj7yEtVa4gCq.J0iM6oP1tGhAuYwZzBdXeSfThIuJwK', NULL, NULL, '2026-05-10 09:45:00', 0),
( 8,'dramirez',    '$2a$10$Ey0qPt6sOnMk8zFuWb5hDr.K1jN7pQ2uHiBvZxAaCeYfTgUiJvKxL', NULL, NULL, '2026-05-18 22:00:00', 1),
( 9,'ehernandez',  '$2a$10$Fz1rQu7tPoNl9aGvXc6iEs.L2kO8qR3vIjCwAyBbDfZgUhVjKwLyM', NULL, NULL, '2026-05-12 17:33:00', 0),
(10,'mdominguez',  '$2a$10$Ga2sRv8uQpOm0bHwYd7jFt.M3lP9rS4wJkDxBzCcEgAhViWkLxMzN', NULL, NULL, '2026-05-19 07:15:00', 0),
(11,'cvazquez',    '$2a$10$Hb3tSw9vRqPn1cIxZe8kGu.N4mQ0sT5xKlEyCaDdFhBiWjXlMyNaO', NULL, NULL, '2026-03-08 16:00:00', 0),
(12,'agomez',      '$2a$10$Ic4uTx0wSrQo2dJyAf9lHv.O5nR1tU6yLmFzDbEeGiCjXkYmNzObP', NULL, NULL, '2026-05-16 12:50:00', 0),
(13,'portega',     '$2a$10$Jd5vUy1xTsRp3eKzBg0mIw.P6oS2uV7zMnGaEcFfHjDkYlZnOaPcQ', NULL, NULL, '2026-02-14 10:00:00', 5),  -- bloqueada
(14,'rcastro',     '$2a$10$Ke6wVz2yUtSq4fLaCh1nJx.Q7pT3vW8aNoHbFdGgIkElZmAoPbQdR', 'TKN-A8F2C9E4D1B6', '2026-05-26 23:59:00', NULL, 0),  -- pendiente
(15,'idelgado',    '$2a$10$Lf7xWa3zVuTr5gMbDi2oKy.R8qU4wX9bOpIcGeHhJlFmAnBpQcReS', NULL, NULL, '2026-05-19 09:00:00', 0),
(16,'dvargas',     '$2a$10$Mg8yXb4aWvUs6hNcEj3pLz.S9rV5xY0cPqJdHfIiKmGnBoCqRdSfT', NULL, NULL, '2026-05-14 20:18:00', 0),
(17,'miglesias',   '$2a$10$Nh9zYc5bXwVt7iOdFk4qMa.T0sW6yZ1dQrKeIgJjLnHoCpDrSeTgU', NULL, NULL, '2026-05-09 11:22:00', 0),
(18,'hpareja',     '$2a$10$Oi0aZd6cYxWu8jPeGl5rNb.U1tX7zA2eRsLfJhKkMoIpDqEsTfUhV', NULL, NULL, '2026-05-18 18:40:00', 0),
(19,'csoler',      '$2a$10$Pj1bAe7dZyXv9kQfHm6sOc.V2uY8aB3fStMgKiLlNpJqErFtUgViW', NULL, NULL, '2026-05-13 15:00:00', 0),
(20,'anon_20',     '$2a$10$ZzZzZzZzZzZzZzZzZzZzZeZzZzZzZzZzZzZzZzZzZzZzZzZzZzZzZ', NULL, NULL, NULL, 0),                                      -- anonimizada
(21,'afernandez',  '$2a$10$Qk2cBf8eAzYw0lRgIn7tPd.W3vZ9bC4gTuNhLjMmOqKrFsGuVhWjX', NULL, NULL, '2026-05-19 07:45:00', 0),
(22,'itorres',     '$2a$10$Rl3dCg9fBaZx1mShJo8uQe.X4wA0cD5hUvOiMkNnPrLsGtHvWiXkY', NULL, NULL, '2026-05-18 16:20:00', 0),
(23,'msalinas',    '$2a$10$Sm4eDh0gCbAy2nTiKp9vRf.Y5xB1dE6iVwPjNlOoQsMtHuIwXjYlZ', NULL, NULL, '2026-05-19 08:10:00', 0),
(24,'bcordero',    '$2a$10$Tn5fEi1hDcBz3oUjLq0wSg.Z6yC2eF7jWxQkOmPpRtNuIvJxYkZmA', NULL, NULL, '2026-05-17 13:30:00', 0),
(25,'rbermudez',   '$2a$10$Uo6gFj2iEdCa4pVkMr1xTh.A7zD3fG8kXyRlPnQqSuOvJwKyZlAnB', NULL, NULL, '2026-05-16 10:00:00', 0),
(26,'pestevez',    '$2a$10$Vp7hGk3jFeDb5qWlNs2yUi.B8aE4gH9lYzSmQoRrTvPwKxLzAmBoC', NULL, NULL, '2026-05-19 09:30:00', 0),
(27,'lmarin',      '$2a$10$Wq8iHl4kGfEc6rXmOt3zVj.C9bF5hI0mZaTnRpSsUwQxLyMaBnCpD', NULL, NULL, '2026-05-19 08:00:00', 0),
(28,'spinto',      '$2a$10$Xr9jIm5lHgFd7sYnPu4aWk.D0cG6iJ1nAbUoSqTtVxRyMzNbCoDqE', NULL, NULL, '2026-05-19 08:15:00', 0),
(29,'mferrer',     '$2a$10$Ys0kJn6mIhGe8tZoQv5bXl.E1dH7jK2oBcVpTrUuWyZzNaOdDpErF', NULL, NULL, '2026-05-18 17:50:00', 0),
(30,'tquiroga',    '$2a$10$Zt1lKo7nJiHf9uApRw6cYm.F2eI8kL3pCdWqUsVvXzAaObPeEqFsG', NULL, NULL, '2026-05-17 14:00:00', 0),
(31,'phidalgo',    '$2a$10$Au2mLp8oKjIg0vBqSx7dZn.G3fJ9lM4qDeXrTwYwAbBcQfFrGtHuI', NULL, NULL, '2026-05-19 09:45:00', 0),
(32,'savila',      '$2a$10$Bv3nMq9pLkJh1wCrTy8eAo.H4gK0mN5rEfYsUxBcCdDrSgHsIuJvK', NULL, NULL, '2026-05-19 08:50:00', 0);

-- -----------------------------------------------------------------------------
-- 2.3 ALUMNI
-- -----------------------------------------------------------------------------
INSERT INTO alumni (id_usuario, titulacion, facultad, ano_graduacion, campus, ciudad_residencia, telefono, trabajo_actual) VALUES
( 1,'Administración y Dirección de Empresas','Facultad de Ciencias Económicas y Empresariales',2018,'SEVILLA','Sevilla',  '+34 655 123 456','Consultora Senior en Deloitte'),
( 2,'Derecho',                                'Facultad de Derecho',                             2020,'CORDOBA','Córdoba',  '+34 656 234 567','Abogado asociado en Cuatrecasas'),
( 3,'Ingeniería del Software',                'Escuela Técnica Superior de Ingeniería',          2022,'SEVILLA','Sevilla',  '+34 657 345 678','Backend Developer en Indra'),
( 4,'Psicología',                             'Facultad de Ciencias Sociales y Humanas',         2019,'SEVILLA','Málaga',   '+34 658 456 789','Psicólogo clínico en consulta privada'),
( 5,'Comunicación',                           'Facultad de Ciencias Sociales y Humanas',         2021,'CORDOBA','Madrid',   '+34 659 567 890','Community Manager en BBVA'),
( 6,'Relaciones Internacionales',             'Facultad de Ciencias Sociales y Humanas',         2017,'SEVILLA','Bruselas', '+32 470 123 456','Asistente parlamentario - Parlamento Europeo'),
( 7,'Educación Primaria',                     'Facultad de Ciencias Sociales y Humanas',         2020,'SEVILLA','Sevilla',  '+34 660 678 901','Maestra en CEIP San Ignacio'),
( 8,'Doble Grado ADE + Derecho',              'Facultad de Ciencias Económicas y Empresariales', 2021,'CORDOBA','Madrid',   '+34 661 789 012','Analista financiero en Banco Santander'),
( 9,'Fisioterapia',                           'Facultad de Ciencias de la Salud',                2022,'SEVILLA','Sevilla',  '+34 662 890 123','Fisioterapeuta deportiva en Real Betis Balompié'),
(10,'Ingeniería del Software',                'Escuela Técnica Superior de Ingeniería',          2023,'SEVILLA','Dublín',   '+353 87 123 4567','Software Engineer en Workday'),
(11,'Trabajo Social',                         'Facultad de Ciencias Sociales y Humanas',         2019,'CORDOBA','Córdoba',  '+34 663 901 234','Trabajadora social en Cruz Roja Andalucía'),
(12,'Administración y Dirección de Empresas','Facultad de Ciencias Económicas y Empresariales', 2016,'SEVILLA','Madrid',   '+34 664 012 345','Senior Consultant en EY'),
(13,'Criminología',                           'Facultad de Derecho',                             2021,'CORDOBA','Sevilla',  '+34 665 123 456','Analista en Guardia Civil'),
(14,'Ingeniería del Software',                'Escuela Técnica Superior de Ingeniería',          2024,'SEVILLA','Sevilla',  '+34 666 234 567', NULL),
(15,'Psicología',                             'Facultad de Ciencias Sociales y Humanas',         2020,'SEVILLA','Sevilla',  '+34 667 345 678','Psicóloga de RRHH en Heineken España'),
(16,'Marketing y Relaciones Públicas',        'Facultad de Ciencias Económicas y Empresariales', 2022,'CORDOBA','Barcelona','+34 668 456 789','Marketing Specialist en Mango'),
(17,'Educación Infantil',                     'Facultad de Ciencias Sociales y Humanas',         2018,'SEVILLA','Sevilla',  '+34 669 567 890','Maestra de Educación Infantil en CEIP Andalucía'),
(18,'Administración y Dirección de Empresas','Facultad de Ciencias Económicas y Empresariales', 2023,'SEVILLA','Sevilla',  '+34 670 678 901','Trainee en KPMG Auditoría'),
(19,'Derecho',                                'Facultad de Derecho',                             2017,'CORDOBA','Madrid',   '+34 671 789 012','Abogada laboralista en Sagardoy Abogados'),
(20,'Administración y Dirección de Empresas','Facultad de Ciencias Económicas y Empresariales', 2015,'SEVILLA', NULL,        NULL,             NULL);  -- anonimizado

-- -----------------------------------------------------------------------------
-- 2.4 PDI
-- -----------------------------------------------------------------------------
INSERT INTO pdi (id_usuario, area_trabajo, campus, en_proyecto_investigacion) VALUES
(21,'Economía Financiera y Contabilidad',  'SEVILLA', TRUE),
(22,'Derecho Mercantil',                    'CORDOBA', FALSE),
(23,'Lenguajes y Sistemas Informáticos',    'SEVILLA', TRUE),
(24,'Psicología Social',                     'SEVILLA', TRUE),
(25,'Comunicación Audiovisual',              'CORDOBA', FALSE),
(26,'Didáctica y Organización Escolar',     'SEVILLA', TRUE);

-- -----------------------------------------------------------------------------
-- 2.5 PTGAS
-- -----------------------------------------------------------------------------
INSERT INTO ptgas (id_usuario, departamento) VALUES
(27,'Servicio de Comunicación e Imagen'),
(28,'Oficina Alumni'),
(29,'Secretaría General'),
(30,'Servicio de Innovación y Transformación Digital');

-- -----------------------------------------------------------------------------
-- 2.6 ADMINISTRADOR
-- -----------------------------------------------------------------------------
INSERT INTO administrador (id_usuario, nivel) VALUES
(31,'IT'),
(32,'DPD');

-- -----------------------------------------------------------------------------
-- 2.7 TRABAJO (histórico laboral - ~25 registros, varios por alumni)
-- -----------------------------------------------------------------------------
INSERT INTO trabajo (id_alumni, descripcion, empresa, ciudad, fecha_inicio, fecha_fin) VALUES
( 1,'Becaria de auditoría',                       'PwC',                       'Sevilla', '2018-09-01','2019-08-31'),
( 1,'Consultora Junior',                           'Deloitte',                  'Sevilla', '2019-09-01','2022-12-31'),
( 1,'Consultora Senior',                           'Deloitte',                  'Sevilla', '2023-01-01', NULL),
( 2,'Becario jurídico',                            'Garrigues',                 'Madrid',  '2020-09-15','2021-06-30'),
( 2,'Abogado asociado - área mercantil',           'Cuatrecasas',               'Córdoba', '2021-09-01', NULL),
( 3,'Prácticas de desarrollo',                     'Everis (NTT Data)',         'Sevilla', '2021-09-01','2022-06-30'),
( 3,'Backend Developer Java/Spring',               'Indra',                     'Sevilla', '2022-09-01', NULL),
( 4,'Psicólogo en prácticas',                      'Hospital Macarena',         'Sevilla', '2019-09-01','2020-08-31'),
( 4,'Psicólogo clínico autónomo',                  'Consulta propia',           'Málaga',  '2021-01-15', NULL),
( 5,'Becaria comunicación',                        'Atresmedia',                'Madrid',  '2021-09-01','2022-08-31'),
( 5,'Community Manager',                           'BBVA',                      'Madrid',  '2022-09-15', NULL),
( 6,'Trainee Schuman',                             'Parlamento Europeo',        'Bruselas','2018-03-01','2018-08-31'),
( 6,'Asistente parlamentario',                     'Parlamento Europeo',        'Bruselas','2019-01-01', NULL),
( 8,'Analista junior',                             'Banco Santander',           'Madrid',  '2021-09-15', NULL),
( 9,'Fisioterapeuta en prácticas',                 'Clínica Beiman',            'Sevilla', '2022-07-01','2023-06-30'),
( 9,'Fisioterapeuta deportiva',                    'Real Betis Balompié',       'Sevilla', '2023-07-01', NULL),
(10,'Junior Software Engineer',                    'Accenture',                 'Sevilla', '2023-09-01','2024-12-31'),
(10,'Software Engineer II',                        'Workday',                   'Dublín',  '2025-01-15', NULL),
(12,'Auditor junior',                              'KPMG',                      'Sevilla', '2016-09-01','2019-12-31'),
(12,'Manager auditoría',                           'EY',                        'Madrid',  '2020-01-15','2023-06-30'),
(12,'Senior Consultant',                           'EY',                        'Madrid',  '2023-07-01', NULL),
(15,'Psicóloga sanitaria en prácticas',            'Hospital Reina Sofía',      'Córdoba', '2020-09-01','2021-08-31'),
(15,'Técnica de RRHH',                             'Heineken España',           'Sevilla', '2022-01-10', NULL),
(16,'Becaria marketing',                           'Inditex - Pull&Bear',       'A Coruña','2022-09-01','2023-08-31'),
(16,'Marketing Specialist',                        'Mango',                     'Barcelona','2023-09-15', NULL),
(19,'Abogada junior',                              'Sagardoy Abogados',         'Madrid',  '2017-10-01','2020-12-31'),
(19,'Abogada laboralista',                         'Sagardoy Abogados',         'Madrid',  '2021-01-01', NULL);

-- -----------------------------------------------------------------------------
-- 2.8 HOBBIE
-- -----------------------------------------------------------------------------
INSERT INTO hobbie (id_alumni, nombre, descripcion) VALUES
( 1,'Running',         'Maratón de Sevilla en 4 ocasiones'),
( 1,'Lectura',         'Club de lectura mensual'),
( 2,'Pádel',           'Liga local los fines de semana'),
( 3,'Videojuegos',     'Desarrollo de juegos indie en Unity'),
( 3,'Senderismo',      'Sierra Norte de Sevilla'),
( 4,'Yoga',            'Practicante e instructora ocasional'),
( 5,'Fotografía',      'Especializada en reportaje social'),
( 6,'Idiomas',         'Aprendiendo neerlandés y alemán'),
( 7,'Manualidades',    'Cerámica artesanal'),
( 9,'Trail running',   'Carreras de montaña'),
(10,'Música',          'Guitarrista en grupo amateur'),
(11,'Voluntariado',    'Comedor social Cáritas'),
(12,'Golf',            'Hándicap 18'),
(15,'Mindfulness',     'Práctica diaria y retiros anuales'),
(16,'Moda sostenible', 'Blog personal sobre slow fashion'),
(17,'Teatro infantil', 'Grupo aficionado'),
(18,'Surf',            'Cádiz, fines de semana'),
(19,'Cocina',          'Curso de pastelería francesa');

-- -----------------------------------------------------------------------------
-- 2.9 PREFERENCIA_PRIVACIDAD (RN-2, RN-3, RN-4)
--     Por defecto OCULTO; cada alumni decide qué campos exponer.
-- -----------------------------------------------------------------------------
INSERT INTO preferencia_privacidad (id_alumni, campo, es_visible) VALUES
-- María García López (perfil bastante abierto, busca networking)
( 1,'email',          TRUE),
( 1,'telefono',       FALSE),
( 1,'ciudad_residencia', TRUE),
( 1,'trabajo_actual', TRUE),
( 1,'titulacion',     TRUE),
-- Carlos Rodríguez (perfil profesional)
( 2,'email',          TRUE),
( 2,'telefono',       FALSE),
( 2,'ciudad_residencia', TRUE),
( 2,'trabajo_actual', TRUE),
-- Lucía Martín (muy abierta)
( 3,'email',          TRUE),
( 3,'telefono',       TRUE),
( 3,'ciudad_residencia', TRUE),
( 3,'trabajo_actual', TRUE),
( 3,'titulacion',     TRUE),
-- Pablo Sánchez (más reservado)
( 4,'email',          FALSE),
( 4,'telefono',       FALSE),
( 4,'ciudad_residencia', FALSE),
( 4,'trabajo_actual', TRUE),
-- Andrea Jiménez
( 5,'email',          TRUE),
( 5,'trabajo_actual', TRUE),
( 5,'ciudad_residencia', TRUE),
-- Javier Moreno
( 6,'email',          TRUE),
( 6,'ciudad_residencia', TRUE),
( 6,'trabajo_actual', TRUE),
-- Sara López
( 7,'email',          FALSE),
( 7,'ciudad_residencia', TRUE),
( 7,'trabajo_actual', TRUE),
-- Diego Ramírez
( 8,'email',          TRUE),
( 8,'trabajo_actual', TRUE),
-- Elena Hernández
( 9,'email',          TRUE),
( 9,'trabajo_actual', TRUE),
( 9,'ciudad_residencia', TRUE),
-- Miguel Ángel Domínguez
(10,'email',          TRUE),
(10,'trabajo_actual', TRUE),
(10,'ciudad_residencia', TRUE),
-- Cristina Vázquez
(11,'email',          FALSE),
(11,'trabajo_actual', TRUE),
-- Alejandro Gómez
(12,'email',          TRUE),
(12,'trabajo_actual', TRUE),
(12,'telefono',       FALSE),
-- Patricia Ortega (suspendida pero conserva preferencias)
(13,'email',          FALSE),
(13,'trabajo_actual', FALSE),
-- Inés Delgado
(15,'email',          TRUE),
(15,'trabajo_actual', TRUE),
-- Daniel Vargas
(16,'email',          TRUE),
(16,'trabajo_actual', TRUE),
(16,'ciudad_residencia', TRUE),
-- Marta Iglesias (muy privada)
(17,'email',          FALSE),
(17,'telefono',       FALSE),
(17,'trabajo_actual', FALSE),
-- Hugo Pareja
(18,'email',          TRUE),
(18,'trabajo_actual', TRUE),
-- Claudia Soler
(19,'email',          TRUE),
(19,'trabajo_actual', TRUE);

-- -----------------------------------------------------------------------------
-- 2.10 EVENTO (mezcla eventos y actividades; estados variados)
--      Hoy = 2026-05-19
-- -----------------------------------------------------------------------------
INSERT INTO evento (id_evento, nombre, descripcion, es_actividad, nivel, hobby_relacionado, fecha_inicio, fecha_fin, fecha_limite_inscripcion, ubicacion, capacidad_maxima, aforo_actual, estado, id_responsable, fecha_creacion) VALUES
( 1,'Encuentro Anual Alumni Loyola 2026',
    'Reunión anual de antiguos alumnos con cena de gala, mesa redonda de exalumnos destacados y entrega de premios Alumni del Año.',
    FALSE, NULL, NULL,
    '2026-06-15 19:00:00','2026-06-15 23:30:00','2026-06-08 23:59:59',
    'Campus Sevilla - Aula Magna', 250, 11, 'PUBLICADO', 27, '2026-03-12 10:00:00'),
( 2,'Networking Tech Sevilla',
    'Encuentro informal entre alumni del ámbito tecnológico, con ponencia inicial sobre IA aplicada a la industria.',
    FALSE, NULL, NULL,
    '2026-05-28 19:30:00','2026-05-28 22:00:00','2026-05-26 23:59:59',
    'Campus Sevilla - Sala Polivalente', 80, 4, 'PUBLICADO', 28, '2026-04-20 12:30:00'),
( 3,'Workshop de Liderazgo Personal',
    'Taller práctico de habilidades directivas impartido por antiguos alumnos directivos.',
    TRUE, 'INTERMEDIO', NULL,
    '2026-06-10 17:00:00','2026-06-10 20:00:00','2026-06-05 23:59:59',
    'Campus Córdoba - Aula 1.2', 30, 3, 'PUBLICADO', 27, '2026-04-15 09:45:00'),
( 4,'Foro de Empleo Loyola 2026',
    'Feria de empleo con presencia de más de 40 empresas. Networking, entrevistas exprés y ponencias.',
    FALSE, NULL, NULL,
    '2026-04-03 09:30:00','2026-04-03 19:00:00','2026-03-30 23:59:59',
    'Campus Sevilla - Edificio Empresariales', 500, 5, 'FINALIZADO', 28, '2026-01-10 11:00:00'),
( 5,'Club de Lectura - "El Infinito en un Junco"',
    'Sesión mensual del club de lectura Alumni. Comentaremos la obra de Irene Vallejo.',
    TRUE, 'BASICO', 'Lectura',
    '2026-05-30 18:00:00','2026-05-30 20:00:00','2026-05-29 12:00:00',
    'Online (Microsoft Teams)', 25, 4, 'PUBLICADO', 28, '2026-05-02 16:00:00'),
( 6,'Curso de IA Generativa aplicada al trabajo',
    'Cuatro sesiones online sobre prompt engineering, herramientas y casos de uso profesional.',
    TRUE, 'INTERMEDIO', NULL,
    '2026-06-18 18:00:00','2026-07-09 20:00:00','2026-06-15 23:59:59',
    'Online (Zoom)', 100, 6, 'PUBLICADO', 30, '2026-04-25 14:15:00'),
( 7,'Cena de Promoción 2018',
    'Encuentro exclusivo para alumni graduados en 2018 con motivo del 8º aniversario.',
    FALSE, NULL, NULL,
    '2026-06-01 21:00:00','2026-06-02 00:30:00','2026-05-25 23:59:59',
    'Hotel Alfonso XIII - Sevilla', 60, 2, 'PUBLICADO', 28, '2026-03-30 18:00:00'),
( 8,'Mentoring para Recién Graduados 2024-2025',
    'Programa de mentoring 1:1 entre alumni con experiencia y graduados de las últimas dos promociones.',
    TRUE, 'BASICO', NULL,
    '2026-05-28 17:30:00','2026-05-28 19:30:00','2026-05-26 12:00:00',
    'Campus Sevilla - Sala Mentor', 40, 2, 'PUBLICADO', 28, '2026-04-10 10:20:00'),
( 9,'Conferencia: ESG y futuro de la empresa',
    'Ponencia magistral sobre sostenibilidad y gobierno corporativo, con coloquio posterior.',
    FALSE, NULL, NULL,
    '2026-06-05 18:00:00','2026-06-05 20:30:00','2026-06-03 23:59:59',
    'Campus Córdoba - Salón de Actos', 120, 4, 'PUBLICADO', 27, '2026-04-18 09:30:00'),
(10,'Grupo de Running Alumni - Salidas mensuales',
    'Quedadas mensuales para correr juntos por la Vega del Guadalquivir. Todos los niveles bienvenidos.',
    TRUE, 'BASICO', 'Running',
    '2026-05-31 09:00:00','2026-12-20 11:00:00','2026-12-15 23:59:59',
    'Parque del Alamillo - Sevilla', 20, 3, 'PUBLICADO', 28, '2026-05-01 08:00:00'),
(11,'Feria de Innovación Loyola',
    'Showcase de proyectos de innovación de alumni emprendedores y spin-offs universitarias.',
    FALSE, NULL, NULL,
    '2026-06-25 10:00:00','2026-06-25 19:00:00','2026-06-22 23:59:59',
    'Campus Sevilla - Edificio Polivalente', 200, 3, 'PUBLICADO', 30, '2026-05-05 11:00:00'),
(12,'Taller de Fotografía Documental',
    'Taller intensivo de fotografía documental impartido por antigua alumna.',
    TRUE, 'INTERMEDIO', 'Fotografía',
    '2026-06-12 17:00:00','2026-06-12 21:00:00','2026-06-08 23:59:59',
    'Campus Córdoba - Aula 2.4', 15, 0, 'CANCELADO', 27, '2026-04-22 13:00:00');

-- -----------------------------------------------------------------------------
-- 2.11 PROPUESTA_EVENTO
--      Mezcla de estados PENDIENTE, APROBADA (con FK a evento publicado) y RECHAZADA
-- -----------------------------------------------------------------------------
INSERT INTO propuesta_evento (id_propuesta, titulo, descripcion, tipo_recurso, fecha_sugerida, ubicacion_sugerida, capacidad_sugerida, id_solicitante, estado, motivo_rechazo, id_evaluador, fecha_envio, fecha_decision, id_evento_generado) VALUES
( 1,'Encuentro Anual Alumni Loyola 2026',
    'Propuesta de reunión anual con cena y mesa redonda; gestionada por Oficina Alumni.',
    'EVENTO','2026-06-15','Aula Magna Sevilla',250,28,'APROBADA', NULL,27,'2026-03-01 09:30:00','2026-03-10 14:00:00',1),
( 2,'Networking Tech Sevilla',
    'Sugerimos un encuentro centrado en alumni del sector TIC con ponencia sobre IA.',
    'EVENTO','2026-05-28','Sala Polivalente Sevilla',80,3,'APROBADA', NULL,27,'2026-04-12 17:20:00','2026-04-18 10:00:00',2),
( 3,'Workshop de Liderazgo Personal',
    'Taller práctico de competencias directivas para alumni junior y mid.',
    'ACTIVIDAD','2026-06-10','Aula 1.2 Córdoba',30,12,'APROBADA', NULL,27,'2026-04-05 11:00:00','2026-04-12 09:00:00',3),
( 4,'Club de Lectura "El Infinito en un Junco"',
    'Sesión mensual online comentando la obra de Irene Vallejo.',
    'ACTIVIDAD','2026-05-30','Online',25,17,'APROBADA', NULL,28,'2026-04-28 19:45:00','2026-05-01 10:00:00',5),
( 5,'Curso de IA Generativa aplicada al trabajo',
    'Cuatro sesiones online sobre prompt engineering y herramientas de IA.',
    'ACTIVIDAD','2026-06-18','Online',100,10,'APROBADA', NULL,30,'2026-04-20 22:10:00','2026-04-24 13:00:00',6),
( 6,'Cena de Promoción 2018',
    'Cena conmemorativa para promoción 2018 con motivo del 8º aniversario.',
    'EVENTO','2026-06-01','Hotel Alfonso XIII',60,1,'APROBADA', NULL,28,'2026-03-25 12:00:00','2026-03-29 09:30:00',7),
( 7,'Mentoring para Recién Graduados',
    'Programa de mentoring 1:1 entre alumni veteranos y recién graduados.',
    'ACTIVIDAD','2026-05-28','Sala Mentor Sevilla',40,12,'APROBADA', NULL,28,'2026-04-02 10:00:00','2026-04-08 14:00:00',8),
( 8,'Conferencia ESG y futuro de la empresa',
    'Conferencia magistral sobre sostenibilidad corporativa.',
    'EVENTO','2026-06-05','Salón Actos Córdoba',120,21,'APROBADA', NULL,27,'2026-04-10 16:00:00','2026-04-16 11:00:00',9),
( 9,'Grupo de Running Alumni',
    'Quedadas mensuales para correr juntos por la Vega del Guadalquivir.',
    'ACTIVIDAD','2026-05-31','Parque del Alamillo',20, 1,'APROBADA', NULL,28,'2026-04-22 21:00:00','2026-04-29 09:00:00',10),
(10,'Feria de Innovación Loyola',
    'Showcase de proyectos de alumni emprendedores y spin-offs.',
    'EVENTO','2026-06-25','Edificio Polivalente Sevilla',200,30,'APROBADA', NULL,30,'2026-04-30 09:00:00','2026-05-04 13:00:00',11),
(11,'Taller de Fotografía Documental',
    'Taller intensivo de fotografía documental.',
    'ACTIVIDAD','2026-06-12','Aula 2.4 Córdoba',15, 5,'APROBADA', NULL,27,'2026-04-18 12:30:00','2026-04-21 10:00:00',12),
(12,'Jornada de salud mental para Alumni',
    'Mesa redonda con psicólogos alumni sobre bienestar emocional postuniversitario.',
    'EVENTO','2026-07-12','Sala Polivalente Sevilla',100, 4,'PENDIENTE', NULL, NULL,'2026-05-12 18:00:00', NULL, NULL),
(13,'Hackathon Alumni IT',
    'Hackathon de 24 horas para alumni desarrolladores con premios y empresas patrocinadoras.',
    'EVENTO','2026-09-20','Edificio Empresariales Sevilla',60,10,'PENDIENTE', NULL, NULL,'2026-05-15 23:40:00', NULL, NULL),
(14,'Catas de vino Alumni Córdoba',
    'Cata mensual con bodegas Montilla-Moriles.',
    'ACTIVIDAD','2026-06-30','Centro Cultural Córdoba',25,16,'PENDIENTE', NULL, NULL,'2026-05-17 20:10:00', NULL, NULL),
(15,'Encuentro político con eurodiputados Alumni',
    'Encuentro privado para discutir oportunidades laborales en Bruselas.',
    'EVENTO','2026-07-04','Por definir',40, 6,'RECHAZADA','Falta validación con Vicerrectorado y posible sesgo de contenido político. Se sugiere replantear como mesa redonda académica.',27,'2026-04-26 11:15:00','2026-05-03 10:00:00', NULL),
(16,'Quedadas para padres alumni',
    'Encuentros entre alumni con hijos para compartir experiencias de crianza.',
    'ACTIVIDAD','2026-06-22','Parque María Luisa Sevilla',30, 7,'RECHAZADA','El alcance no se alinea con los objetivos institucionales del programa Alumni. Se recomienda canalizar por asociación externa.',28,'2026-04-30 17:00:00','2026-05-06 12:00:00', NULL),
(17,'Curso avanzado de Excel y Power BI',
    'Curso de cuatro sesiones sobre business intelligence con Excel y Power BI.',
    'ACTIVIDAD','2026-07-15','Online',50,12,'PENDIENTE', NULL, NULL,'2026-05-18 09:30:00', NULL, NULL);

-- -----------------------------------------------------------------------------
-- 2.12 INSCRIPCION (RN-9: única por usuario/evento. aforo coherente con evento)
-- -----------------------------------------------------------------------------
INSERT INTO inscripcion (id_usuario, id_evento, fecha_inscripcion, asistio, cancelada, fecha_cancelacion) VALUES
-- Evento 1: Encuentro Anual (sin celebrar aún -> asistio FALSE por defecto)
( 1, 1,'2026-03-15 09:30:00', FALSE, FALSE, NULL),
( 2, 1,'2026-03-16 11:00:00', FALSE, FALSE, NULL),
( 3, 1,'2026-03-18 19:45:00', FALSE, FALSE, NULL),
( 5, 1,'2026-04-02 12:15:00', FALSE, FALSE, NULL),
( 7, 1,'2026-04-10 14:00:00', FALSE, FALSE, NULL),
( 9, 1,'2026-04-18 17:30:00', FALSE, FALSE, NULL),
(12, 1,'2026-04-22 10:00:00', FALSE, FALSE, NULL),
(15, 1,'2026-05-02 21:00:00', FALSE, FALSE, NULL),
(17, 1,'2026-05-05 13:00:00', FALSE, FALSE, NULL),
(22, 1,'2026-05-08 09:15:00', FALSE, FALSE, NULL),  -- PDI invitado
(24, 1,'2026-05-10 19:00:00', FALSE, FALSE, NULL),  -- PDI invitado

-- Evento 2: Networking Tech
( 3, 2,'2026-04-25 18:00:00', FALSE, FALSE, NULL),
(10, 2,'2026-04-26 09:00:00', FALSE, FALSE, NULL),
(14, 2,'2026-04-28 12:30:00', FALSE, FALSE, NULL),
(23, 2,'2026-05-02 16:00:00', FALSE, FALSE, NULL),  -- PDI Ingeniería

-- Evento 3: Workshop Liderazgo
( 8, 3,'2026-04-22 11:00:00', FALSE, FALSE, NULL),
(12, 3,'2026-04-25 15:30:00', FALSE, FALSE, NULL),
(16, 3,'2026-05-01 18:00:00', FALSE, FALSE, NULL),

-- Evento 4: Foro de Empleo (YA FINALIZADO -> asistio TRUE/FALSE realista)
(14, 4,'2026-03-15 10:00:00', TRUE,  FALSE, NULL),
(18, 4,'2026-03-18 12:00:00', TRUE,  FALSE, NULL),
(10, 4,'2026-03-20 09:00:00', FALSE, FALSE, NULL),  -- no asistió
( 3, 4,'2026-03-22 14:00:00', TRUE,  FALSE, NULL),
( 9, 4,'2026-03-25 11:30:00', TRUE,  FALSE, NULL),

-- Evento 5: Club de Lectura
( 1, 5,'2026-05-08 22:00:00', FALSE, FALSE, NULL),
( 4, 5,'2026-05-10 18:30:00', FALSE, FALSE, NULL),
(17, 5,'2026-05-12 09:00:00', FALSE, FALSE, NULL),
(19, 5,'2026-05-14 20:00:00', FALSE, FALSE, NULL),

-- Evento 6: Curso IA Generativa
( 3, 6,'2026-04-28 21:00:00', FALSE, FALSE, NULL),
( 5, 6,'2026-04-30 10:00:00', FALSE, FALSE, NULL),
(10, 6,'2026-05-02 18:15:00', FALSE, FALSE, NULL),
(12, 6,'2026-05-05 14:30:00', FALSE, FALSE, NULL),
(16, 6,'2026-05-08 11:00:00', FALSE, FALSE, NULL),
(21, 6,'2026-05-12 19:00:00', FALSE, FALSE, NULL),  -- PDI

-- Evento 7: Cena Promoción 2018 (solo promoción 2018)
( 1, 7,'2026-04-08 18:00:00', FALSE, FALSE, NULL),
(17, 7,'2026-04-12 21:00:00', FALSE, FALSE, NULL),

-- Evento 8: Mentoring Recién Graduados
(14, 8,'2026-04-15 09:00:00', FALSE, FALSE, NULL),
(18, 8,'2026-04-18 19:00:00', FALSE, FALSE, NULL),
(10, 8,'2026-04-20 12:00:00', FALSE, TRUE, '2026-05-10 09:00:00'),  -- canceló

-- Evento 9: Conferencia ESG
( 2, 9,'2026-04-22 17:00:00', FALSE, FALSE, NULL),
( 8, 9,'2026-04-25 19:00:00', FALSE, FALSE, NULL),
(11, 9,'2026-04-30 11:00:00', FALSE, FALSE, NULL),
(12, 9,'2026-05-02 16:30:00', FALSE, FALSE, NULL),

-- Evento 10: Grupo de Running
( 1,10,'2026-05-05 08:00:00', FALSE, FALSE, NULL),
( 9,10,'2026-05-07 10:00:00', FALSE, FALSE, NULL),
(18,10,'2026-05-12 21:00:00', FALSE, FALSE, NULL),

-- Evento 11: Feria de Innovación
(10,11,'2026-05-10 22:00:00', FALSE, FALSE, NULL),
( 3,11,'2026-05-12 12:00:00', FALSE, FALSE, NULL),
(16,11,'2026-05-15 18:00:00', FALSE, FALSE, NULL);

-- -----------------------------------------------------------------------------
-- 2.13 NOTIFICACION
-- -----------------------------------------------------------------------------
INSERT INTO notificacion (id_destinatario, tipo, asunto, mensaje, fecha_envio, leida) VALUES
( 6,'INACTIVIDAD',
   'Echamos de menos tu actividad en Alumni Loyola',
   'Hola Javier, hemos detectado que llevas más de un año sin actualizar tu perfil ni asistir a eventos. Si lo deseas, puedes reactivar tu participación o gestionar tus preferencias de visibilidad.',
   '2026-04-15 08:00:00', FALSE),
( 4,'PROPUESTA_RESUELTA',
   'Tu propuesta está pendiente de revisión',
   'Hola Pablo, tu propuesta "Jornada de salud mental para Alumni" ha sido recibida y está siendo evaluada por el Servicio de Comunicación.',
   '2026-05-12 18:05:00', TRUE),
(10,'PROPUESTA_RESUELTA',
   'Tu propuesta "Hackathon Alumni IT" está en revisión',
   'Hola Miguel Ángel, hemos recibido tu propuesta de hackathon. En breve recibirás respuesta.',
   '2026-05-15 23:45:00', FALSE),
( 6,'PROPUESTA_RESUELTA',
   'Resolución de tu propuesta "Encuentro político con eurodiputados Alumni"',
   'Hola Javier, lamentamos comunicarte que tu propuesta no ha sido aprobada. Motivo: falta validación con Vicerrectorado y posible sesgo de contenido político.',
   '2026-05-03 10:05:00', TRUE),
( 7,'PROPUESTA_RESUELTA',
   'Resolución de tu propuesta "Quedadas para padres alumni"',
   'Hola Sara, tras la revisión, tu propuesta no ha sido aprobada por no encajar con los objetivos institucionales del programa Alumni.',
   '2026-05-06 12:05:00', TRUE),
( 1,'EVENTO_RECORDATORIO',
   'Recordatorio: Encuentro Anual Alumni el 15 de junio',
   'Hola María, te recordamos que estás inscrita en el Encuentro Anual Alumni Loyola 2026. ¡Nos vemos pronto!',
   '2026-05-15 09:00:00', TRUE),
( 3,'EVENTO_RECORDATORIO',
   'Recordatorio: Networking Tech el 28 de mayo',
   'Hola Lucía, te esperamos en el Networking Tech Sevilla. Por favor, llega 15 minutos antes para el registro.',
   '2026-05-18 10:00:00', FALSE),
(14,'SISTEMA',
   'Activa tu cuenta Alumni',
   'Hola Roberto, ¡bienvenido a la red Alumni Loyola! Activa tu cuenta antes del 26/05/2026 desde el enlace recibido en tu email institucional.',
   '2026-05-19 08:00:00', FALSE),
(13,'SISTEMA',
   'Cuenta suspendida por intentos fallidos',
   'Tu cuenta ha sido suspendida temporalmente tras superar el número de intentos fallidos. Contacta con soporte para reactivarla.',
   '2026-02-14 10:05:00', TRUE),
(10,'EVENTO_RECORDATORIO',
   'Curso IA Generativa empieza el 18 de junio',
   'Hola Miguel Ángel, ¡el curso está casi a punto! Te llegará el enlace de Zoom 24h antes.',
   '2026-05-18 12:00:00', FALSE),
(12,'EVENTO_RECORDATORIO',
   'Workshop de Liderazgo el 10 de junio',
   'Recuerda traer el cuaderno y los ejercicios previos enviados por email.',
   '2026-05-18 13:00:00', FALSE),
( 5,'SISTEMA',
   'Resumen mensual de actividad',
   'Hola Andrea, este mes 14 alumni han consultado tu perfil. Revisa tus preferencias de privacidad si quieres ajustarlas.',
   '2026-05-01 07:00:00', TRUE);

-- -----------------------------------------------------------------------------
-- 2.14 REGISTRO_AUDITORIA (RN-11)
-- -----------------------------------------------------------------------------
INSERT INTO registro_auditoria (id_actor, fecha, accion, entidad_afectada, id_entidad, resultado, ip_origen, detalle) VALUES
(31,'2026-02-14 10:04:30','SUSPENDER_USUARIO',         'usuario',           13,'EXITO','10.20.30.15',
   'Cuenta suspendida automáticamente tras 5 intentos fallidos consecutivos.'),
(31,'2026-03-08 09:12:00','ANONIMIZAR_USUARIO',        'usuario',           20,'EXITO','10.20.30.15',
   'Anonimización por solicitud RGPD del titular (ticket #DPD-2026-0014). Conservación de inscripciones históricas.'),
(32,'2026-04-12 14:30:00','CAMBIAR_ROL',                'usuario',            8,'EXITO','10.20.30.21',
   'Promoción de alumni a colaborador-mentor (rol funcional interno).'),
(31,'2026-04-18 10:02:00','APROBAR_PROPUESTA',          'propuesta_evento',   2,'EXITO','10.20.30.15',
   'Propuesta "Networking Tech Sevilla" aprobada y generación de evento ID=2.'),
(27,'2026-05-03 10:00:30','RECHAZAR_PROPUESTA',         'propuesta_evento',  15,'EXITO','10.20.30.42',
   'Rechazada propuesta "Encuentro político con eurodiputados Alumni". Motivo registrado en notificación.'),
(28,'2026-05-06 12:01:00','RECHAZAR_PROPUESTA',         'propuesta_evento',  16,'EXITO','10.20.30.45',
   'Rechazada propuesta "Quedadas para padres alumni". Motivo: alcance fuera del programa.'),
(31,'2026-05-10 08:15:00','EJECUTAR_JOB_INACTIVIDAD',   'sistema',         NULL,'EXITO','127.0.0.1',
   'InactivityReminderJob ejecutado: 1 usuario notificado (id=6).'),
(32,'2026-05-12 17:00:00','EXPORTAR_DATOS_USUARIO',     'usuario',           11,'EXITO','10.20.30.21',
   'Export de datos solicitado por el titular (RGPD - derecho de portabilidad).'),
(31,'2026-05-15 09:30:00','MODIFICAR_EVENTO',           'evento',            12,'EXITO','10.20.30.15',
   'Cambio de estado a CANCELADO por baja inscripción y enfermedad del ponente.'),
(31,'2026-05-18 22:14:00','LOGIN_FALLIDO',              'credenciales',      13,'ERROR','83.55.122.40',
   'Intento de acceso a cuenta suspendida. Bloqueo mantenido.'),
(32,'2026-05-19 08:50:00','CONSULTAR_DASHBOARD',        'sistema',         NULL,'EXITO','10.20.30.21',
   'Acceso al panel de control y consulta de métricas de inactividad.');

-- -----------------------------------------------------------------------------
-- 2.15 INTEGRACION_SALESFORCE (un subconjunto de alumni sincronizados)
-- -----------------------------------------------------------------------------
INSERT INTO integracion_salesforce (id_alumni, id_externo_sf, token_acceso, fecha_ultima_sync, estado_conexion, mensaje_ultimo_error) VALUES
( 1,'003Ax00000A1bC2','sf_tk_A1bC2_e9f4d6c3', '2026-05-18 03:00:00','OK',     NULL),
( 2,'003Ax00000A1bD3','sf_tk_A1bD3_a7c2b1d8', '2026-05-18 03:00:00','OK',     NULL),
( 3,'003Ax00000A1bE4','sf_tk_A1bE4_f3e8d9b2', '2026-05-18 03:00:00','OK',     NULL),
( 5,'003Ax00000A1bF5','sf_tk_A1bF5_c5d2e7a9', '2026-05-18 03:00:00','OK',     NULL),
( 6,'003Ax00000A1bG6','sf_tk_A1bG6_b8a3c4d1', '2026-04-20 03:00:00','ERROR', 'Email rebotado: javier.moreno@al.uloyola.es no responde desde 2025-12.'),
( 8,'003Ax00000A1bH7','sf_tk_A1bH7_e2f7a8b6', '2026-05-18 03:00:00','OK',     NULL),
(10,'003Ax00000A1bI8','sf_tk_A1bI8_d6c9b3a4', '2026-05-18 03:00:00','OK',     NULL),
(12,'003Ax00000A1bJ9','sf_tk_A1bJ9_a4e8d2c7', '2026-05-18 03:00:00','OK',     NULL),
(15,'003Ax00000A1bK0','sf_tk_A1bK0_b9f3e6d2', '2026-05-18 03:00:00','OK',     NULL),
(16,'003Ax00000A1bL1','sf_tk_A1bL1_c7a4f2e9', '2026-05-18 03:00:00','OK',     NULL),
(19,'003Ax00000A1bM2','sf_tk_A1bM2_d3e9c1b6', '2026-05-18 03:00:00','OK',     NULL),
(14,'003Ax00000A1bN3', NULL,                   NULL,                  'PENDIENTE','Cuenta pendiente de activación; sincronización postergada.');

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 3. VISTAS DE APOYO (consultas comunes del dashboard y servicios API)
-- =============================================================================

-- 3.1 Eventos próximos publicados (GET /api/events)
CREATE OR REPLACE VIEW v_eventos_publicados AS
SELECT  e.id_evento,
        e.nombre,
        e.es_actividad,
        e.fecha_inicio,
        e.fecha_limite_inscripcion,
        e.ubicacion,
        e.capacidad_maxima,
        e.aforo_actual,
        (e.capacidad_maxima - e.aforo_actual) AS plazas_libres,
        e.estado,
        CONCAT(u.nombre,' ',u.apellidos) AS responsable
FROM    evento e
LEFT JOIN usuario u ON u.id_usuario = e.id_responsable
WHERE   e.estado = 'PUBLICADO'
  AND   e.fecha_inicio >= NOW()
ORDER BY e.fecha_inicio ASC;

-- 3.2 Propuestas pendientes de evaluación (bandeja PTGAS/Admin)
CREATE OR REPLACE VIEW v_propuestas_pendientes AS
SELECT  p.id_propuesta,
        p.titulo,
        p.tipo_recurso,
        p.fecha_sugerida,
        p.fecha_envio,
        u.rol      AS rol_solicitante,
        CONCAT(u.nombre,' ',u.apellidos) AS solicitante,
        u.email    AS email_solicitante,
        DATEDIFF(NOW(), p.fecha_envio) AS dias_en_espera
FROM    propuesta_evento p
JOIN    usuario u ON u.id_usuario = p.id_solicitante
WHERE   p.estado = 'PENDIENTE'
ORDER BY p.fecha_envio ASC;

-- 3.3 Cuentas inactivas > 1 año (job RF-12)
CREATE OR REPLACE VIEW v_cuentas_inactivas AS
SELECT  u.id_usuario,
        u.nombre,
        u.apellidos,
        u.email,
        u.rol,
        c.ultimo_acceso,
        DATEDIFF(NOW(), c.ultimo_acceso) AS dias_inactivo
FROM    usuario u
JOIN    credenciales c ON c.id_usuario = u.id_usuario
WHERE   u.estado = 'ACTIVO'
  AND   c.ultimo_acceso IS NOT NULL
  AND   c.ultimo_acceso < DATE_SUB(NOW(), INTERVAL 1 YEAR)
ORDER BY c.ultimo_acceso ASC;

-- 3.4 Resumen de inscripciones por evento (panel admin)
CREATE OR REPLACE VIEW v_inscripciones_por_evento AS
SELECT  e.id_evento,
        e.nombre,
        e.estado,
        e.capacidad_maxima,
        COUNT(CASE WHEN i.cancelada = FALSE THEN 1 END) AS inscritos,
        COUNT(CASE WHEN i.cancelada = TRUE  THEN 1 END) AS cancelaciones,
        COUNT(CASE WHEN i.asistio   = TRUE  THEN 1 END) AS asistencias_reales
FROM    evento e
LEFT JOIN inscripcion i ON i.id_evento = e.id_evento
GROUP BY e.id_evento, e.nombre, e.estado, e.capacidad_maxima
ORDER BY e.fecha_inicio DESC;

-- 3.5 Métricas globales del dashboard (RF-9)
CREATE OR REPLACE VIEW v_dashboard_metricas AS
SELECT
    (SELECT COUNT(*) FROM usuario  WHERE rol='ALUMNI' AND estado='ACTIVO')              AS alumni_activos,
    (SELECT COUNT(*) FROM usuario  WHERE rol='ALUMNI' AND estado='SUSPENDIDO')          AS alumni_suspendidos,
    (SELECT COUNT(*) FROM usuario  WHERE rol='ALUMNI' AND estado='ANONIMIZADO')         AS alumni_anonimizados,
    (SELECT COUNT(*) FROM usuario  WHERE rol='ALUMNI' AND estado='PENDIENTE_ACTIVACION') AS alumni_pendientes,
    (SELECT COUNT(*) FROM evento   WHERE estado='PUBLICADO' AND fecha_inicio>=NOW())    AS eventos_proximos,
    (SELECT COUNT(*) FROM evento   WHERE estado='CANCELADO')                            AS eventos_cancelados,
    (SELECT COUNT(*) FROM propuesta_evento WHERE estado='PENDIENTE')                    AS propuestas_pendientes,
    (SELECT COUNT(*) FROM inscripcion WHERE cancelada = FALSE)                          AS inscripciones_activas,
    (SELECT COUNT(*) FROM registro_auditoria WHERE fecha >= DATE_SUB(NOW(),INTERVAL 30 DAY)) AS auditoria_30d;

-- =============================================================================
-- 4. CONSULTAS DE VERIFICACIÓN (descomenta para ejecutar tras la carga)
-- =============================================================================
-- SELECT 'usuarios'      AS tabla, COUNT(*) AS filas FROM usuario       UNION ALL
-- SELECT 'credenciales',  COUNT(*) FROM credenciales                    UNION ALL
-- SELECT 'alumni',        COUNT(*) FROM alumni                          UNION ALL
-- SELECT 'pdi',           COUNT(*) FROM pdi                             UNION ALL
-- SELECT 'ptgas',         COUNT(*) FROM ptgas                           UNION ALL
-- SELECT 'administrador', COUNT(*) FROM administrador                   UNION ALL
-- SELECT 'trabajo',       COUNT(*) FROM trabajo                         UNION ALL
-- SELECT 'hobbie',        COUNT(*) FROM hobbie                          UNION ALL
-- SELECT 'preferencia_privacidad', COUNT(*) FROM preferencia_privacidad UNION ALL
-- SELECT 'evento',        COUNT(*) FROM evento                          UNION ALL
-- SELECT 'propuesta_evento', COUNT(*) FROM propuesta_evento             UNION ALL
-- SELECT 'inscripcion',   COUNT(*) FROM inscripcion                     UNION ALL
-- SELECT 'notificacion',  COUNT(*) FROM notificacion                    UNION ALL
-- SELECT 'registro_auditoria', COUNT(*) FROM registro_auditoria         UNION ALL
-- SELECT 'integracion_salesforce', COUNT(*) FROM integracion_salesforce;
--
-- SELECT * FROM v_dashboard_metricas;
-- SELECT * FROM v_eventos_publicados;
-- SELECT * FROM v_propuestas_pendientes;

-- =============================================================================
-- FIN DEL SCRIPT
-- =============================================================================
