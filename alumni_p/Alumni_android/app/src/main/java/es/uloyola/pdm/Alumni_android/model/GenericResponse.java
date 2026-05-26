package es.uloyola.pdm.Alumni_android.model;

/**
 * GenericResponse
 * ---------------
 * Estructura comun de TODAS las respuestas del backend:
 *
 *   {"success": true|false, "error": "mensaje si falla", ...}
 *
 * Esta clase base sirve para endpoints que solo necesitan saber si la
 * operacion ha ido bien (logout, cancelar inscripcion, etc.). Para
 * respuestas con datos especificos hay clases hijas (LoginResponse,
 * BuscarAlumniResponse, ...).
 */
public class GenericResponse {

    private boolean success;
    private String  error;

    public boolean isSuccess()      { return success; }
    public void    setSuccess(boolean v) { this.success = v; }

    public String getError()        { return error; }
    public void   setError(String v) { this.error = v; }
}
