package es.loyola.servlets;

import es.loyola.classes.Usuario;
import es.loyola.dao.ActividadManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerEventos;
import es.loyola.dao.ManagerEventos.InscripcionResultado;
import es.loyola.dao.ManagerInscripciones;
import es.loyola.dao.ManagerUsuarios;
import es.loyola.security.SessionContext;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Inscripcion a eventos y actividades (RF-10, CU-004, RN-6..RN-9).
 * - POST: inscribir al usuario en sesion (o el indicado si es ADMIN).
 *         Aplica reglas: estado aprobado/publicado, plazo abierto, plazas libres,
 *         no duplicidad y rol autorizado (ALUMNI o PDI).
 * - DELETE: cancelar la inscripcion del usuario en sesion (o cualquiera si es ADMIN).
 * - GET: lista las inscripciones del usuario en sesion (o de otro si es ADMIN).
 */
@WebServlet("/InscripcionServlet")
public class InscripcionServlet extends HttpServlet {
    private ManagerEventos eventosManager = new ManagerEventos();
    private ActividadManager actividadManager = new ActividadManager();
    private ManagerInscripciones inscripciones = new ManagerInscripciones();
    private ManagerUsuarios usuariosManager = new ManagerUsuarios();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);
            String usuario = request.getParameter("usuario");
            if (usuario == null) {
                usuario = solicitante;
            }
            if (!usuario.equalsIgnoreCase(solicitante) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo puede consultar las inscripciones del usuario autenticado.");
                return;
            }
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("inscripciones", ServletJsonUtil.inscripcionArray(inscripciones.findByUsuario(usuario)));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);

            String objetivo = ServletJsonUtil.value(request, body, "usuario");
            if (isBlank(objetivo)) {
                objetivo = solicitante;
            }
            if (!objetivo.equalsIgnoreCase(solicitante) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo puede inscribirse a si mismo.");
                return;
            }
            Usuario usuarioObjetivo = usuariosManager.findByUsuario(objetivo);
            if (usuarioObjetivo == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado.");
                return;
            }

            String tipo = ServletJsonUtil.value(request, body, "tipo");
            if (tipo == null) tipo = "evento";

            Integer id = firstInt(request, body, "eventoId", "id");
            if (id == null) {
                id = firstInt(request, body, "actividadId", "id");
            }
            if (id == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el identificador.");
                return;
            }

            InscripcionResultado resultado;
            if ("actividad".equalsIgnoreCase(tipo)) {
                resultado = actividadManager.inscribir(id, usuarioObjetivo, inscripciones);
            } else if ("evento".equalsIgnoreCase(tipo)) {
                resultado = eventosManager.inscribir(id, usuarioObjetivo, inscripciones);
            } else {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "El tipo debe ser evento o actividad.");
                return;
            }

            String accionAudit = "INSCRIBIR_" + tipo.toUpperCase();
            auditoria.registrar(solicitante, rol, accionAudit, "Usuario", objetivo,
                    resultado.ok ? "OK" : "DENEGADO",
                    tipo + "#" + id + " -> " + resultado.motivo);

            if (!resultado.ok) {
                ServletJsonUtil.writeError(response, resultado.status, resultado.motivo);
                return;
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("tipo", tipo.toLowerCase());
            json.put("id", id);
            json.put("usuario", objetivo);
            json.put("mensaje", resultado.motivo);
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (NumberFormatException e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "El identificador debe ser numerico.");
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = new JSONObject();
            try {
                body = ServletJsonUtil.readJson(request);
            } catch (Exception ignored) {
            }
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);
            String objetivo = ServletJsonUtil.value(request, body, "usuario");
            if (isBlank(objetivo)) {
                objetivo = solicitante;
            }
            if (!objetivo.equalsIgnoreCase(solicitante) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo puede cancelar su propia inscripcion.");
                return;
            }
            String tipo = ServletJsonUtil.value(request, body, "tipo");
            if (tipo == null) tipo = "evento";
            Integer id = firstInt(request, body, "eventoId", "id");
            if (id == null) id = firstInt(request, body, "actividadId", "id");
            if (id == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el identificador.");
                return;
            }

            InscripcionResultado resultado;
            if ("actividad".equalsIgnoreCase(tipo)) {
                resultado = actividadManager.cancelarInscripcion(id, objetivo, inscripciones);
            } else {
                resultado = eventosManager.cancelarInscripcion(id, objetivo, inscripciones);
            }
            auditoria.registrar(solicitante, rol, "CANCELAR_INSCRIPCION_" + tipo.toUpperCase(),
                    "Usuario", objetivo, resultado.ok ? "OK" : "DENEGADO",
                    tipo + "#" + id + " -> " + resultado.motivo);
            if (!resultado.ok) {
                ServletJsonUtil.writeError(response, resultado.status, resultado.motivo);
                return;
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("tipo", tipo.toLowerCase());
            json.put("id", id);
            json.put("usuario", objetivo);
            json.put("mensaje", resultado.motivo);
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private Integer firstInt(HttpServletRequest request, JSONObject body, String first, String second) {
        Integer value = ServletJsonUtil.intValue(request, body, first);
        return value == null ? ServletJsonUtil.intValue(request, body, second) : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
