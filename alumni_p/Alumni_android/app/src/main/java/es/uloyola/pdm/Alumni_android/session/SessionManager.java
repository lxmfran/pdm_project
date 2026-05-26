package es.uloyola.pdm.Alumni_android.session;

import es.uloyola.pdm.Alumni_android.classes.Usuario;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;

/**
 * SessionManager
 * --------------
 * Singleton sencillo que recuerda QUIEN esta logueado en la app.
 *
 * No gestiona la cookie de sesion (de eso se ocupa AlumniCookieJar dentro
 * de OkHttp); aqui solo guardamos los datos de usuario/rol para que las
 * pantallas puedan saber quien es el usuario actual y mostrarle lo que
 * corresponda.
 *
 * Al hacer logout se limpia tanto el usuario como las cookies, para
 * dejar la app en estado anonimo.
 */
public class SessionManager {

    private static SessionManager instancia;

    private Usuario usuario;
    private String rol;

    private SessionManager() { }

    public static synchronized SessionManager get() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    public void iniciarSesion(Usuario usuario, String rol) {
        this.usuario = usuario;
        this.rol = rol;
    }

    public void cerrarSesion() {
        this.usuario = null;
        this.rol = null;
        RetrofitClient.getCookieJar().limpiar();
    }

    public boolean estaAutenticado() {
        return usuario != null;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getRol() {
        return rol;
    }
}
