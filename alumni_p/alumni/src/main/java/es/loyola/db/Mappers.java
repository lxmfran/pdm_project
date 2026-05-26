package es.loyola.db;

import es.loyola.enums.ExperienciaHobbies;
import es.loyola.enums.Facultad;

/**
 * Conversores entre los valores del modelo Java y los del esquema de la BD.
 *
 * El esquema usa estados en masculino (ACTIVO, SUSPENDIDO, ANONIMIZADO) mientras
 * que el modelo Java los maneja en femenino (ACTIVA, SUSPENDIDA, ANONIMIZADA).
 * Estas funciones aislan esa diferencia en la capa DAO para no tocar los servlets.
 */
public final class Mappers {

    private Mappers() {
    }

    // --- Estado de cuenta ----------------------------------------------------

    /** Estado de BD ('ACTIVO'...) -> estado del modelo Java ('ACTIVA'...). */
    public static String estadoDesdeBd(String dbEstado) {
        if (dbEstado == null) {
            return "ACTIVA";
        }
        switch (dbEstado.toUpperCase()) {
            case "ACTIVO":
                return "ACTIVA";
            case "SUSPENDIDO":
                return "SUSPENDIDA";
            case "ANONIMIZADO":
                return "ANONIMIZADA";
            case "PENDIENTE_ACTIVACION":
                return "PENDIENTE_ACTIVACION";
            default:
                return dbEstado.toUpperCase();
        }
    }

    /** Estado del modelo Java ('ACTIVA'...) -> estado de BD ('ACTIVO'...). */
    public static String estadoHaciaBd(String javaEstado) {
        if (javaEstado == null) {
            return "ACTIVO";
        }
        switch (javaEstado.toUpperCase()) {
            case "ACTIVA":
            case "ACTIVO":
                return "ACTIVO";
            case "SUSPENDIDA":
            case "SUSPENDIDO":
                return "SUSPENDIDO";
            case "ANONIMIZADA":
            case "ANONIMIZADO":
                return "ANONIMIZADO";
            case "PENDIENTE_ACTIVACION":
                return "PENDIENTE_ACTIVACION";
            default:
                return "ACTIVO";
        }
    }

    // --- Nivel de actividad <-> ExperienciaHobbies ---------------------------

    /** ENUM 'nivel' de la BD (BASICO/INTERMEDIO/AVANZADO) -> ExperienciaHobbies. */
    public static ExperienciaHobbies nivelDesdeBd(String dbNivel) {
        if (dbNivel == null) {
            return null;
        }
        switch (dbNivel.toUpperCase()) {
            case "BASICO":
                return ExperienciaHobbies.PRINCIPIANTE;
            case "INTERMEDIO":
                return ExperienciaHobbies.INTERMEDIO;
            case "AVANZADO":
                return ExperienciaHobbies.AVANZADO;
            default:
                return null;
        }
    }

    /** ExperienciaHobbies -> ENUM 'nivel' de la BD. */
    public static String nivelHaciaBd(ExperienciaHobbies nivel) {
        if (nivel == null) {
            return null;
        }
        switch (nivel) {
            case PRINCIPIANTE:
                return "BASICO";
            case INTERMEDIO:
                return "INTERMEDIO";
            case AVANZADO:
                return "AVANZADO";
            default:
                return null;
        }
    }

    // --- Facultad (texto libre en BD) <-> enum Facultad ----------------------

    /**
     * La BD almacena la facultad como texto libre ('Facultad de Derecho'...).
     * Esta funcion hace una correspondencia de mejor esfuerzo con el enum Facultad.
     */
    public static Facultad facultadDesdeBd(String texto) {
        if (texto == null) {
            return Facultad.INGENIERIA;
        }
        String t = texto.toLowerCase();
        if (t.contains("ingenier") || t.contains("tecnica") || t.contains("técnica") || t.contains("software")) {
            return Facultad.INGENIERIA;
        }
        if (t.contains("derecho")) {
            return Facultad.DERECHO;
        }
        if (t.contains("empresar") || t.contains("administracion")
                || t.contains("administración") || t.contains("marketing")) {
            return Facultad.ADE;
        }
        if (t.contains("economic") || t.contains("económic") || t.contains("economía") || t.contains("economia")) {
            return Facultad.ECONOMICAS;
        }
        if (t.contains("salud") || t.contains("medicina")) {
            return Facultad.MEDICINA;
        }
        if (t.contains("biolog")) {
            return Facultad.BIOLOGIA;
        }
        if (t.contains("matemat") || t.contains("matemát")) {
            return Facultad.MATEMATICAS;
        }
        if (t.contains("social") || t.contains("human")) {
            return Facultad.HUMANISMO;
        }
        return Facultad.INGENIERIA;
    }

    /**
     * Palabra clave para filtrar por facultad en consultas LIKE sobre el texto de la BD.
     */
    public static String facultadKeyword(Facultad facultad) {
        if (facultad == null) {
            return "";
        }
        switch (facultad) {
            case INGENIERIA:
                return "Ingenier";
            case DERECHO:
                return "Derecho";
            case ADE:
                return "Empresar";
            case ECONOMICAS:
                return "Econ";
            case MEDICINA:
                return "Salud";
            case BIOLOGIA:
                return "Biolog";
            case MATEMATICAS:
                return "Matem";
            case HUMANISMO:
                return "Social";
            default:
                return facultad.name();
        }
    }
}
