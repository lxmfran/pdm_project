package es.uloyola.pdm.Alumni_android.model;

/** Cuerpo JSON enviado a POST /LoginServlet. */
public class LoginRequest {
    private String usuario;
    private String contrasenia;

    public LoginRequest(String usuario, String contrasenia) {
        this.usuario = usuario;
        this.contrasenia = contrasenia;
    }

    public String getUsuario()            { return usuario; }
    public void   setUsuario(String v)    { this.usuario = v; }
    public String getContrasenia()        { return contrasenia; }
    public void   setContrasenia(String v){ this.contrasenia = v; }
}
