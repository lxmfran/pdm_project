package es.loyola.servlets;

import es.loyola.classes.Alumni;
import es.loyola.classes.Evento;
import es.loyola.classes.EventoImpl;
import es.loyola.classes.Organizador;
import es.loyola.classes.OrganizadorImpl;
import es.loyola.classes.PropuestaEvento;
import es.loyola.classes.PropuestaEventoImpl;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerEventos;
import es.loyola.dao.ManagerPropuestas;
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
 * Gestion de Eventos (RF-4, RF-7, RF-8, CU-005, CU-007).
 *
 * - GET: lista eventos (por defecto, sólo PUBLICADOS; ADMIN/PTGAS ven todos).
 *        Soporta consulta por id y paginación.
 * - POST: si el rol es ALUMNI o PDI, registra una PROPUESTA en estado PENDIENTE.
 *         Si es PTGAS o ADMIN, publica directamente el evento.
 * - PUT: modifica el evento. Sólo PTGAS/ADMIN. Valida fechas y propietario.
 * - DELETE: cancela (no elimina físicamente) el evento. Sólo ADMIN/PTGAS.
 */
@WebServlet("/EventoServlet")
public class EventoServlet extends HttpServlet {
    private ManagerEventos eventosManager = new ManagerEventos();
    private ManagerPropuestas propuestasManager = new ManagerPropuestas();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String rol = SessionContext.currentRol(request);
            Integer id = queryInt(request, "id");
            JSONObject json = new JSONObject();
            json.put("success", true);

            if (id == null) {
                String estado = request.getParameter("estado");
                List<Evento> base;
                if ("ADMIN".equalsIgnoreCase(rol) || "PTGAS".equalsIgnoreCase(rol)) {
                    base = eventosManager.findAll();
                } else {
                    base = eventosManager.findVisibles();
                }
                if (estado != null && estado.trim().length() > 0) {
                    List<Evento> filtrados = new ArrayList<Evento>();
                    for (Evento e : base) {
                        if (estado.equalsIgnoreCase(e.getEstado())) {
                            filtrados.add(e);
                        }
                    }
                    base = filtrados;
                }
                int page = parseInt(request.getParameter("page"), 1);
                int size = parseInt(request.getParameter("size"), 20);
                if (page < 1) page = 1;
                if (size < 1) size = 20;
                if (size > 100) size = 100;
                int total = base.size();
                int from = Math.min((page - 1) * size, total);
                int to = Math.min(from + size, total);
                json.put("eventos", ServletJsonUtil.eventoArray(base.subList(from, to)));
                json.put("total", total);
                json.put("page", page);
                json.put("size", size);
            } else {
                Evento evento = eventosManager.findById(id);
                if (evento == null) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Evento no encontrado.");
                    return;
                }
                // Alumni/PDI sólo pueden ver eventos publicados
                if (!"ADMIN".equalsIgnoreCase(rol) && !"PTGAS".equalsIgnoreCase(rol)
                        && !Evento.ESTADO_PUBLICADO.equalsIgnoreCase(evento.getEstado())) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Evento no encontrado.");
                    return;
                }
                json.put("evento", ServletJsonUtil.eventoToJson(evento));
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

            String nombre = first(body, "nombre", "nombreEvento");
            if (isBlank(nombre)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar el nombre del evento.");
                return;
            }
            String descripcion = body.optString("descripcion", body.optString("descripcionEvento", ""));
            String lugar = body.optString("lugar", body.optString("lugarEvento", ""));
            if (isBlank(descripcion) || isBlank(lugar)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar descripcion y lugar del evento.");
                return;
            }

            // Alumni/PDI: registran una propuesta (RF-4, RN-5, CU-007)
            if ("ALUMNI".equalsIgnoreCase(rol) || "PDI".equalsIgnoreCase(rol)) {
                PropuestaEventoImpl propuesta = (PropuestaEventoImpl)
                        ManagerPropuestas.nuevaPendiente(PropuestaEvento.TIPO_EVENTO, solicitante, rol,
                                nombre, descripcion, lugar);
                propuesta.setPonente(body.optString("ponente", ""));
                propuesta.setCapacidadMaxima(parseInteger(body, "capacidadMaxima", 0));
                propuesta.setFechaEvento(ServletJsonUtil.dateValue(body, "fechaEvento"));
                propuesta.setFechaAperturaInscripcion(ServletJsonUtil.dateValue(body, "fechaApertura"));
                propuesta.setFechaLimiteInscripcion(ServletJsonUtil.dateValue(body, "fechaLimite"));
                propuestasManager.save(propuesta);
                auditoria.registrar(solicitante, rol, "PROPONER_EVENTO", "PropuestaEvento",
                        String.valueOf(propuesta.getId()), "OK", nombre);

                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("propuesta", ServletJsonUtil.propuestaToJson(propuesta));
                json.put("mensaje", "Tu propuesta de evento se envio correctamente y queda pendiente de revision.");
                ServletJsonUtil.write(response, HttpServletResponse.SC_CREATED, json);
                return;
            }

            if (!"PTGAS".equalsIgnoreCase(rol) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo PTGAS o administradores pueden publicar eventos directamente.");
                return;
            }

            Evento evento = buildEvento(null, body, nombre);
            evento.setPropietario(solicitante);
            // Validación temporal CU-005 ex.: fecha del evento debe ser futura
            if (evento.getFechaEvento() != null && evento.getFechaEvento().before(new Date())) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "La fecha del evento no puede ser anterior a hoy.");
                return;
            }
            eventosManager.save(evento);
            auditoria.registrar(solicitante, rol, "PUBLICAR_EVENTO", "Evento",
                    String.valueOf(evento.getId()), "OK", evento.getNombreEvento());

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("evento", ServletJsonUtil.eventoToJson(evento));
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
                        "Solo PTGAS o administradores pueden modificar eventos.");
                return;
            }

            Integer id = ServletJsonUtil.intValue(request, body, "id");
            if (id == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el id del evento.");
                return;
            }
            Evento actual = eventosManager.findById(id);
            if (actual == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Evento no encontrado.");
                return;
            }
            // PTGAS sólo modifica eventos propios; ADMIN cualquiera
            if ("PTGAS".equalsIgnoreCase(rol) && actual.getPropietario() != null
                    && !actual.getPropietario().equalsIgnoreCase(solicitante)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo el propietario del evento o un administrador pueden modificarlo.");
                return;
            }

            Evento actualizado = buildEvento(id, body, first(body, "nombre", "nombreEvento"));
            if (isBlank(actualizado.getNombreEvento())) {
                actualizado.setNombreEvento(actual.getNombreEvento());
            }
            actualizado.setListaAlumni(actual.getListaAlumni());
            actualizado.setEstado(actual.getEstado());
            actualizado.setPropietario(actual.getPropietario());
            eventosManager.update(actualizado);
            auditoria.registrar(solicitante, rol, "MODIFICAR_EVENTO", "Evento",
                    String.valueOf(id), "OK", actualizado.getNombreEvento());

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("evento", ServletJsonUtil.eventoToJson(actualizado));
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
            String accion = request.getParameter("accion"); // "cancelar" | "eliminar"
            JSONObject body = new JSONObject();
            if (id == null) {
                body = ServletJsonUtil.readJson(request);
                id = ServletJsonUtil.intValue(request, body, "id");
                if (accion == null) {
                    accion = body.optString("accion", null);
                }
            }
            if (id == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, "Debe informar el id del evento.");
                return;
            }
            Evento evento = eventosManager.findById(id);
            if (evento == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Evento no encontrado.");
                return;
            }

            // Eliminar físicamente sólo ADMIN; CANCELAR puede PTGAS propio o ADMIN
            if ("eliminar".equalsIgnoreCase(accion)) {
                if (!"ADMIN".equalsIgnoreCase(rol)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Solo los administradores pueden eliminar eventos.");
                    return;
                }
                eventosManager.deleteById(id);
                auditoria.registrar(solicitante, rol, "ELIMINAR_EVENTO", "Evento", String.valueOf(id), "OK",
                        evento.getNombreEvento());
            } else {
                if (!"ADMIN".equalsIgnoreCase(rol) && !"PTGAS".equalsIgnoreCase(rol)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Solo PTGAS o administradores pueden cancelar eventos.");
                    return;
                }
                if ("PTGAS".equalsIgnoreCase(rol) && evento.getPropietario() != null
                        && !evento.getPropietario().equalsIgnoreCase(solicitante)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                            "Solo el propietario del evento o un administrador pueden cancelarlo.");
                    return;
                }
                evento.cancelarEvento();
                eventosManager.update(evento);
                auditoria.registrar(solicitante, rol, "CANCELAR_EVENTO", "Evento", String.valueOf(id), "OK",
                        evento.getNombreEvento());
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

    private Evento buildEvento(Integer id, JSONObject body, String nombre) throws Exception {
        String organizadorNombre = body.optString("organizadorNombre", "Universidad Loyola");
        String organizadorId = body.optString("organizadorId", "LOYOLA");
        if (body.has("organizador")) {
            JSONObject org = body.getJSONObject("organizador");
            organizadorNombre = org.optString("nombre", organizadorNombre);
            organizadorId = org.optString("identificador", organizadorId);
        }
        Organizador organizador = new OrganizadorImpl(organizadorNombre, organizadorId);
        EventoImpl evento = new EventoImpl(id, nombre,
                body.optString("descripcion", body.optString("descripcionEvento", "")),
                body.optString("lugar", body.optString("lugarEvento", "")),
                body.optString("ponente", ""),
                organizador,
                ServletJsonUtil.dateValue(body, "fechaApertura"),
                ServletJsonUtil.dateValue(body, "fechaLimite"),
                ServletJsonUtil.dateValue(body, "fechaEvento"),
                parseInteger(body, "capacidadMaxima", 0),
                new ArrayList<Alumni>());
        evento.setEstado(Evento.ESTADO_PUBLICADO);
        return evento;
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

    private String first(JSONObject body, String first, String second) {
        String value = body.optString(first, null);
        return value == null ? body.optString(second, null) : value;
    }

    private Integer queryInt(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.trim().length() == 0) {
            return null;
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
