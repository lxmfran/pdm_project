package es.loyola.servlets;

import es.loyola.classes.Actividad;
import es.loyola.classes.Administrador;
import es.loyola.classes.Alumni;
import es.loyola.classes.Evento;
import es.loyola.classes.Inscripcion;
import es.loyola.classes.Organizador;
import es.loyola.classes.Pdi;
import es.loyola.classes.PropuestaEvento;
import es.loyola.classes.Ptgas;
import es.loyola.classes.RegistroAuditoria;
import es.loyola.classes.Trabajo;
import es.loyola.classes.Usuario;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ServletJsonUtil {
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private ServletJsonUtil() {
    }

    public static void write(HttpServletResponse response, int status, JSONObject json) throws IOException {
        prepare(response, status);
        response.getWriter().write(json.toString());
    }

    public static void write(HttpServletResponse response, int status, JSONArray json) throws IOException {
        prepare(response, status);
        response.getWriter().write(json.toString());
    }

    public static void writeError(HttpServletResponse response, int status, String message) throws IOException {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("error", message);
        write(response, status, json);
    }

    public static JSONObject readJson(HttpServletRequest request) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        if (builder.length() == 0) {
            return new JSONObject();
        }
        return new JSONObject(builder.toString());
    }

    public static String value(HttpServletRequest request, JSONObject json, String name) {
        String parameter = request.getParameter(name);
        if (parameter != null) {
            return parameter;
        }
        return json.optString(name, null);
    }

    public static Integer intValue(HttpServletRequest request, JSONObject json, String name) {
        String value = value(request, json, name);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return Integer.valueOf(value);
    }

    public static Date dateValue(JSONObject json, String name) throws ParseException {
        String value = json.optString(name, null);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        return new SimpleDateFormat(DATE_PATTERN).parse(value);
    }

    /**
     * Serializa el usuario autenticado con todos sus datos (sólo para él mismo y administradores).
     */
    public static JSONObject usuarioToJson(Usuario usuario, String rol) {
        return usuarioToJson(usuario, rol, true);
    }

    /**
     * Serializa un usuario aplicando privacidad: si includeSensitive es false,
     * se omiten DNI, email, teléfono y datos de privacidad por defecto del Alumni.
     */
    public static JSONObject usuarioToJson(Usuario usuario, String rol, boolean includeSensitive) {
        JSONObject json = new JSONObject();
        json.put("id", usuario.getId());
        json.put("usuario", usuario.getUsuario());
        json.put("rol", rol);
        json.put("nombre", usuario.getNombre());
        json.put("apellidos", usuario.getApellidos());
        json.put("estadoCuenta", usuario.getEstadoCuenta());
        json.put("ultimoAcceso", format(usuario.getFechaUltimoAcceso()));
        if (includeSensitive) {
            json.put("email", usuario.getEmail());
            json.put("telefono", usuario.getTelefono());
            json.put("dni", usuario.getCredenciales() == null ? JSONObject.NULL : usuario.getCredenciales().getDni());
        }
        if (usuario instanceof Alumni) {
            json.put("perfilAlumni", alumniToJson((Alumni) usuario, includeSensitive));
        } else if (usuario instanceof Pdi) {
            Pdi pdi = (Pdi) usuario;
            json.put("titulacion", pdi.getTitulacion());
            json.put("campus", pdi.getCampus() == null ? JSONObject.NULL : pdi.getCampus().name());
            json.put("facultad", pdi.getFacultad() == null ? JSONObject.NULL : pdi.getFacultad().name());
            json.put("areaTrabajo", pdi.getAreaTrabajo());
        } else if (usuario instanceof Ptgas) {
            Ptgas ptgas = (Ptgas) usuario;
            json.put("titulacion", ptgas.getTitulacion());
            json.put("campus", ptgas.getCampus() == null ? JSONObject.NULL : ptgas.getCampus().name());
            json.put("facultad", ptgas.getFacultad() == null ? JSONObject.NULL : ptgas.getFacultad().name());
            json.put("areaActual", ptgas.getAreaActual());
            json.put("enProyectoInvestigacion", ptgas.getEnProyectoInvestigacion());
            json.put("departamento", ptgas.getDepartamento());
            json.put("proyectos", ptgas.getProyectos() == null ? new JSONArray() : new JSONArray(ptgas.getProyectos()));
        } else if (usuario instanceof Administrador) {
            Administrador admin = (Administrador) usuario;
            json.put("permisos", admin.getPermisos() == null ? new JSONArray() : new JSONArray(admin.getPermisos()));
        }
        return json;
    }

    /**
     * Serializa Alumni con datos completos (perfil propio o vista de administrador).
     */
    public static JSONObject alumniToJson(Alumni alumni) {
        return alumniToJson(alumni, true);
    }

    public static JSONObject alumniToJson(Alumni alumni, boolean includeSensitive) {
        JSONObject json = new JSONObject();
        json.put("id", alumni.getId());
        json.put("usuario", alumni.getUsuario());
        json.put("nombre", alumni.getNombre());
        json.put("apellidos", alumni.getApellidos());
        json.put("titulacion", alumni.getTitulacion());
        json.put("promocion", alumni.getPromocion());
        json.put("anioGraduacion", alumni.getAnioGraduacion());
        json.put("facultad", alumni.getFacultad() == null ? JSONObject.NULL : alumni.getFacultad().name());
        json.put("campus", alumni.getCampus() == null ? JSONObject.NULL : alumni.getCampus().name());
        json.put("fotoPerfil", alumni.getFotoPerfil() == null ? "" : alumni.getFotoPerfil());
        json.put("mostrarContacto", alumni.getMostrarContacto());
        if (includeSensitive) {
            json.put("email", alumni.getEmail());
            json.put("telefono", alumni.getTelefono());
            json.put("ciudad", alumni.getCiudad());
            json.put("ciudadResidencia", alumni.getCiudadResidencia());
            json.put("hobbies", alumni.getHobbies());
            json.put("trabajoActual", alumni.getTrabajoActual());
            json.put("trabajo", trabajoToJson(alumni.getTrabajo()));
            json.put("mostrarEmail", alumni.getMostrarEmail());
            json.put("mostrarTelefono", alumni.getMostrarTelefono());
            json.put("mostrarCiudad", alumni.getMostrarCiudad());
            json.put("mostrarTrabajo", alumni.getMostrarTrabajo());
            json.put("mostrarHobbies", alumni.getMostrarHobbies());
        }
        return json;
    }

    /**
     * Vista pública de Alumni respetando preferencias de visibilidad (RF-13, RN-2, RN-3, RN-4).
     * Si el solicitante es el propietario o un administrador, ve todo.
     */
    public static JSONObject alumniPublicView(Alumni alumni, String requesterUsuario, String requesterRol) {
        boolean propietario = alumni.getUsuario() != null
                && alumni.getUsuario().equalsIgnoreCase(requesterUsuario);
        boolean admin = "ADMIN".equalsIgnoreCase(requesterRol);
        if (propietario || admin) {
            return alumniToJson(alumni, true);
        }

        JSONObject json = new JSONObject();
        json.put("id", alumni.getId());
        json.put("usuario", alumni.getUsuario());
        json.put("nombre", alumni.getNombre());
        json.put("apellidos", alumni.getApellidos());
        json.put("titulacion", alumni.getTitulacion());
        json.put("promocion", alumni.getPromocion());
        json.put("anioGraduacion", alumni.getAnioGraduacion());
        json.put("facultad", alumni.getFacultad() == null ? JSONObject.NULL : alumni.getFacultad().name());
        json.put("campus", alumni.getCampus() == null ? JSONObject.NULL : alumni.getCampus().name());
        json.put("fotoPerfil", alumni.getFotoPerfil() == null ? "" : alumni.getFotoPerfil());
        json.put("mostrarContacto", alumni.getMostrarContacto());

        // Aplica preferencias campo a campo
        boolean contacto = alumni.getMostrarContacto() != null && alumni.getMostrarContacto();
        if (contacto && alumni.getMostrarEmail()) {
            json.put("email", alumni.getEmail());
        }
        if (contacto && alumni.getMostrarTelefono()) {
            json.put("telefono", alumni.getTelefono());
        }
        if (alumni.getMostrarCiudad()) {
            json.put("ciudad", alumni.getCiudad());
            json.put("ciudadResidencia", alumni.getCiudadResidencia());
        }
        if (alumni.getMostrarHobbies()) {
            json.put("hobbies", alumni.getHobbies());
        }
        if (alumni.getMostrarTrabajo()) {
            json.put("trabajoActual", alumni.getTrabajoActual());
            json.put("trabajo", trabajoToJson(alumni.getTrabajo()));
        }
        return json;
    }

    public static JSONObject eventoToJson(Evento evento) {
        JSONObject json = new JSONObject();
        json.put("id", evento.getId());
        json.put("nombre", evento.getNombreEvento());
        json.put("descripcion", evento.getDescripcionEvento());
        json.put("lugar", evento.getLugarEvento());
        json.put("ponente", evento.getPonente());
        json.put("estado", evento.getEstado());
        json.put("propietario", evento.getPropietario() == null ? JSONObject.NULL : evento.getPropietario());
        json.put("fechaApertura", format(evento.getFechaAperturaInscripcion()));
        json.put("fechaLimite", format(evento.getFechaLimite()));
        json.put("fechaEvento", format(evento.getFechaEvento()));
        json.put("capacidadMaxima", evento.getCapacidadMaxima());
        json.put("organizador", organizadorToJson(evento.getOrganizador()));
        JSONArray inscritos = new JSONArray();
        if (evento.getListaAlumni() != null) {
            for (Alumni alumni : evento.getListaAlumni()) {
                inscritos.put(alumni.getUsuario());
            }
        }
        json.put("inscritos", inscritos);
        return json;
    }

    public static JSONObject actividadToJson(Actividad actividad) {
        JSONObject json = new JSONObject();
        json.put("id", actividad.getId());
        json.put("nombre", actividad.getNombreActividad());
        json.put("descripcion", actividad.getDescripcion());
        json.put("lugar", actividad.getLugarActividad());
        json.put("estado", actividad.getEstado());
        json.put("propietario", actividad.getPropietario() == null ? JSONObject.NULL : actividad.getPropietario());
        json.put("nivelParticipacion", actividad.getNivelParticipacion() == null ? JSONObject.NULL : actividad.getNivelParticipacion().name());
        json.put("fechaApertura", format(actividad.getFechaApertura()));
        json.put("fechaLimite", format(actividad.getFechaLimiteActividad()));
        json.put("fecha", format(actividad.getFecha()));
        json.put("maxPlazas", actividad.getMaxPlazas());
        json.put("organizador", organizadorToJson(actividad.getOrganizadorActividad()));
        json.put("hobbie", actividad.getHobbiePracticado() == null ? JSONObject.NULL : actividad.getHobbiePracticado().getNombreHobbie());
        JSONArray inscritos = new JSONArray();
        if (actividad.getAlumniInscritos() != null) {
            for (Alumni alumni : actividad.getAlumniInscritos()) {
                inscritos.put(alumni.getUsuario());
            }
        }
        json.put("inscritos", inscritos);
        return json;
    }

    public static JSONObject propuestaToJson(PropuestaEvento p) {
        JSONObject json = new JSONObject();
        json.put("id", p.getId());
        json.put("tipo", p.getTipo());
        json.put("solicitante", p.getSolicitante());
        json.put("rolSolicitante", p.getRolSolicitante());
        json.put("nombre", p.getNombre());
        json.put("descripcion", p.getDescripcion());
        json.put("lugar", p.getLugar());
        json.put("ponente", p.getPonente() == null ? "" : p.getPonente());
        json.put("capacidadMaxima", p.getCapacidadMaxima());
        json.put("fechaEvento", format(p.getFechaEvento()));
        json.put("fechaApertura", format(p.getFechaAperturaInscripcion()));
        json.put("fechaLimite", format(p.getFechaLimiteInscripcion()));
        json.put("estado", p.getEstado());
        json.put("motivoDecision", p.getMotivoDecision() == null ? "" : p.getMotivoDecision());
        json.put("evaluador", p.getEvaluador() == null ? "" : p.getEvaluador());
        json.put("fechaEnvio", formatDateTime(p.getFechaEnvio()));
        json.put("fechaDecision", formatDateTime(p.getFechaDecision()));
        json.put("recursoPublicadoId", p.getRecursoPublicadoId() == null ? JSONObject.NULL : p.getRecursoPublicadoId());
        return json;
    }

    public static JSONObject inscripcionToJson(Inscripcion i) {
        JSONObject json = new JSONObject();
        json.put("id", i.getId());
        json.put("tipo", i.getTipoRecurso());
        json.put("recursoId", i.getRecursoId());
        json.put("usuario", i.getUsuario());
        json.put("rol", i.getRolUsuario());
        json.put("ticket", i.getTicket());
        json.put("estado", i.getEstado());
        json.put("fechaInscripcion", formatDateTime(i.getFechaInscripcion()));
        return json;
    }

    public static JSONObject auditoriaToJson(RegistroAuditoria r) {
        JSONObject json = new JSONObject();
        json.put("id", r.getId());
        json.put("fecha", formatDateTime(r.getFecha()));
        json.put("actor", r.getActor());
        json.put("rol", r.getRol());
        json.put("accion", r.getAccion());
        json.put("entidad", r.getEntidad());
        json.put("entidadId", r.getEntidadId());
        json.put("resultado", r.getResultado());
        json.put("detalle", r.getDetalle() == null ? "" : r.getDetalle());
        return json;
    }

    public static JSONArray alumniArray(Iterable<Alumni> alumnis) {
        JSONArray array = new JSONArray();
        for (Alumni alumni : alumnis) {
            array.put(alumniToJson(alumni));
        }
        return array;
    }

    public static JSONArray alumniPublicArray(Iterable<Alumni> alumnis, String requesterUsuario, String requesterRol) {
        JSONArray array = new JSONArray();
        for (Alumni alumni : alumnis) {
            array.put(alumniPublicView(alumni, requesterUsuario, requesterRol));
        }
        return array;
    }

    public static JSONArray eventoArray(Iterable<Evento> eventos) {
        JSONArray array = new JSONArray();
        for (Evento evento : eventos) {
            array.put(eventoToJson(evento));
        }
        return array;
    }

    public static JSONArray actividadArray(Iterable<Actividad> actividades) {
        JSONArray array = new JSONArray();
        for (Actividad a : actividades) {
            array.put(actividadToJson(a));
        }
        return array;
    }

    public static JSONArray propuestaArray(Iterable<PropuestaEvento> propuestas) {
        JSONArray array = new JSONArray();
        for (PropuestaEvento p : propuestas) {
            array.put(propuestaToJson(p));
        }
        return array;
    }

    public static JSONArray inscripcionArray(Iterable<Inscripcion> inscripciones) {
        JSONArray array = new JSONArray();
        for (Inscripcion i : inscripciones) {
            array.put(inscripcionToJson(i));
        }
        return array;
    }

    public static JSONArray auditoriaArray(Iterable<RegistroAuditoria> registros) {
        JSONArray array = new JSONArray();
        for (RegistroAuditoria r : registros) {
            array.put(auditoriaToJson(r));
        }
        return array;
    }

    private static JSONObject trabajoToJson(Trabajo trabajo) {
        if (trabajo == null) {
            return new JSONObject();
        }
        JSONObject json = new JSONObject();
        json.put("descripcion", trabajo.getDescripcion());
        json.put("posicion", trabajo.getPosicion());
        json.put("lugar", trabajo.getLugar());
        json.put("ciudad", trabajo.getCiudadTrabajo());
        json.put("fechaInicio", format(trabajo.getFechaInicio()));
        json.put("fechaFin", format(trabajo.getFechaFin()));
        json.put("fechaFinal", format(trabajo.getFechaFinal()));
        return json;
    }

    private static JSONObject organizadorToJson(Organizador organizador) {
        if (organizador == null) {
            return new JSONObject();
        }
        JSONObject json = new JSONObject();
        json.put("nombre", organizador.getNombreOrganizador());
        json.put("identificador", organizador.getIdentificador());
        return json;
    }

    public static String format(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATE_PATTERN).format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(DATETIME_PATTERN).format(date);
    }

    private static void prepare(HttpServletResponse response, int status) {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }
}
