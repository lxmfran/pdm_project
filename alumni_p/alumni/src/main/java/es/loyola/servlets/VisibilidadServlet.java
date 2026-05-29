package es.loyola.servlets;

import es.loyola.classes.Alumni;
import es.loyola.dao.AlumniManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.security.SessionContext;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Preferencias de visibilidad campo a campo del propio perfil Alumni (RF-11, RI-9).
 *
 *   GET  /VisibilidadServlet  -> devuelve las preferencias actuales del usuario en sesion.
 *   POST /VisibilidadServlet  -> alias de PUT.
 *   PUT  /VisibilidadServlet  -> actualiza una o varias claves del cuerpo JSON:
 *        {
 *          "mostrarContacto":  true|false,
 *          "mostrarEmail":     true|false,
 *          "mostrarTelefono":  true|false,
 *          "mostrarCiudad":    true|false,
 *          "mostrarTrabajo":   true|false,
 *          "mostrarHobbies":   true|false
 *        }
 *
 * Solo afecta al propio perfil del usuario autenticado. PerfilServlet sigue
 * aceptando los mismos campos para edicion conjunta, pero este endpoint dedicado
 * cubre el PUT /api/users/me/visibility previsto por el PDS.
 */
@WebServlet("/VisibilidadServlet")
public class VisibilidadServlet extends HttpServlet {
    private AlumniManager alumniManager = new AlumniManager();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = SessionContext.currentUsuario(request);
        if (usuario == null || usuario.isEmpty()) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Debes iniciar sesion.");
            return;
        }
        Alumni alumni = alumniManager.findByUsuario(usuario);
        if (alumni == null) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND,
                    "Solo los perfiles Alumni gestionan visibilidad por campo.");
            return;
        }
        JSONObject prefs = new JSONObject();
        prefs.put("mostrarContacto", alumni.getMostrarContacto());
        prefs.put("mostrarEmail",    alumni.getMostrarEmail());
        prefs.put("mostrarTelefono", alumni.getMostrarTelefono());
        prefs.put("mostrarCiudad",   alumni.getMostrarCiudad());
        prefs.put("mostrarTrabajo",  alumni.getMostrarTrabajo());
        prefs.put("mostrarHobbies",  alumni.getMostrarHobbies());

        JSONObject out = new JSONObject();
        out.put("success", true);
        out.put("usuario", usuario);
        out.put("visibilidad", prefs);
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, out);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPut(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = SessionContext.currentUsuario(request);
        String rol = SessionContext.currentRol(request);
        if (usuario == null || usuario.isEmpty()) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Debes iniciar sesion.");
            return;
        }
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            Alumni alumni = alumniManager.findByUsuario(usuario);
            if (alumni == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND,
                        "Solo los perfiles Alumni gestionan visibilidad por campo.");
                return;
            }
            if (body.has("mostrarContacto"))
                alumni.setMostrarContacto(body.optBoolean("mostrarContacto", alumni.getMostrarContacto()));
            if (body.has("mostrarEmail"))
                alumni.setMostrarEmail(body.optBoolean("mostrarEmail", alumni.getMostrarEmail()));
            if (body.has("mostrarTelefono"))
                alumni.setMostrarTelefono(body.optBoolean("mostrarTelefono", alumni.getMostrarTelefono()));
            if (body.has("mostrarCiudad"))
                alumni.setMostrarCiudad(body.optBoolean("mostrarCiudad", alumni.getMostrarCiudad()));
            if (body.has("mostrarTrabajo"))
                alumni.setMostrarTrabajo(body.optBoolean("mostrarTrabajo", alumni.getMostrarTrabajo()));
            if (body.has("mostrarHobbies"))
                alumni.setMostrarHobbies(body.optBoolean("mostrarHobbies", alumni.getMostrarHobbies()));

            alumniManager.update(alumni);
            auditoria.registrar(usuario, rol, "EDITAR_VISIBILIDAD", "Alumni", usuario, "OK", null);

            JSONObject prefs = new JSONObject();
            prefs.put("mostrarContacto", alumni.getMostrarContacto());
            prefs.put("mostrarEmail",    alumni.getMostrarEmail());
            prefs.put("mostrarTelefono", alumni.getMostrarTelefono());
            prefs.put("mostrarCiudad",   alumni.getMostrarCiudad());
            prefs.put("mostrarTrabajo",  alumni.getMostrarTrabajo());
            prefs.put("mostrarHobbies",  alumni.getMostrarHobbies());

            JSONObject out = new JSONObject();
            out.put("success", true);
            out.put("usuario", usuario);
            out.put("visibilidad", prefs);
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, out);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage() == null ? "Cuerpo invalido." : e.getMessage());
        }
    }
}
