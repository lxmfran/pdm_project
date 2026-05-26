package es.loyola.servlets;

import es.loyola.classes.Alumni;
import es.loyola.classes.Trabajo;
import es.loyola.classes.TrabajoImpl;
import es.loyola.dao.AlumniManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
import es.loyola.security.SessionContext;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Gestión del perfil Alumni (RF-11, RF-13, CU-003).
 * - GET aplica privacidad: el solicitante sólo ve su propio perfil completo o el público de otros.
 * - POST/PUT exige que el solicitante sea propietario o un administrador.
 * - Soporta actualización de preferencias de visibilidad campo a campo.
 */
@WebServlet("/PerfilServlet")
public class PerfilServlet extends HttpServlet {
    private AlumniManager alumniManager = new AlumniManager();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usuario = request.getParameter("usuario");
        if (isBlank(usuario)) {
            usuario = SessionContext.currentUsuario(request);
        }
        if (isBlank(usuario)) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el usuario.");
            return;
        }

        Alumni alumni = alumniManager.findByUsuario(usuario);
        if (alumni == null) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Alumni no encontrado.");
            return;
        }

        String solicitante = SessionContext.currentUsuario(request);
        String rolSolicitante = SessionContext.currentRol(request);

        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("perfil", ServletJsonUtil.alumniPublicView(alumni, solicitante, rolSolicitante));
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPut(request, response);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String objetivo = ServletJsonUtil.value(request, body, "usuario");
            String solicitante = SessionContext.currentUsuario(request);
            String rolSolicitante = SessionContext.currentRol(request);

            if (isBlank(objetivo)) {
                // Por defecto, edita el propio perfil
                objetivo = solicitante;
            }
            if (isBlank(objetivo)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el usuario.");
                return;
            }

            boolean esPropietario = objetivo.equalsIgnoreCase(solicitante);
            boolean esAdmin = "ADMIN".equalsIgnoreCase(rolSolicitante);
            if (!esPropietario && !esAdmin) {
                auditoria.registrar(solicitante, rolSolicitante, "EDITAR_PERFIL", "Alumni", objetivo,
                        "DENEGADO", "El solicitante no es propietario ni administrador");
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo el propietario del perfil o un administrador pueden modificarlo.");
                return;
            }

            Alumni alumni = alumniManager.findByUsuario(objetivo);
            if (alumni == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Alumni no encontrado.");
                return;
            }

            updateFields(alumni, body);
            alumniManager.update(alumni);
            auditoria.registrar(solicitante, rolSolicitante, "EDITAR_PERFIL", "Alumni", objetivo, "OK", null);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("perfil", ServletJsonUtil.alumniToJson(alumni, true));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (IllegalArgumentException e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private void updateFields(Alumni alumni, JSONObject body) {
        if (body.has("nombre")) {
            alumni.setNombre(body.optString("nombre", alumni.getNombre()));
        }
        if (body.has("apellidos")) {
            alumni.setApellidos(body.optString("apellidos", alumni.getApellidos()));
        }
        if (body.has("email")) {
            alumni.setEmail(body.optString("email", alumni.getEmail()));
        }
        if (body.has("telefono")) {
            alumni.setTelefono(body.get("telefono").toString());
        }
        if (body.has("titulacion")) {
            alumni.setTitulacion(body.optString("titulacion", alumni.getTitulacion()));
        }
        if (body.has("promocion")) {
            alumni.setPromocion(Integer.valueOf(body.get("promocion").toString()));
        }
        if (body.has("facultad")) {
            alumni.setFacultad(Facultad.valueOf(body.getString("facultad").toUpperCase()));
        }
        if (body.has("campus")) {
            alumni.setCampus(Campus.valueOf(body.getString("campus").toUpperCase()));
        }
        if (body.has("ciudad")) {
            alumni.setCiudad(body.optString("ciudad", alumni.getCiudad()));
            alumni.setCiudadResidencia(body.optString("ciudad", alumni.getCiudadResidencia()));
        }
        if (body.has("hobbies")) {
            alumni.setHobbies(body.optString("hobbies", alumni.getHobbies()));
        }
        if (body.has("fotoPerfil")) {
            alumni.setFotoPerfil(body.optString("fotoPerfil", alumni.getFotoPerfil()));
        }
        if (body.has("contrasenia")) {
            // Aplica política de seguridad: hash y validación
            alumni.getCredenciales().cambiarContrasena(body.getString("contrasenia"));
        }
        // Privacidad por campo (RF-11)
        if (body.has("mostrarContacto")) {
            alumni.setMostrarContacto(body.optBoolean("mostrarContacto", alumni.getMostrarContacto()));
        }
        if (body.has("mostrarEmail")) {
            alumni.setMostrarEmail(body.optBoolean("mostrarEmail", alumni.getMostrarEmail()));
        }
        if (body.has("mostrarTelefono")) {
            alumni.setMostrarTelefono(body.optBoolean("mostrarTelefono", alumni.getMostrarTelefono()));
        }
        if (body.has("mostrarCiudad")) {
            alumni.setMostrarCiudad(body.optBoolean("mostrarCiudad", alumni.getMostrarCiudad()));
        }
        if (body.has("mostrarTrabajo")) {
            alumni.setMostrarTrabajo(body.optBoolean("mostrarTrabajo", alumni.getMostrarTrabajo()));
        }
        if (body.has("mostrarHobbies")) {
            alumni.setMostrarHobbies(body.optBoolean("mostrarHobbies", alumni.getMostrarHobbies()));
        }
        if (body.has("trabajo")) {
            JSONObject trabajoJson = body.getJSONObject("trabajo");
            Trabajo actual = alumni.getTrabajo();
            alumni.setTrabajo(new TrabajoImpl(
                    trabajoJson.optString("descripcion", actual == null ? null : actual.getDescripcion()),
                    trabajoJson.optString("lugar", actual == null ? null : actual.getLugar()),
                    trabajoJson.optString("ciudad", actual == null ? null : actual.getCiudadTrabajo()),
                    actual == null ? null : actual.getFechaInicio(),
                    actual == null ? null : actual.getFechaFin()));
            alumni.setTrabajoActual(alumni.getTrabajo().getDescripcion());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
