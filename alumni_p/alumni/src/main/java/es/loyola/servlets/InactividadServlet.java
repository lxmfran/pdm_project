package es.loyola.servlets;

import es.loyola.classes.Usuario;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerUsuarios;
import es.loyola.security.SessionContext;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Proceso de recordatorio por inactividad (RF-12, RN-12).
 *
 * - GET: lista usuarios con inactividad superior al umbral indicado (por defecto 365 dias).
 * - POST: ejecuta el job - registra notificacion en auditoria para cada inactivo y devuelve la lista
 *         para que el front-end / sistema de correo la procese. Solo ADMIN.
 */
@WebServlet("/InactividadServlet")
public class InactividadServlet extends HttpServlet {
    private ManagerUsuarios usuariosManager = new ManagerUsuarios();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Acceso restringido a administradores.");
            return;
        }
        int dias = parseInt(request.getParameter("dias"), 365);
        List<Usuario> inactivos = usuariosManager.findInactivos(dias);

        JSONArray array = new JSONArray();
        for (Usuario u : inactivos) {
            JSONObject json = new JSONObject();
            json.put("usuario", u.getUsuario());
            json.put("email", u.getEmail());
            json.put("rol", usuariosManager.getRol(u));
            json.put("estadoCuenta", u.getEstadoCuenta());
            json.put("ultimoAcceso", ServletJsonUtil.formatDateTime(u.getFechaUltimoAcceso()));
            array.put(json);
        }
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("diasUmbral", dias);
        json.put("total", inactivos.size());
        json.put("inactivos", array);
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String solicitante = SessionContext.currentUsuario(request);
        if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Acceso restringido a administradores.");
            return;
        }
        int dias = parseInt(request.getParameter("dias"), 365);
        List<Usuario> inactivos = usuariosManager.findInactivos(dias);
        int notificados = 0;
        for (Usuario u : inactivos) {
            auditoria.registrar(solicitante, "ADMIN", "NOTIFICAR_INACTIVIDAD", "Usuario",
                    u.getUsuario(), "OK", "Recordatorio enviado tras " + dias + " dias de inactividad.");
            notificados++;
        }
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("diasUmbral", dias);
        json.put("notificados", notificados);
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
