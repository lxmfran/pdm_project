package es.loyola.servlets;

import es.loyola.classes.Usuario;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerUsuarios;
import es.loyola.security.PasswordUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Activacion de cuenta de usuario (CU-009, RF-2).
 *
 * Acepta usuario + token + nuevaContrasena. Al ser una maqueta, el token es
 * simbolico (se valida que coincida con un hash sencillo del usuario), pero
 * la API queda lista para integrar con un servicio de email/token real.
 */
@WebServlet("/ActivarCuentaServlet")
public class ActivarCuentaServlet extends HttpServlet {
    private ManagerUsuarios usuariosManager = new ManagerUsuarios();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String usuario = ServletJsonUtil.value(request, body, "usuario");
            String token = ServletJsonUtil.value(request, body, "token");
            String nuevaContrasena = ServletJsonUtil.value(request, body, "nuevaContrasena");

            if (isBlank(usuario) || isBlank(token) || isBlank(nuevaContrasena)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar usuario, token y nuevaContrasena.");
                return;
            }

            Usuario u = usuariosManager.findByUsuario(usuario);
            if (u == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado.");
                return;
            }
            if (!"PENDIENTE_ACTIVACION".equalsIgnoreCase(u.getEstadoCuenta())) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_CONFLICT,
                        "La cuenta no esta pendiente de activacion.");
                return;
            }
            // Validacion simbolica del token (maqueta)
            String tokenEsperado = "ACT-" + Math.abs((usuario + "loyola-alumni").hashCode());
            if (!tokenEsperado.equals(token)) {
                auditoria.registrar(usuario, "-", "ACTIVAR_CUENTA", "Usuario", usuario, "FALLIDO",
                        "Token invalido o expirado");
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Token invalido o expirado.");
                return;
            }
            if (!PasswordUtil.isStrong(nuevaContrasena)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "La contrasena no cumple la politica de seguridad (minimo 8 caracteres, con letra y digito).");
                return;
            }
            u.getCredenciales().cambiarContrasena(nuevaContrasena);
            u.setEstadoCuenta("ACTIVA");
            usuariosManager.update(u);
            auditoria.registrar(usuario, "-", "ACTIVAR_CUENTA", "Usuario", usuario, "OK", null);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("usuario", usuario);
            json.put("estadoCuenta", "ACTIVA");
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
