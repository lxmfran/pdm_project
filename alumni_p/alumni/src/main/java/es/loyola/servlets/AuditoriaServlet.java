package es.loyola.servlets;

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
 * Endpoint de consulta del registro de auditoria (RI-8, RN-11).
 * Solo ADMIN.
 */
@WebServlet("/AuditoriaServlet")
public class AuditoriaServlet extends HttpServlet {
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Acceso restringido a administradores.");
            return;
        }
        int limite = parseInt(request.getParameter("limite"), 200);
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("registros", ServletJsonUtil.auditoriaArray(auditoria.findRecent(limite)));
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
