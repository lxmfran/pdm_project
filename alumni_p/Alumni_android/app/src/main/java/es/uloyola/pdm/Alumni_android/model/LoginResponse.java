package es.uloyola.pdm.Alumni_android.model;

import es.uloyola.pdm.Alumni_android.classes.Usuario;

/**
 * LoginResponse
 * -------------
 * Respuesta de POST /LoginServlet:
 *
 *   {
 *     "success":   true,
 *     "rol":       "ALUMNI" | "PDI" | "PTGAS" | "ADMIN",
 *     "usuario":   { ...datos del usuario... },
 *     "sessionId": "ABC123..."
 *   }
 *
 * Si las credenciales son incorrectas el backend devuelve:
 *   { "success": false, "error": "Usuario o contrasena incorrectos." }
 */
public class LoginResponse extends GenericResponse {

    private String  rol;
    private Usuario usuario;
    private String  sessionId;

    public String getRol()             { return rol; }
    public void   setRol(String v)     { this.rol = v; }

    public Usuario getUsuario()        { return usuario; }
    public void    setUsuario(Usuario v) { this.usuario = v; }

    public String getSessionId()       { return sessionId; }
    public void   setSessionId(String v) { this.sessionId = v; }
}
