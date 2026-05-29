package es.loyola.servlets;

import es.loyola.classes.Actividad;
import es.loyola.classes.ActividadImpl;
import es.loyola.classes.Alumni;
import es.loyola.classes.Evento;
import es.loyola.classes.EventoImpl;
import es.loyola.classes.Notificacion;
import es.loyola.classes.Organizador;
import es.loyola.classes.OrganizadorImpl;
import es.loyola.classes.PropuestaEvento;
import es.loyola.dao.ActividadManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerEventos;
import es.loyola.dao.ManagerNotificaciones;
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
 * Gestion de propuestas de eventos/actividades (RF-4, RF-7, CU-007, CU-010).
 *
 * - GET: lista propuestas. ADMIN/PTGAS ven todo; otros sólo las suyas.
 *        Filtros: estado (PENDIENTE/APROBADA/RECHAZADA/PUBLICADA), solicitante.
 * - PUT: aprobar o rechazar (motivo). Sólo PTGAS/ADMIN.
 *        Al aprobar y publicar, se crea el Evento/Actividad real.
 */
@WebServlet("/PropuestaServlet")
public class PropuestaServlet extends HttpServlet {
    private ManagerPropuestas propuestas = new ManagerPropuestas();
    private ManagerEventos eventos = new ManagerEventos();
    private ActividadManager actividades = new ActividadManager();
    private ManagerAuditoria auditoria = new ManagerAuditoria();
    private ManagerNotificaciones notif = new ManagerNotificaciones();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);
            String estado = request.getParameter("estado");

            List<PropuestaEvento> resultados;
            if ("ADMIN".equalsIgnoreCase(rol) || "PTGAS".equalsIgnoreCase(rol)) {
                String filtroSolicitante = request.getParameter("solicitante");
                if (filtroSolicitante != null && filtroSolicitante.trim().length() > 0) {
                    resultados = propuestas.findBySolicitante(filtroSolicitante);
                } else if (estado != null && estado.trim().length() > 0) {
                    resultados = propuestas.findByEstado(estado);
                } else {
                    resultados = propuestas.findAll();
                }
                if (estado != null && estado.trim().length() > 0) {
                    List<PropuestaEvento> filtradas = new ArrayList<PropuestaEvento>();
                    for (PropuestaEvento p : resultados) {
                        if (estado.equalsIgnoreCase(p.getEstado())) {
                            filtradas.add(p);
                        }
                    }
                    resultados = filtradas;
                }
            } else {
                resultados = propuestas.findBySolicitante(solicitante);
            }

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("total", resultados.size());
            json.put("propuestas", ServletJsonUtil.propuestaArray(resultados));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            JSONObject body = ServletJsonUtil.readJson(request);
            String solicitante = SessionContext.currentUsuario(request);
            String rol = SessionContext.currentRol(request);

            if (!"PTGAS".equalsIgnoreCase(rol) && !"ADMIN".equalsIgnoreCase(rol)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo PTGAS o Administrador pueden evaluar propuestas.");
                return;
            }

            Integer id = ServletJsonUtil.intValue(request, body, "id");
            String decision = body.optString("decision", null); // APROBAR | RECHAZAR | PUBLICAR
            String motivo = body.optString("motivo", "");
            if (id == null || isBlank(decision)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar id y decision (APROBAR|RECHAZAR|PUBLICAR).");
                return;
            }

            PropuestaEvento propuesta = propuestas.findById(id);
            if (propuesta == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Propuesta no encontrada.");
                return;
            }

            JSONObject json = new JSONObject();
            json.put("success", true);

            if ("RECHAZAR".equalsIgnoreCase(decision)) {
                if (isBlank(motivo)) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "El rechazo requiere indicar un motivo.");
                    return;
                }
                propuesta.setEstado(PropuestaEvento.ESTADO_RECHAZADA);
                propuesta.setMotivoDecision(motivo);
                propuesta.setEvaluador(solicitante);
                propuesta.setFechaDecision(new Date());
                auditoria.registrar(solicitante, rol, "RECHAZAR_PROPUESTA", "PropuestaEvento",
                        String.valueOf(id), "OK", motivo);
                notif.crear(propuesta.getSolicitante(), Notificacion.TIPO_PROPUESTA_RESUELTA,
                        "Propuesta rechazada: " + propuesta.getNombre(),
                        "Tu propuesta \"" + propuesta.getNombre() + "\" ha sido rechazada. "
                                + "Motivo: " + motivo);
            } else if ("APROBAR".equalsIgnoreCase(decision)) {
                propuesta.setEstado(PropuestaEvento.ESTADO_APROBADA);
                propuesta.setMotivoDecision(motivo);
                propuesta.setEvaluador(solicitante);
                propuesta.setFechaDecision(new Date());
                auditoria.registrar(solicitante, rol, "APROBAR_PROPUESTA", "PropuestaEvento",
                        String.valueOf(id), "OK", null);
                notif.crear(propuesta.getSolicitante(), Notificacion.TIPO_PROPUESTA_RESUELTA,
                        "Propuesta aprobada: " + propuesta.getNombre(),
                        "Tu propuesta \"" + propuesta.getNombre() + "\" ha sido aprobada y "
                                + "esta lista para su publicacion.");
            } else if ("PUBLICAR".equalsIgnoreCase(decision)) {
                if (!PropuestaEvento.ESTADO_APROBADA.equalsIgnoreCase(propuesta.getEstado())
                        && !PropuestaEvento.ESTADO_PENDIENTE.equalsIgnoreCase(propuesta.getEstado())) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "Solo propuestas APROBADAS o PENDIENTES pueden publicarse.");
                    return;
                }
                // Validación temporal (CU-005 ex.)
                if (propuesta.getFechaEvento() != null && propuesta.getFechaEvento().before(new Date())) {
                    ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                            "La fecha del evento es anterior a hoy: no se puede publicar.");
                    return;
                }
                Integer publicadoId = publicar(propuesta, solicitante);
                propuesta.setEstado(PropuestaEvento.ESTADO_PUBLICADA);
                propuesta.setEvaluador(solicitante);
                propuesta.setFechaDecision(new Date());
                propuesta.setRecursoPublicadoId(publicadoId);
                auditoria.registrar(solicitante, rol, "PUBLICAR_PROPUESTA", "PropuestaEvento",
                        String.valueOf(id), "OK", "Recurso publicado=" + publicadoId);
                notif.crear(propuesta.getSolicitante(), Notificacion.TIPO_PROPUESTA_RESUELTA,
                        "Tu propuesta esta publicada: " + propuesta.getNombre(),
                        "El recurso \"" + propuesta.getNombre()
                                + "\" ha sido publicado y ya admite inscripciones.");
            } else {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Decision no soportada. Use APROBAR, RECHAZAR o PUBLICAR.");
                return;
            }

            // Persiste la decision en la base de datos (RNF-9)
            propuestas.update(propuesta);

            json.put("propuesta", ServletJsonUtil.propuestaToJson(propuesta));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private Integer publicar(PropuestaEvento p, String solicitante) {
        Organizador organizador = new OrganizadorImpl("Universidad Loyola", "LOYOLA");
        if (PropuestaEvento.TIPO_ACTIVIDAD.equalsIgnoreCase(p.getTipo())) {
            ActividadImpl a = new ActividadImpl(null, p.getLugar() == null ? "" : p.getLugar(),
                    p.getNombre(), null, null,
                    p.getFechaAperturaInscripcion() == null ? new Date() : p.getFechaAperturaInscripcion(),
                    new ArrayList<Alumni>(),
                    p.getFechaLimiteInscripcion() == null ? p.getFechaEvento() : p.getFechaLimiteInscripcion(),
                    p.getCapacidadMaxima() == null ? 0 : p.getCapacidadMaxima(),
                    organizador);
            a.setDescripcion(p.getDescripcion());
            a.setPropietario(solicitante);
            a.setEstado(Actividad.ESTADO_PUBLICADA);
            actividades.save(a);
            return a.getId();
        }
        EventoImpl e = new EventoImpl(null, p.getNombre(),
                p.getDescripcion() == null ? "" : p.getDescripcion(),
                p.getLugar() == null ? "" : p.getLugar(),
                p.getPonente() == null ? "" : p.getPonente(),
                organizador,
                p.getFechaAperturaInscripcion(),
                p.getFechaLimiteInscripcion(),
                p.getFechaEvento(),
                p.getCapacidadMaxima() == null ? 0 : p.getCapacidadMaxima(),
                new ArrayList<Alumni>());
        e.setPropietario(solicitante);
        e.setEstado(Evento.ESTADO_PUBLICADO);
        eventos.save(e);
        return e.getId();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
