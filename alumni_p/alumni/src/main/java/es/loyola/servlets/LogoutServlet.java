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
 * Cierre de sesión: invalida la HttpSession.
 */
@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usuario = SessionContext.currentUsuario(request);
        String rol = SessionContext.currentRol(request);
        SessionContext.logout(request);
        if (usuario != null) {
            auditoria.registrar(usuario, rol, "LOGOUT", "Usuario", usuario, "OK", null);
        }
        JSONObject json = new JSONObject();
        json.put("success", true);
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
