package es.loyola.servlets;

import es.loyola.classes.AdministradorImpl;
import es.loyola.classes.Alumni;
import es.loyola.classes.AlumniImpl;
import es.loyola.classes.Credenciales;
import es.loyola.classes.CredencialesImpl;
import es.loyola.classes.PdiImpl;
import es.loyola.classes.PtgasImpl;
import es.loyola.classes.Trabajo;
import es.loyola.classes.TrabajoImpl;
import es.loyola.classes.Usuario;
import es.loyola.dao.AlumniManager;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerInscripciones;
import es.loyola.dao.ManagerUsuarios;
import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
import es.loyola.security.PasswordUtil;
import es.loyola.security.SessionContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Administracion de usuarios (RF-6, CU-006, RN-10, RN-11).
 * - Exige sesion activa y rol ADMIN para todas las operaciones.
 * - Crea/modifica/suspende usuarios con politica de contrasena segura.
 * - DELETE realiza baja logica con anonimizacion (RN-10).
 * - Todas las operaciones quedan registradas en auditoria.
 */
@WebServlet("/UsuarioAdminServlet")
public class UsuarioAdminServlet extends HttpServlet {
    private ManagerUsuarios usuariosManager = new ManagerUsuarios();
    private AlumniManager alumniManager = new AlumniManager();
    private ManagerInscripciones inscripciones = new ManagerInscripciones();
    private ManagerAuditoria auditoria = new ManagerAuditoria();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "Acceso restringido a administradores.");
            return;
        }
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("usuarios", usuariosToJson(true));
        ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String adminUsuario = SessionContext.currentUsuario(request);
            if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
                auditoria.registrar(adminUsuario, SessionContext.currentRol(request), "CREAR_USUARIO",
                        "Usuario", "-", "DENEGADO", "Sesion sin rol ADMIN");
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo un administrador puede crear usuarios.");
                return;
            }
            JSONObject body = ServletJsonUtil.readJson(request);
            String usuario = body.optString("usuario", "").trim();
            if (isBlank(usuario) || isBlank(body.optString("rol", ""))) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar usuario y rol.");
                return;
            }
            if (usuariosManager.findByUsuario(usuario) != null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_CONFLICT,
                        "Ya existe una cuenta con ese usuario.");
                return;
            }

            String contrasenia = body.optString("contrasenia", null);
            boolean generada = false;
            if (isBlank(contrasenia)) {
                contrasenia = generarTemporal(usuario);
                generada = true;
            } else if (!PasswordUtil.isStrong(contrasenia)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "La contrasena no cumple la politica de seguridad (minimo 8 caracteres, con letra y digito).");
                return;
            }

            Usuario creado = buildUsuario(body, null, contrasenia);
            creado.setEstadoCuenta("PENDIENTE_ACTIVACION");
            usuariosManager.save(creado);
            alumniManager.refreshFromUsuarios();
            auditoria.registrar(adminUsuario, "ADMIN", "CREAR_USUARIO", "Usuario", usuario, "OK",
                    "rol=" + body.optString("rol"));

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("usuario", ServletJsonUtil.usuarioToJson(creado, usuariosManager.getRol(creado), true));
            json.put("contrasenaTemporalGenerada", generada);
            ServletJsonUtil.write(response, HttpServletResponse.SC_CREATED, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String adminUsuario = SessionContext.currentUsuario(request);
            if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo un administrador puede modificar usuarios.");
                return;
            }
            JSONObject body = ServletJsonUtil.readJson(request);
            String usuario = body.optString("usuario", "").trim();
            Usuario existente = usuariosManager.findByUsuario(usuario);
            if (existente == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado.");
                return;
            }

            String accion = body.optString("accion", null);
            if (!isBlank(accion)) {
                if ("suspender".equalsIgnoreCase(accion)) {
                    existente.setEstadoCuenta("SUSPENDIDA");
                } else if ("reactivar".equalsIgnoreCase(accion)) {
                    existente.setEstadoCuenta("ACTIVA");
                } else if ("anonimizar".equalsIgnoreCase(accion)) {
                    usuariosManager.anonimizar(usuario);
                    inscripciones.anonimizar(usuario, "anon-" + existente.getId());
                    alumniManager.refreshFromUsuarios();
                    auditoria.registrar(adminUsuario, "ADMIN", "ANONIMIZAR_USUARIO", "Usuario",
                            usuario, "OK", null);
                    JSONObject json = new JSONObject();
                    json.put("success", true);
                    json.put("usuario", ServletJsonUtil.usuarioToJson(existente, usuariosManager.getRol(existente), true));
                    ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
                    return;
                }
                usuariosManager.update(existente);
                auditoria.registrar(adminUsuario, "ADMIN", "ESTADO_USUARIO_" + accion.toUpperCase(),
                        "Usuario", usuario, "OK", "estado=" + existente.getEstadoCuenta());
                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("usuario", ServletJsonUtil.usuarioToJson(existente, usuariosManager.getRol(existente), true));
                ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
                return;
            }

            String contrasenia = body.optString("contrasenia", null);
            if (!isBlank(contrasenia) && !PasswordUtil.isStrong(contrasenia)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "La contrasena no cumple la politica de seguridad.");
                return;
            }

            Usuario actualizado = buildUsuario(body, existente, contrasenia);
            usuariosManager.update(actualizado);
            alumniManager.refreshFromUsuarios();
            auditoria.registrar(adminUsuario, "ADMIN", "MODIFICAR_USUARIO", "Usuario", usuario, "OK", null);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("usuario", ServletJsonUtil.usuarioToJson(actualizado, usuariosManager.getRol(actualizado), true));
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String adminUsuario = SessionContext.currentUsuario(request);
            if (!"ADMIN".equalsIgnoreCase(SessionContext.currentRol(request))) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Solo un administrador puede dar de baja usuarios.");
                return;
            }
            JSONObject body = new JSONObject();
            try {
                body = ServletJsonUtil.readJson(request);
            } catch (Exception ignored) {
            }
            String usuario = ServletJsonUtil.value(request, body, "usuario");
            if (isBlank(usuario)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Debe informar el usuario a dar de baja.");
                return;
            }
            if (usuario.equalsIgnoreCase(adminUsuario)) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Un administrador no puede darse de baja a si mismo desde este endpoint.");
                return;
            }
            Usuario objetivo = usuariosManager.findByUsuario(usuario);
            if (objetivo == null) {
                ServletJsonUtil.writeError(response, HttpServletResponse.SC_NOT_FOUND, "Usuario no encontrado.");
                return;
            }

            String reemplazo = "anon-" + objetivo.getId();
            usuariosManager.anonimizar(usuario);
            inscripciones.anonimizar(usuario, reemplazo);
            alumniManager.refreshFromUsuarios();
            auditoria.registrar(adminUsuario, "ADMIN", "ANONIMIZAR_USUARIO", "Usuario", usuario, "OK", null);

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("usuario", usuario);
            json.put("estado", "ANONIMIZADA");
            ServletJsonUtil.write(response, HttpServletResponse.SC_OK, json);
        } catch (Exception e) {
            ServletJsonUtil.writeError(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private Usuario buildUsuario(JSONObject body, Usuario existente, String contraseniaNueva) {
        String rol = body.optString("rol", existente == null ? "ALUMNI" : usuariosManager.getRol(existente)).toUpperCase();
        String usuario = body.optString("usuario", existente == null ? "" : existente.getUsuario()).trim();

        String contrasenia;
        if (!isBlank(contraseniaNueva)) {
            contrasenia = contraseniaNueva;
        } else if (existente != null && existente.getCredenciales() != null) {
            contrasenia = existente.getCredenciales().getContrasenia();
        } else {
            contrasenia = generarTemporal(usuario);
        }

        String dni = body.optString("dni", existente == null || existente.getCredenciales() == null
                ? "" : existente.getCredenciales().getDni());
        Credenciales credenciales = new CredencialesImpl(usuario, contrasenia, dni);
        String nombre = body.optString("nombre", existente == null ? "" : existente.getNombre());
        String apellidos = body.optString("apellidos", existente == null ? "" : existente.getApellidos());
        String email = body.optString("email", existente == null ? usuario + "@uloyola.es" : existente.getEmail());
        String telefono = body.optString("telefono", existente == null ? "" : existente.getTelefono());
        String titulacion = body.optString("titulacion", existente instanceof Alumni
                ? ((Alumni) existente).getTitulacion() : "");
        Campus campus = parseCampus(body.optString("campus", "SEVILLA"));
        Facultad facultad = parseFacultad(body.optString("facultad", "INGENIERIA"));

        Usuario nuevo;
        if ("ADMIN".equals(rol)) {
            nuevo = new AdministradorImpl(nombre, apellidos, email, telefono, credenciales,
                    csv(body.optString("permisos", "GESTION_USUARIOS,EVENTOS")), new Date());
        } else if ("PDI".equals(rol)) {
            nuevo = new PdiImpl(nombre, apellidos, email, telefono, credenciales, titulacion, campus, facultad,
                    body.optString("areaTrabajo", body.optString("trabajoDescripcion", "")));
        } else if ("PTGAS".equals(rol)) {
            nuevo = new PtgasImpl(nombre, apellidos, email, telefono, credenciales, titulacion, campus,
                    facultad, body.optString("areaActual", body.optString("trabajoDescripcion", "")),
                    Boolean.valueOf(body.optString("enProyectoInvestigacion", "false")),
                    body.optString("departamento", ""), csv(body.optString("proyectos", "alumni")));
        } else {
            Trabajo trabajo = new TrabajoImpl(body.optString("trabajoDescripcion", ""),
                    body.optString("trabajoLugar", ""), body.optString("ciudad", ""), new Date(), null);
            String fotoPerfil = body.optString("fotoPerfil", "");
            if (isBlank(fotoPerfil) && existente instanceof Alumni) {
                fotoPerfil = ((Alumni) existente).getFotoPerfil();
            }
            nuevo = new AlumniImpl(nombre, apellidos, email, telefono, credenciales,
                    titulacion, parseInteger(body, "promocion", 2024),
                    facultad, campus, trabajo, body.optString("ciudad", ""), body.optString("hobbies", ""),
                    fotoPerfil);
        }
        if (existente != null) {
            nuevo.setId(existente.getId());
            nuevo.setEstadoCuenta(existente.getEstadoCuenta());
            nuevo.setFechaUltimoAcceso(existente.getFechaUltimoAcceso());
        }
        return nuevo;
    }

    private String generarTemporal(String usuario) {
        long t = System.currentTimeMillis();
        return (usuario == null ? "User" : usuario) + "-A1" + Long.toString(t, 36);
    }

    private JSONArray usuariosToJson(boolean includeSensitive) {
        JSONArray array = new JSONArray();
        for (Usuario usuario : usuariosManager.findAll()) {
            array.put(ServletJsonUtil.usuarioToJson(usuario, usuariosManager.getRol(usuario), includeSensitive));
        }
        return array;
    }

    private List<String> csv(String value) {
        if (isBlank(value)) {
            return new ArrayList<String>();
        }
        return Arrays.asList(value.split("\\s*,\\s*"));
    }

    private Integer parseInteger(JSONObject body, String key, Integer defaultValue) {
        if (!body.has(key) || body.isNull(key) || isBlank(body.optString(key, ""))) {
            return defaultValue;
        }
        return Integer.valueOf(body.get(key).toString());
    }

    private Campus parseCampus(String value) {
        return Campus.valueOf((isBlank(value) ? "SEVILLA" : value).toUpperCase());
    }

    private Facultad parseFacultad(String value) {
        return Facultad.valueOf((isBlank(value) ? "INGENIERIA" : value).toUpperCase());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().length() == 0;
    }
}
