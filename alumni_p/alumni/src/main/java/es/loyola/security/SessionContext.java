package es.loyola.security;

import es.loyola.classes.Usuario;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Acceso uniforme al usuario autenticado y su rol a través de la HttpSession.
 * Centraliza los atributos para evitar errores tipográficos en cada servlet.
 */
public final class SessionContext {
    public static final String ATTR_USUARIO = "usuario";
    public static final String ATTR_ROL = "rol";
    public static final String ATTR_USUARIO_OBJ = "usuarioObj";
    public static final String ATTR_ULTIMO_ACCESO = "ultimoAcceso";

    private SessionContext() {
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(ATTR_USUARIO) != null;
    }

    public static String currentUsuario(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_USUARIO);
        return value == null ? null : value.toString();
    }

    public static String currentRol(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_ROL);
        return value == null ? null : value.toString();
    }

    public static Usuario currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(ATTR_USUARIO_OBJ);
        return value instanceof Usuario ? (Usuario) value : null;
    }

    public static void login(HttpServletRequest request, Usuario usuario, String rol) {
        HttpSession session = request.getSession(true);
        session.setAttribute(ATTR_USUARIO, usuario.getUsuario());
        session.setAttribute(ATTR_ROL, rol);
        session.setAttribute(ATTR_USUARIO_OBJ, usuario);
        session.setAttribute(ATTR_ULTIMO_ACCESO, new java.util.Date());
        // Política de seguridad: timeout de 30 minutos
        session.setMaxInactiveInterval(30 * 60);
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static boolean hasRole(HttpServletRequest request, String... roles) {
        String current = currentRol(request);
        if (current == null) {
            return false;
        }
        for (int i = 0; i < roles.length; i++) {
            if (current.equalsIgnoreCase(roles[i])) {
                return true;
            }
        }
        return false;
    }
}
