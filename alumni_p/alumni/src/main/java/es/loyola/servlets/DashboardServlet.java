package es.loyola.servlets;

import es.loyola.classes.Usuario;
import es.loyola.dao.ActividadManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerEventos;
import es.loyola.dao.ManagerInscripciones;
import es.loyola.dao.ManagerPropuestas;
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
 * Panel de control del administrador (RF-9, CU-011).
 * Devuelve métricas globales y los últimos registros de auditoría.
 */
@WebServlet("/DashboardServlet")
public class DashboardServlet extends HttpServlet {
    private ManagerUsuarios usuariosManager = new ManagerUsuarios();
    private ManagerEventos eventosManager = new ManagerEventos();
    private ActividadManager actividadManager = new ActividadManager();
    private ManagerPropuestas propuestas = new ManagerPropuestas();
    private ManagerInscripciones inscripciones = new ManagerInscripciones();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String rol = SessionContext.currentRol(request);
        if (!"ADMIN".equalsIgnoreCase(rol) && !"PTGAS".equalsIgnoreCase(rol)) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Acceso restringido a Administrador o PTGAS.");
            return;
        }

        int activos = 0, suspendidos = 0, anonimos = 0, pendientes = 0;
        for (Usuario u : usuariosManager.findAll()) {
            String estado = u.getEstadoCuenta();
            if (estado == null || "ACTIVA".equalsIgnoreCase(estado)) {
                activos++;
            } else if ("SUSPENDIDA".equalsIgnoreCase(estado)) {
                suspendidos++;
            } else if ("ANONIMIZADA".equalsIgnoreCase(estado)) {
                anonimos++;
            } else if ("PENDIENTE_ACTIVACION".equalsIgnoreCase(estado)) {
                pendientes++;
            }
        }

        JSONObject metricas = new JSONObject();
        metricas.put("usuariosTotales", usuariosManager.findAll().size());
        metricas.put("usuariosActivos", activos);
        metricas.put("usuariosSuspendidos", suspendidos);
        metricas.put("usuariosAnonimizados", anonimos);
        metricas.put("usuariosPendientesActivacion", pendientes);
        metricas.put("eventosTotales", eventosManager.total());
        metricas.put("eventosPublicados", eventosManager.totalPublicados());
        metricas.put("actividadesTotales", actividadManager.total());
        metricas.put("actividadesPublicadas", actividadManager.totalPublicadas());
        metricas.put("propuestasPendientes", propuestas.countPendientes());
        metricas.put("inscripcionesActivas", inscripciones.total());

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("metricas", metricas);
        json.put("auditoriaReciente", ServletJsonUtil.auditoriaArray(auditoria.findRecent(50)));
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }
}
