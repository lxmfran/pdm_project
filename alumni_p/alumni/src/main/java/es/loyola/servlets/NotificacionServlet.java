package es.loyola.servlets;

import es.loyola.classes.Notificacion;
import es.loyola.dao.ManagerNotificaciones;
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
 * Bandeja de notificaciones del usuario autenticado (RI-11).
 *
 *   GET  /NotificacionServlet            -> lista mis notificaciones, recientes primero.
 *   GET  /NotificacionServlet?unread=1   -> solo cuenta no leidas.
 *   PUT  /NotificacionServlet            -> body { id: N } marca como leida.
 *
 * Requiere usuario autenticado. No expone notificaciones de otros usuarios.
 */
@WebServlet("/NotificacionServlet")
public class NotificacionServlet extends HttpServlet {
    private ManagerNotificaciones notif = new ManagerNotificaciones();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = SessionContext.currentUsuario(request);
        if (usuario == null || usuario.isEmpty()) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Debes iniciar sesion para ver tus notificaciones.");
            return;
        }
        String unread = request.getParameter("unread");
        JSONObject json = new JSONObject();
        json.put("success", true);
        if (unread != null && !unread.isEmpty()) {
            json.put("noLeidas", notif.contarNoLeidas(usuario));
        } else {
            List<Notificacion> lista = notif.findByDestinatario(usuario);
            JSONArray arr = new JSONArray();
            for (Notificacion n : lista) {
                JSONObject o = new JSONObject();
                o.put("id", n.getId());
                o.put("tipo", n.getTipo());
                o.put("asunto", n.getAsunto());
                o.put("mensaje", n.getMensaje());
                o.put("fechaEnvio", ServletJsonUtil.formatDateTime(n.getFechaEnvio()));
                o.put("leida", n.isLeida());
                arr.put(o);
            }
            json.put("total", lista.size());
            json.put("notificaciones", arr);
        }
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPut(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String usuario = SessionContext.currentUsuario(request);
        if (usuario == null || usuario.isEmpty()) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Debes iniciar sesion.");
            return;
        }
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            int id;
            if (body.has("id")) {
                id = body.getInt("id");
            } else if (request.getParameter("id") != null) {
                id = Integer.parseInt(request.getParameter("id"));
            } else {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Falta el campo id.");
                return;
            }
            boolean ok = notif.marcarLeida(id, usuario);
            JSONObject json = new JSONObject();
            json.put("success", ok);
            if (!ok) json.put("error", "Notificacion no encontrada o no es tuya.");
            ServletJsonUtil.write(response,
                    ok ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage() == null ? "Cuerpo invalido." : e.getMessage());
        }
    }
}
