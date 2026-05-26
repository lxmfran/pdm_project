package es.loyola.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filtro común de autenticación. Protege todos los endpoints excepto
 * los explícitamente públicos (login, recursos estáticos y la home).
 *
 * Implementa los requisitos del PDS:
 * - RN-1: solo usuarios autenticados acceden a la plataforma.
 * - RNF-10: todos los endpoints comprueban autenticación.
 * - RNF-5: respuestas JSON estandarizadas con códigos HTTP coherentes.
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Fuerza UTF-8 en la lectura del cuerpo de la peticion ANTES de que
        // cualquier servlet llame a getReader() o getParameter(). Sin esto, el
        // contenedor interpreta el cuerpo como ISO-8859-1 y corrompe las tildes
        // y caracteres especiales (efecto que ademas se acumula en cada guardado).
        if (httpRequest.getCharacterEncoding() == null) {
            httpRequest.setCharacterEncoding("UTF-8");
        }
        httpResponse.setCharacterEncoding("UTF-8");

        String path = httpRequest.getServletPath();
        if (path == null) {
            path = "";
        }

        if (isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (!SessionContext.isAuthenticated(httpRequest)) {
            writeError(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Sesion no iniciada o expirada. Inicie sesion para continuar.");
            return;
        }

        // Actualiza el último acceso a la sesión
        httpRequest.getSession().setAttribute(SessionContext.ATTR_ULTIMO_ACCESO, new java.util.Date());
        chain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        if (path == null || path.length() == 0 || path.equals("/")) {
            return true;
        }
        if (path.equals("/LoginServlet")) {
            return true;
        }
        if (path.equals("/LogoutServlet")) {
            return true;
        }
        if (path.equals("/ActivarCuentaServlet")) {
            return true;
        }
        // Recursos estáticos
        if (path.endsWith(".jsp") || path.endsWith(".html") || path.endsWith(".css")
                || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".jpg")
                || path.endsWith(".jpeg") || path.endsWith(".gif") || path.endsWith(".svg")
                || path.endsWith(".ico") || path.endsWith(".webp") || path.endsWith(".woff")
                || path.endsWith(".woff2") || path.endsWith(".ttf")) {
            return true;
        }
        if (path.startsWith("/assets/")) {
            return true;
        }
        return false;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"error\":\"" + escape(message) + "\"}");
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 8);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' || c == '"') {
                sb.append('\\');
                sb.append(c);
            } else if (c == '\n') {
                sb.append("\\n");
            } else if (c == '\r') {
                sb.append("\\r");
            } else if (c == '\t') {
                sb.append("\\t");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
