package es.loyola.servlets;

import es.loyola.classes.Usuario;
import es.loyola.dao.ManagerAuditoria;
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
 * Inicio de sesion (CU-001, RF-1).
 * Valida credenciales, comprueba estado de cuenta y crea HttpSession.
 * Registra el intento en auditoria.
 */
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private ManagerUsuarios usuarioManager = new ManagerUsuarios();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String usuario = ServletJsonUtil.value(request, body, "usuario");
            String contrasenia = ServletJsonUtil.value(request, body, "contrasenia");
            if (contrasenia == null) {
                contrasenia = ServletJsonUtil.value(request, body, "password");
            }

            if (isBlank(usuario) || isBlank(contrasenia)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar usuario y contrasena.");
                return;
            }

            Usuario candidato = usuarioManager.findByUsuario(usuario);
            if (candidato != null && candidato.getEstadoCuenta() != null
                    && !"ACTIVA".equalsIgnoreCase(candidato.getEstadoCuenta())) {
                auditoria.registrar(usuario, "-", "LOGIN", "Usuario", usuario, "BLOQUEADO",
                        "Cuenta en estado " + candidato.getEstadoCuenta());
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "La cuenta no esta activa. Contacte con el administrador.");
                return;
            }

            Usuario validado = usuarioManager.validateCredentials(usuario, contrasenia);
            if (validado == null) {
                auditoria.registrar(usuario, "-", "LOGIN", "Usuario", usuario, "FALLIDO",
                        "Credenciales incorrectas");
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Usuario o contrasena incorrectos.");
                return;
            }

            String rol = usuarioManager.getRol(validado);
            SessionContext.login(request, validado, rol);
            auditoria.registrar(validado.getUsuario(), rol, "LOGIN", "Usuario", validado.getUsuario(), "OK", null);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("rol", rol);
            json.put("usuario", ServletJsonUtil.usuarioToJson(validado, rol));
            json.put("sessionId", request.getSession().getId());
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
