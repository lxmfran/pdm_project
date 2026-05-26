package es.uloyola.pdm.Alumni_android.conexionServer;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RetrofitClient
 * ==============
 * Singleton que centraliza la configuracion de Retrofit para toda la app.
 *
 * - BASE_URL: backend Tomcat por HTTPS (TLS 1.3). 10.0.2.2 es la IP especial
 *   con la que el emulador de Android alcanza el localhost del PC anfitrion.
 * - OkHttpClient con:
 *     - AlumniCookieJar: persiste la cookie JSESSIONID entre peticiones
 *       (sin esto el backend rechazaria las llamadas siguientes con 401).
 *     - Interceptor de logs para ver peticiones/respuestas en logcat.
 * - GsonConverterFactory: convierte automaticamente entre JSON y POJOs.
 *
 * Patron Singleton:
 * -----------------
 * El campo retrofit es estatico y se crea perezosamente la primera vez que
 * se pide. Se devuelve siempre la misma instancia para no recrear conexiones.
 */
public class RetrofitClient {

    /**
     * URL base del backend.
     *
     * NOTA: usamos HTTPS porque el backend obliga TLS 1.3 (RNF-7).
     * El certificado autofirmado del servidor se incluye en la app
     * (res/raw/alumni_cert.crt) y se declara como de confianza en
     * res/xml/network_security_config.xml.
     */
    public static final String BASE_URL = "https://10.0.2.2:8443/alumni/";

    private static Retrofit retrofit = null;
    private static final AlumniCookieJar cookieJar = new AlumniCookieJar();

    /** Devuelve la instancia unica de PvoService configurada. */
    public static PvoService getService() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(PvoService.class);
    }

    /** Acceso al gestor de cookies para limpiar la sesion al hacer logout. */
    public static AlumniCookieJar getCookieJar() {
        return cookieJar;
    }
}
