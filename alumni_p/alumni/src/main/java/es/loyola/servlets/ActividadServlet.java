package es.loyola.servlets;

import es.loyola.classes.Actividad;
import es.loyola.classes.ActividadImpl;
import es.loyola.classes.Alumni;
import es.loyola.classes.Hobbie;
import es.loyola.classes.HobbieImpl;
import es.loyola.classes.Organizador;
import es.loyola.classes.OrganizadorImpl;
import es.loyola.classes.PropuestaEvento;
import es.loyola.classes.PropuestaEventoImpl;
import es.loyola.dao.ActividadManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerPropuestas;
import es.loyola.enums.ExperienciaHobbies;
import es.loyola.security.SessionContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 * Gestión de Actividades (CU-005, CU-007, RF-4, RF-8).
 *
 * - GET: lista actividades (sólo publicadas para Alumni/PDI; todo para PTGAS/Admin).
 *        Filtra por estado y soporta paginación.
 * - POST: si rol es ALUMNI/PDI, crea PROPUESTA pendiente. Si es PTGAS/ADMIN, publica directamente.
 * - PUT: modifica una actividad. Sólo PTGAS/ADMIN; PTGAS sólo las suyas.
 * - DELETE: cancela (accion=cancelar) o elimina (accion=eliminar, sólo ADMIN).
 */
@WebServlet("/ActividadServlet")
public class ActividadServlet extends HttpServlet {
    private ActividadManager actividadManager = new ActividadManager();
    private ManagerPropuestas propuestas = new ManagerPropuestas();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String rol = SessionContext.currentRol(request);
            Integer id = queryInt(request, "id");
            JSONObject json = new JSONObject();
            json.put("success", true);
            if (id == null) {
                String estado = request.getParameter("estado");
                List<Actividad> base;
                if ("ADMIN".equalsIgnoreCase(rol) || "PTGAS".equalsIgnoreCase(rol)) {
                    base = actividadManager.findAll();
                } else {
                    base = actividadManager.findVisibles();
                }
                if (estado != null && estado.trim().length() > 0) {
                    List<Actividad> filtradas = new ArrayList<Actividad>();
                    for (Actividad a : base) {
                        if (estado.equalsIgnoreCase(a.getEstado())) {
                            filtradas.add(a);
                        }
                    }
                    base = filtradas;
                }
                int page = parseInt(request.getParameter("page"), 1);
                int size = parseInt(request.getParameter("size"), 20);
                if (page < 1) page = 1;
                if (size < 1) size = 20;
                if (size > 100) size = 100;
                int total = base.size();
                int from = Math.min((page - 1) * size, total);
                int to = Math.min(from + size, total);
                json.put("total", total);
                json.put("page", page);
                json.put("size", size);
                json.put("actividades", ServletJsonUtil.actividadArray(base.subList(from, to)));
            } else {
                Actividad actividad = actividadManager.findById(id);
                if (actividad == null) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Actividad no encontrada.");
                    return;
                }
                if (!"ADMIN".equalsIgnoreCase(rol) && !"PTGAS".equalsIgnoreCase(rol)
                        && !Actividad.ESTADO_PUBLICADA.equalsIgnoreCase(actividad.getEstado())) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Actividad no encontrada.");
                    return;
                }
                json.put("actividad", ServletJsonUtil.actividadToJson(actividad));
            }
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (NumberFormatException e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "El id debe ser numerico.");
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);

            String nombre = body.optString("nombre", body.optString("nombreActividad", null));
            String descripcion = body.optString("descripcion", "");
            String lugar = body.optString("lugar", body.optString("lugarActividad", ""));
            if (isBlank(nombre) || isBlank(descripcion) || isBlank(lugar)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar nombre, descripcion y lugar.");
                return;
            }

            if ("ALUMNI".equalsIgnoreCase(rol) || "PDI".equalsIgnoreCase(rol)) {
                PropuestaEventoImpl propuesta = (PropuestaEventoImpl)
                        ManagerPropuestas.nuevaPendiente(PropuestaEvento.TIPO_ACTIVIDAD, solicitante, rol,
                                nombre, descripcion, lugar);
                propuesta.setCapacidadMaxima(parseInteger(body, "maxPlazas", 0));
                propuesta.setFechaAperturaInscripcion(ServletJsonUtil.dateValue(body, "fechaApertura"));
                propuesta.setFechaLimiteInscripcion(ServletJsonUtil.dateValue(body, "fechaLimite"));
                propuesta.setFechaEvento(ServletJsonUtil.dateValue(body, "fecha"));
                propuestas.save(propuesta);
                auditoria.registrar(solicitante, rol, "PROPONER_ACTIVIDAD", "PropuestaEvento",
                        String.valueOf(propuesta.getId()), "OK", nombre);

                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("propuesta", ServletJsonUtil.propuestaToJson(propuesta));
                json.put("mensaje", "Tu propuesta de actividad se envio correctamente y queda pendiente de revision.");
                ServletJsonUtil.write(response, HttpServletResponse.SC_CREATED, json);
                return;
            }

            if (!"PTGAS".equalsIgnoreCase(rol) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo PTGAS o administradores pueden publicar actividades directamente.");
                return;
            }

            ActividadImpl actividad = buildActividad(null, body, nombre, descripcion, lugar);
            actividad.setPropietario(solicitante);
            if (actividad.getFecha() != null && actividad.getFecha().before(new Date())) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "La fecha de la actividad no puede ser anterior a hoy.");
                return;
            }
            actividadManager.save(actividad);
            auditoria.registrar(solicitante, rol, "PUBLICAR_ACTIVIDAD", "Actividad",
                    String.valueOf(actividad.getId()), "OK", actividad.getNombreActividad());

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("actividad", ServletJsonUtil.actividadToJson(actividad));
            ServletJsonUtil.write(response, HttpServletResponse.SC_CREATED, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);
            if (!"PTGAS".equalsIgnoreCase(rol) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo PTGAS o administradores pueden modificar actividades.");
                return;
            }
            Integer id = ServletJsonUtil.intValue(request, body, "id");
            if (id == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el id.");
                return;
            }
            Actividad actual = actividadManager.findById(id);
            if (actual == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Actividad no encontrada.");
                return;
            }
            if ("PTGAS".equalsIgnoreCase(rol) && actual.getPropietario() != null
                    && !actual.getPropietario().equalsIgnoreCase(solicitante)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo el propietario o un administrador puede modificarla.");
                return;
            }

            if (body.has("nombre")) actual.setNombreActividad(body.optString("nombre", actual.getNombreActividad()));
            if (body.has("descripcion")) actual.setDescripcion(body.optString("descripcion", actual.getDescripcion()));
            if (body.has("lugar")) actual.setLugarActividad(body.optString("lugar", actual.getLugarActividad()));
            if (body.has("maxPlazas")) actual.setMaxPlazas(parseInteger(body, "maxPlazas", actual.getMaxPlazas()));
            if (body.has("fechaApertura")) actual.setFechaAperturaInscripcion(ServletJsonUtil.dateValue(body, "fechaApertura"));
            if (body.has("fechaLimite")) actual.setFechaLimiteActividad(ServletJsonUtil.dateValue(body, "fechaLimite"));
            if (body.has("fecha")) actual.setFecha(ServletJsonUtil.dateValue(body, "fecha"));
            actividadManager.update(actual);
            auditoria.registrar(solicitante, rol, "MODIFICAR_ACTIVIDAD", "Actividad",
                    String.valueOf(id), "OK", actual.getNombreActividad());

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("actividad", ServletJsonUtil.actividadToJson(actual));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);
            Integer id = queryInt(request, "id");
            String accion = request.getParameter("accion");
            JSONObject body = new JSONObject();
            if (id == null) {
                body = ServletJsonUtil.readJson(request);
                id = ServletJsonUtil.intValue(request, body, "id");
                if (accion == null) accion = body.optString("accion", null);
            }
            if (id == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el id.");
                return;
            }
            Actividad actividad = actividadManager.findById(id);
            if (actividad == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Actividad no encontrada.");
                return;
            }

            if ("eliminar".equalsIgnoreCase(accion)) {
                if (!"ADMIN".equalsIgnoreCase(rol)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Solo administradores pueden eliminar actividades.");
                    return;
                }
                actividadManager.deleteById(id);
                auditoria.registrar(solicitante, rol, "ELIMINAR_ACTIVIDAD", "Actividad",
                        String.valueOf(id), "OK", actividad.getNombreActividad());
            } else {
                if (!"ADMIN".equalsIgnoreCase(rol) && !"PTGAS".equalsIgnoreCase(rol)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Solo PTGAS o administradores pueden cancelar actividades.");
                    return;
                }
                if ("PTGAS".equalsIgnoreCase(rol) && actividad.getPropietario() != null
                        && !actividad.getPropietario().equalsIgnoreCase(solicitante)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Solo el propietario o un administrador puede cancelarla.");
                    return;
                }
                actividad.cancelar();
                actividadManager.update(actividad);
                auditoria.registrar(solicitante, rol, "CANCELAR_ACTIVIDAD", "Actividad",
                        String.valueOf(id), "OK", actividad.getNombreActividad());
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("id", id);
            json.put("accion", accion == null ? "cancelar" : accion);
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private ActividadImpl buildActividad(Integer id, JSONObject body, String nombre, String descripcion, String lugar) throws Exception {
        Organizador organizador = new OrganizadorImpl(
                body.optString("organizadorNombre", "Universidad Loyola"),
                body.optString("organizadorId", "LOYOLA"));
        ExperienciaHobbies nivel = null;
        String nivelTexto = body.optString("nivelParticipacion", null);
        if (nivelTexto != null && nivelTexto.trim().length() > 0) {
            try {
                nivel = ExperienciaHobbies.valueOf(nivelTexto.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        Hobbie hobbie = null;
        String hobbieTexto = body.optString("hobbie", null);
        if (hobbieTexto != null && hobbieTexto.trim().length() > 0) {
            hobbie = new HobbieImpl(hobbieTexto, 1, nivel);
        }
        Date apertura = ServletJsonUtil.dateValue(body, "fechaApertura");
        Date fecha = ServletJsonUtil.dateValue(body, "fecha");
        if (fecha == null) fecha = ServletJsonUtil.dateValue(body, "fechaLimite");
        Integer plazas = parseInteger(body, "maxPlazas", 0);
        ActividadImpl actividad = new ActividadImpl(id, lugar, nombre, hobbie, nivel, apertura,
                new ArrayList<Alumni>(), fecha, plazas, organizador);
        actividad.setDescripcion(descripcion);
        actividad.setEstado(Actividad.ESTADO_PUBLICADA);
        return actividad;
    }

    private Integer queryInt(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return Integer.valueOf(value);
    }

    private Integer parseInteger(JSONObject body, String key, Integer defaultValue) {
        if (!body.has(key) || body.isNull(key)) {
            return defaultValue;
        }
        String value = body.optString(key, "").trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        return Integer.valueOf(value);
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
