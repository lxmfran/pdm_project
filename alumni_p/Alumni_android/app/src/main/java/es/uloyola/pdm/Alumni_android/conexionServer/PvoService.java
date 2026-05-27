package es.uloyola.pdm.Alumni_android.conexionServer;

import java.util.Map;

import es.uloyola.pdm.Alumni_android.model.ActividadResponse;
import es.uloyola.pdm.Alumni_android.model.ActividadesResponse;
import es.uloyola.pdm.Alumni_android.model.BuscarAlumniResponse;
import es.uloyola.pdm.Alumni_android.model.DashboardResponse;
import es.uloyola.pdm.Alumni_android.model.DecisionPropuestaRequest;
import es.uloyola.pdm.Alumni_android.model.EventoRequest;
import es.uloyola.pdm.Alumni_android.model.EventoResponse;
import es.uloyola.pdm.Alumni_android.model.EventosResponse;
import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.model.InscripcionRequest;
import es.uloyola.pdm.Alumni_android.model.InscripcionesResponse;
import es.uloyola.pdm.Alumni_android.model.LoginRequest;
import es.uloyola.pdm.Alumni_android.model.LoginResponse;
import es.uloyola.pdm.Alumni_android.model.PerfilResponse;
import es.uloyola.pdm.Alumni_android.model.PropuestasResponse;
import es.uloyola.pdm.Alumni_android.model.UsuarioAdminResponse;
import es.uloyola.pdm.Alumni_android.model.UsuariosListResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

/**
 * PvoService — interfaz Retrofit con TODOS los endpoints del backend.
 * Todas las POST/PUT envian JSON con @Body (no form-encoded).
 */
public interface PvoService {

    // ===== Autenticacion =====
    @POST("LoginServlet")
    Call<LoginResponse> login(@Body LoginRequest credenciales);

    @POST("LogoutServlet")
    Call<GenericResponse> logout();

    // ===== Directorio de alumni =====
    @GET("BuscarAlumniServlet")
    Call<BuscarAlumniResponse> buscarAlumni(@QueryMap Map<String, String> filtros);

    // ===== Perfil =====
    @GET("PerfilServlet")
    Call<PerfilResponse> obtenerPerfil(@Query("usuario") String usuario);

    @POST("PerfilServlet")
    Call<PerfilResponse> actualizarPerfil(@Body Map<String, Object> cambios);

    // ===== Eventos =====
    @GET("EventoServlet")
    Call<EventosResponse> listarEventos(@QueryMap Map<String, String> params);

    @GET("EventoServlet")
    Call<EventoResponse> obtenerEvento(@Query("id") int id);

    @POST("EventoServlet")
    Call<EventoResponse> crearEvento(@Body EventoRequest evento);

    @HTTP(method = "DELETE", path = "EventoServlet", hasBody = false)
    Call<GenericResponse> eliminarEvento(@Query("id") int id, @Query("accion") String accion);

    // ===== Actividades =====
    @GET("ActividadServlet")
    Call<ActividadesResponse> listarActividades(@QueryMap Map<String, String> params);

    @GET("ActividadServlet")
    Call<ActividadResponse> obtenerActividad(@Query("id") int id);

    @POST("ActividadServlet")
    Call<ActividadResponse> crearActividad(@Body EventoRequest actividad);

    // ===== Inscripciones =====
    @GET("InscripcionServlet")
    Call<InscripcionesResponse> misInscripciones();

    @POST("InscripcionServlet")
    Call<GenericResponse> inscribirse(@Body InscripcionRequest req);

    @HTTP(method = "DELETE", path = "InscripcionServlet", hasBody = true)
    Call<GenericResponse> cancelarInscripcion(@Body InscripcionRequest req);

    // ===== Propuestas =====
    @GET("PropuestaServlet")
    Call<PropuestasResponse> listarPropuestas(@Query("estado") String estado);

    @PUT("PropuestaServlet")
    Call<GenericResponse> decidirPropuesta(@Body DecisionPropuestaRequest decision);

    // ===== Panel admin =====
    @GET("DashboardServlet")
    Call<DashboardResponse> dashboard();

    // ===== Gestion de usuarios (solo ADMIN) =====
    @GET("UsuarioAdminServlet")
    Call<UsuariosListResponse> listarUsuarios();

    @POST("UsuarioAdminServlet")
    Call<UsuarioAdminResponse> crearUsuarioAdmin(@Body Map<String, Object> datos);

    @PUT("UsuarioAdminServlet")
    Call<UsuarioAdminResponse> modificarUsuarioAdmin(@Body Map<String, Object> datos);

    @HTTP(method = "DELETE", path = "UsuarioAdminServlet", hasBody = true)
    Call<GenericResponse> eliminarUsuarioAdmin(@Body Map<String, Object> datos);
}
