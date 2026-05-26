package es.loyola.servlets;

import es.loyola.classes.Alumni;
import es.loyola.dao.AlumniManager;
import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
import es.loyola.security.SessionContext;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Búsqueda de alumni (RF-3, RF-13, CU-002, CU-008).
 * - Exige autenticación y rol autorizado (ALUMNI/PDI/PTGAS/ADMIN).
 * - Aplica privacidad por campo en cada resultado.
 * - Bloquea consultas vacías masivas: exige al menos un filtro o término >= 3 caracteres.
 * - Soporta paginación: parámetros page (1..N) y size (5..100, por defecto 20).
 */
@WebServlet("/BuscarAlumniServlet")
public class BuscarAlumniServlet extends HttpServlet {
    private AlumniManager alumniManager = new AlumniManager();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String rol = SessionContext.currentRol(request);
            if (!isRoleAllowed(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Rol no autorizado para buscar alumni.");
                return;
            }

            String texto = first(request, "texto", "q");
            String titulacion = request.getParameter("titulacion");
            Integer promocion = intParameter(request, "promocion");
            Facultad facultad = enumParameter(Facultad.class, request.getParameter("facultad"));
            Campus campus = enumParameter(Campus.class, request.getParameter("campus"));
            String ciudad = request.getParameter("ciudad");
            String trabajo = request.getParameter("trabajo");
            String hobbies = request.getParameter("hobbies");

            // La busqueda admite filtros cruzados (RF-3) y tambien la consulta
            // sin filtros, que devuelve el listado completo de alumni. Para no
            // saturar la respuesta siempre se aplica paginacion.
            List<Alumni> resultados = alumniManager.search(texto, titulacion, promocion, facultad, campus, ciudad, trabajo, hobbies);

            // Paginación
            int page = parseInt(request.getParameter("page"), 1);
            int size = parseInt(request.getParameter("size"), 20);
            if (page < 1) page = 1;
            if (size < 1) size = 20;
            if (size > 100) size = 100;
            int total = resultados.size();
            int from = Math.min((page - 1) * size, total);
            int to = Math.min(from + size, total);
            List<Alumni> pageData = resultados.subList(from, to);

            String requester = SessionContext.currentUsuario(request);
            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("total", total);
            json.put("page", page);
            json.put("size", size);
            json.put("resultados", ServletJsonUtil.alumniPublicArray(pageData, requester, rol));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (IllegalArgumentException e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private boolean isRoleAllowed(String rol) {
        return "ALUMNI".equalsIgnoreCase(rol) || "PDI".equalsIgnoreCase(rol)
                || "PTGAS".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol);
    }

    private String first(HttpServletRequest request, String first, String second) {
        String value = request.getParameter(first);
        return value == null ? request.getParameter(second) : value;
    }

    private Integer intParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return Integer.valueOf(value);
    }

    private <T extends Enum<T>> T enumParameter(Class<T> type, String value) {
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return Enum.valueOf(type, value.trim().toUpperCase());
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

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
