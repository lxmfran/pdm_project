package es.uloyola.pdm.Alumni_android.conexionServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * AlumniCookieJar
 * ---------------
 * Almacen de cookies en memoria para OkHttp.
 *
 * El backend Tomcat usa la cookie JSESSIONID para identificar la sesion
 * iniciada con LoginServlet. Sin un CookieJar, OkHttp NO reenvia las cookies
 * que llegan en las respuestas, por lo que las peticiones siguientes
 * llegarian sin sesion y el SecurityFilter las rechazaria con 401.
 *
 * Esta implementacion guarda las cookies por host y las reinyecta en cada
 * peticion al mismo host. Las cookies se pierden al cerrar la app, lo que
 * obliga a iniciar sesion en cada arranque (comportamiento aceptable para
 * una version basica).
 */
public class AlumniCookieJar implements CookieJar {

    private final Map<String, List<Cookie>> cookiesPorHost = new HashMap<>();

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookies != null && !cookies.isEmpty()) {
            cookiesPorHost.put(url.host(), new ArrayList<>(cookies));
        }
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookiesPorHost.get(url.host());
        return cookies == null ? new ArrayList<Cookie>() : new ArrayList<>(cookies);
    }

    /** Borra todas las cookies guardadas (al cerrar sesion). */
    public synchronized void limpiar() {
        cookiesPorHost.clear();
    }
}
