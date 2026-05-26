package es.uloyola.pdm.Alumni_android.conexionServer;

import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.model.LoginRequest;
import es.uloyola.pdm.Alumni_android.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * PvoService
 * ----------
 * Endpoints del backend invocados por la app. El backend lee JSON, por lo que
 * todas las llamadas que envien datos usan @Body con un POJO serializado por Gson.
 */
public interface PvoService {

    /** POST /LoginServlet con body JSON {usuario, contrasenia}. */
    @POST("LoginServlet")
    Call<LoginResponse> login(@Body LoginRequest credenciales);

    /** POST /LogoutServlet (sin cuerpo). */
    @POST("LogoutServlet")
    Call<GenericResponse> logout();
}
