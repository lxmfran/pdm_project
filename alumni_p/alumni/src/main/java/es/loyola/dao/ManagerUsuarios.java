package es.loyola.dao;

import es.loyola.classes.Administrador;
import es.loyola.classes.AdministradorImpl;
import es.loyola.classes.Alumni;
import es.loyola.classes.AlumniImpl;
import es.loyola.classes.CredencialesImpl;
import es.loyola.classes.Pdi;
import es.loyola.classes.PdiImpl;
import es.loyola.classes.Ptgas;
import es.loyola.classes.PtgasImpl;
import es.loyola.classes.Trabajo;
import es.loyola.classes.TrabajoImpl;
import es.loyola.classes.Usuario;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
import es.loyola.db.Mappers;
import es.loyola.enums.Campus;
import es.loyola.security.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * DAO de usuarios sobre la base de datos relacional 'desarrollo_alumni' (RNF-9).
 *
 * Implementa la herencia JOINED del esquema: la tabla 'usuario' contiene los
 * datos comunes y las tablas 'alumni', 'pdi', 'ptgas' y 'administrador' los
 * especificos de cada rol. La API publica de la clase no cambia respecto a la
 * version en memoria, de modo que los servlets no requieren modificacion.
 */
public class ManagerUsuarios {

    /** Consulta base que une usuario, credenciales y las cuatro tablas hijas. */
    private static final String SELECT_BASE =
            "SELECT u.id_usuario, u.nombre, u.apellidos, u.email, u.rol, u.estado, "
          + "       c.usuario_login, c.hash_contrasena, c.ultimo_acceso, "
          + "       a.titulacion AS a_titulacion, a.facultad AS a_facultad, a.ano_graduacion, "
          + "       a.campus AS a_campus, a.ciudad_residencia, a.telefono AS a_telefono, a.trabajo_actual, "
          + "       p.area_trabajo, p.campus AS p_campus, p.en_proyecto_investigacion, "
          + "       pt.departamento, ad.nivel "
          + "FROM usuario u "
          + "JOIN credenciales c ON c.id_usuario = u.id_usuario "
          + "LEFT JOIN alumni a ON a.id_usuario = u.id_usuario "
          + "LEFT JOIN pdi p ON p.id_usuario = u.id_usuario "
          + "LEFT JOIN ptgas pt ON pt.id_usuario = u.id_usuario "
          + "LEFT JOIN administrador ad ON ad.id_usuario = u.id_usuario ";

    public List<Usuario> findAll() {
        List<Usuario> out = new ArrayList<Usuario>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "ORDER BY u.id_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(construirUsuario(con, rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar usuarios.", e);
        }
        return out;
    }

    public List<Usuario> findActivos() {
        List<Usuario> out = new ArrayList<Usuario>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE u.estado <> 'ANONIMIZADO' ORDER BY u.id_usuario");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                out.add(construirUsuario(con, rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar usuarios activos.", e);
        }
        return out;
    }

    public Usuario findByUsuario(String usuario) {
        if (usuario == null) {
            return null;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE c.usuario_login = ?")) {
            ps.setString(1, usuario.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? construirUsuario(con, rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar el usuario '" + usuario + "'.", e);
        }
    }

    public Usuario findByEmail(String email) {
        if (email == null) {
            return null;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE u.email = ?")) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? construirUsuario(con, rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar por email.", e);
        }
    }

    /**
     * Valida credenciales. Solo las cuentas ACTIVAS pueden autenticarse (RF-1).
     * Al validar correctamente actualiza la fecha de ultimo acceso (RF-12).
     */
    public Usuario validateCredentials(String usuario, String contrasenia) {
        Usuario encontrado = findByUsuario(usuario);
        if (encontrado == null || encontrado.getCredenciales() == null) {
            return null;
        }
        if (!"ACTIVA".equalsIgnoreCase(encontrado.getEstadoCuenta())) {
            return null;
        }
        if (!PasswordUtil.verify(contrasenia, encontrado.getCredenciales().getContrasenia())) {
            return null;
        }
        actualizarUltimoAcceso(encontrado.getId());
        encontrado.setFechaUltimoAcceso(new Date());
        return encontrado;
    }

    private void actualizarUltimoAcceso(Integer idUsuario) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE credenciales SET ultimo_acceso = NOW(), intentos_fallidos = 0 "
                             + "WHERE id_usuario = ?")) {
            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar el ultimo acceso.", e);
        }
    }

    /**
     * Crea un usuario nuevo: inserta en 'usuario', 'credenciales' y la tabla hija.
     */
    public Usuario save(Usuario usuario) {
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            int idUsuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuario (nombre, apellidos, email, rol, estado) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nvl(usuario.getNombre()));
                ps.setString(2, nvl(usuario.getApellidos()));
                ps.setString(3, nvl(usuario.getEmail()));
                ps.setString(4, getRol(usuario));
                ps.setString(5, Mappers.estadoHaciaBd(usuario.getEstadoCuenta()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    idUsuario = keys.getInt(1);
                }
            }
            usuario.setId(idUsuario);

            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO credenciales (id_usuario, usuario_login, hash_contrasena) VALUES (?,?,?)")) {
                ps.setInt(1, idUsuario);
                ps.setString(2, usuario.getUsuario());
                ps.setString(3, usuario.getCredenciales() == null ? "" : usuario.getCredenciales().getContrasenia());
                ps.executeUpdate();
            }

            insertarFilaHija(con, idUsuario, usuario);
            con.commit();
            return usuario;
        } catch (SQLException e) {
            rollback(con);
            throw new DataAccessException("Error al crear el usuario.", e);
        } finally {
            cerrar(con);
        }
    }

    /**
     * Actualiza un usuario existente (datos base, credenciales y tabla hija).
     */
    public Usuario update(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return null;
        }
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);
            int id = usuario.getId();

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE usuario SET nombre=?, apellidos=?, email=?, rol=?, estado=? WHERE id_usuario=?")) {
                ps.setString(1, nvl(usuario.getNombre()));
                ps.setString(2, nvl(usuario.getApellidos()));
                ps.setString(3, nvl(usuario.getEmail()));
                ps.setString(4, getRol(usuario));
                ps.setString(5, Mappers.estadoHaciaBd(usuario.getEstadoCuenta()));
                ps.setInt(6, id);
                ps.executeUpdate();
            }

            if (usuario.getCredenciales() != null && usuario.getCredenciales().getContrasenia() != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE credenciales SET usuario_login=?, hash_contrasena=? WHERE id_usuario=?")) {
                    ps.setString(1, usuario.getUsuario());
                    ps.setString(2, usuario.getCredenciales().getContrasenia());
                    ps.setInt(3, id);
                    ps.executeUpdate();
                }
            }

            // Fila hija: si el rol no cambia, se ACTUALIZA (nunca se borra, para
            // no disparar el ON DELETE CASCADE que eliminaria trabajo, hobbies y
            // preferencias del alumni). Solo en un cambio de rol se reescribe.
            String rolActual = obtenerRol(con, id);
            String rolNuevo = getRol(usuario);
            if (rolActual != null && rolActual.equalsIgnoreCase(rolNuevo)) {
                actualizarFilaHija(con, id, usuario);
            } else {
                borrarFilasHijas(con, id);
                insertarFilaHija(con, id, usuario);
            }

            con.commit();
            return usuario;
        } catch (SQLException e) {
            rollback(con);
            throw new DataAccessException("Error al actualizar el usuario.", e);
        } finally {
            cerrar(con);
        }
    }

    /** Baja logica: suspende la cuenta sin borrar datos (RF-6). */
    public boolean suspender(String usuario) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE usuario u JOIN credenciales c ON c.id_usuario = u.id_usuario "
                             + "SET u.estado = 'SUSPENDIDO' WHERE c.usuario_login = ?")) {
            ps.setString(1, usuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al suspender el usuario.", e);
        }
    }

    /**
     * Anonimizacion RGPD (RN-10): borra los datos personales pero conserva la
     * fila para mantener la trazabilidad de inscripciones y auditoria.
     */
    public boolean anonimizar(String usuario) {
        Usuario u = findByUsuario(usuario);
        if (u == null) {
            return false;
        }
        int id = u.getId();
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE usuario SET nombre='Usuario', apellidos='Anonimizado', "
                            + "email=?, estado='ANONIMIZADO' WHERE id_usuario=?")) {
                ps.setString(1, "anonimo_" + id + "@anon.local");
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE credenciales SET hash_contrasena=?, token_activacion=NULL WHERE id_usuario=?")) {
                ps.setString(1, PasswordUtil.hash("anon-" + System.currentTimeMillis()));
                ps.setInt(2, id);
                ps.executeUpdate();
            }
            if (u instanceof Alumni) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE alumni SET ciudad_residencia=NULL, telefono=NULL, trabajo_actual=NULL "
                                + "WHERE id_usuario=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                ejecutar(con, "DELETE FROM hobbie WHERE id_alumni=?", id);
                ejecutar(con, "DELETE FROM trabajo WHERE id_alumni=?", id);
                ejecutar(con, "DELETE FROM preferencia_privacidad WHERE id_alumni=?", id);
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            rollback(con);
            throw new DataAccessException("Error al anonimizar el usuario.", e);
        } finally {
            cerrar(con);
        }
    }

    public String getRol(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return "ADMIN";
        }
        if (usuario instanceof Alumni) {
            return "ALUMNI";
        }
        if (usuario instanceof Pdi) {
            return "PDI";
        }
        if (usuario instanceof Ptgas) {
            return "PTGAS";
        }
        return "ALUMNI";
    }

    /**
     * Devuelve los usuarios cuyo ultimo acceso es anterior al umbral indicado (RF-12).
     */
    public List<Usuario> findInactivos(int diasInactividad) {
        List<Usuario> out = new ArrayList<Usuario>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE u.estado <> 'ANONIMIZADO' "
                             + "AND (c.ultimo_acceso IS NULL "
                             + "     OR c.ultimo_acceso < DATE_SUB(NOW(), INTERVAL ? DAY)) "
                             + "ORDER BY c.ultimo_acceso ASC")) {
            ps.setInt(1, diasInactividad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(construirUsuario(con, rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al consultar usuarios inactivos.", e);
        }
        return out;
    }

    /** Devuelve todos los Alumni del sistema (no anonimizados). */
    public static List<Alumni> getAlumniReferences() {
        List<Alumni> out = new ArrayList<Alumni>();
        ManagerUsuarios m = new ManagerUsuarios();
        for (Usuario u : m.findAll()) {
            if (u instanceof Alumni && !"ANONIMIZADA".equalsIgnoreCase(u.getEstadoCuenta())) {
                out.add((Alumni) u);
            }
        }
        return out;
    }

    // --- Construccion de objetos de dominio ----------------------------------

    private Usuario construirUsuario(Connection con, ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nombre = rs.getString("nombre");
        String apellidos = rs.getString("apellidos");
        String email = rs.getString("email");
        String rol = rs.getString("rol");
        String estadoBd = rs.getString("estado");
        String login = rs.getString("usuario_login");
        String hash = rs.getString("hash_contrasena");
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");

        CredencialesImpl credenciales = new CredencialesImpl(login, hash, "");

        Usuario usuario;
        if ("ADMIN".equalsIgnoreCase(rol)) {
            String nivel = rs.getString("nivel");
            usuario = new AdministradorImpl(nombre, apellidos, email, "", credenciales,
                    new ArrayList<String>(Arrays.asList(nivel == null ? "GENERAL" : nivel)), new Date());
        } else if ("PDI".equalsIgnoreCase(rol)) {
            Campus campus = parseCampus(rs.getString("p_campus"));
            usuario = new PdiImpl(nombre, apellidos, email, "", credenciales, "",
                    campus, es.loyola.enums.Facultad.INGENIERIA, nvl(rs.getString("area_trabajo")));
        } else if ("PTGAS".equalsIgnoreCase(rol)) {
            usuario = new PtgasImpl(nombre, apellidos, email, "", credenciales, "",
                    Campus.SEVILLA, es.loyola.enums.Facultad.HUMANISMO, "", Boolean.FALSE,
                    nvl(rs.getString("departamento")), new ArrayList<String>());
        } else {
            Campus campus = parseCampus(rs.getString("a_campus"));
            AlumniImpl alumni = new AlumniImpl(nombre, apellidos, email,
                    nvl(rs.getString("a_telefono")), credenciales,
                    nvl(rs.getString("a_titulacion")),
                    rs.getInt("ano_graduacion"),
                    Mappers.facultadDesdeBd(rs.getString("a_facultad")),
                    campus, null, nvl(rs.getString("ciudad_residencia")), "", "");
            cargarDetallesAlumni(con, id, alumni, rs.getString("trabajo_actual"));
            usuario = alumni;
        }

        usuario.setId(id);
        usuario.setEstadoCuenta(Mappers.estadoDesdeBd(estadoBd));
        usuario.setFechaUltimoAcceso(ultimoAcceso == null ? null : new Date(ultimoAcceso.getTime()));
        return usuario;
    }

    /** Carga trabajo actual, hobbies y preferencias de privacidad de un Alumni. */
    private void cargarDetallesAlumni(Connection con, int idAlumni, AlumniImpl alumni,
                                      String trabajoActualTexto) throws SQLException {
        // Trabajo: el actual (fecha_fin NULL) o el mas reciente
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT descripcion, empresa, ciudad, fecha_inicio, fecha_fin FROM trabajo "
                        + "WHERE id_alumni=? ORDER BY (fecha_fin IS NULL) DESC, fecha_inicio DESC LIMIT 1")) {
            ps.setInt(1, idAlumni);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Trabajo trabajo = new TrabajoImpl(rs.getString("descripcion"),
                            rs.getString("empresa"), rs.getString("ciudad"),
                            rs.getDate("fecha_inicio"), rs.getDate("fecha_fin"));
                    alumni.setTrabajo(trabajo);
                }
            }
        }
        if (alumni.getTrabajo() == null && trabajoActualTexto != null) {
            alumni.setTrabajoActual(trabajoActualTexto);
        }

        // Hobbies: nombres concatenados
        StringBuilder hobbies = new StringBuilder();
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT nombre FROM hobbie WHERE id_alumni=?")) {
            ps.setInt(1, idAlumni);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (hobbies.length() > 0) {
                        hobbies.append(", ");
                    }
                    hobbies.append(rs.getString("nombre"));
                }
            }
        }
        alumni.setHobbies(hobbies.toString());

        // Preferencias de privacidad campo a campo (RI-9)
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT campo, es_visible FROM preferencia_privacidad WHERE id_alumni=?")) {
            ps.setInt(1, idAlumni);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String campo = rs.getString("campo");
                    boolean visible = rs.getBoolean("es_visible");
                    if (campo == null) {
                        continue;
                    }
                    switch (campo.toLowerCase()) {
                        case "email":
                            alumni.setMostrarEmail(visible);
                            break;
                        case "telefono":
                            alumni.setMostrarTelefono(visible);
                            break;
                        case "ciudad":
                        case "ciudad_residencia":
                            alumni.setMostrarCiudad(visible);
                            break;
                        case "trabajo":
                        case "trabajo_actual":
                            alumni.setMostrarTrabajo(visible);
                            break;
                        case "hobbies":
                            alumni.setMostrarHobbies(visible);
                            break;
                        case "contacto":
                            alumni.setMostrarContacto(visible);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    // --- Insercion / borrado de filas hijas ----------------------------------

    private void insertarFilaHija(Connection con, int id, Usuario usuario) throws SQLException {
        if (usuario instanceof Alumni) {
            Alumni a = (Alumni) usuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO alumni (id_usuario, titulacion, facultad, ano_graduacion, campus, "
                            + "ciudad_residencia, telefono, trabajo_actual) VALUES (?,?,?,?,?,?,?,?)")) {
                ps.setInt(1, id);
                ps.setString(2, nvl(a.getTitulacion()));
                ps.setString(3, a.getFacultad() == null ? "INGENIERIA" : a.getFacultad().name());
                ps.setInt(4, a.getAnioGraduacion() == null ? 2024 : a.getAnioGraduacion());
                ps.setString(5, a.getCampus() == null ? "SEVILLA" : a.getCampus().name());
                ps.setString(6, a.getCiudad());
                ps.setString(7, a.getTelefono());
                ps.setString(8, a.getTrabajoActual());
                ps.executeUpdate();
            }
        } else if (usuario instanceof Pdi) {
            Pdi p = (Pdi) usuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO pdi (id_usuario, area_trabajo, campus, en_proyecto_investigacion) "
                            + "VALUES (?,?,?,?)")) {
                ps.setInt(1, id);
                ps.setString(2, nvl(p.getAreaTrabajo()).isEmpty() ? "General" : p.getAreaTrabajo());
                ps.setString(3, p.getCampus() == null ? "SEVILLA" : p.getCampus().name());
                ps.setBoolean(4, false);
                ps.executeUpdate();
            }
        } else if (usuario instanceof Ptgas) {
            Ptgas pt = (Ptgas) usuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO ptgas (id_usuario, departamento) VALUES (?,?)")) {
                ps.setInt(1, id);
                ps.setString(2, nvl(pt.getDepartamento()).isEmpty() ? "General" : pt.getDepartamento());
                ps.executeUpdate();
            }
        } else if (usuario instanceof Administrador) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO administrador (id_usuario, nivel) VALUES (?,?)")) {
                ps.setInt(1, id);
                ps.setString(2, "GENERAL");
                ps.executeUpdate();
            }
        }
    }

    private void borrarFilasHijas(Connection con, int id) throws SQLException {
        ejecutar(con, "DELETE FROM alumni WHERE id_usuario=?", id);
        ejecutar(con, "DELETE FROM pdi WHERE id_usuario=?", id);
        ejecutar(con, "DELETE FROM ptgas WHERE id_usuario=?", id);
        ejecutar(con, "DELETE FROM administrador WHERE id_usuario=?", id);
    }

    /** Actualiza la fila hija sin borrarla (preserva trabajo/hobbies/privacidad). */
    private void actualizarFilaHija(Connection con, int id, Usuario usuario) throws SQLException {
        if (usuario instanceof Alumni) {
            Alumni a = (Alumni) usuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE alumni SET titulacion=?, facultad=?, ano_graduacion=?, campus=?, "
                            + "ciudad_residencia=?, telefono=?, trabajo_actual=? WHERE id_usuario=?")) {
                ps.setString(1, nvl(a.getTitulacion()));
                ps.setString(2, a.getFacultad() == null ? "INGENIERIA" : a.getFacultad().name());
                ps.setInt(3, a.getAnioGraduacion() == null ? 2024 : a.getAnioGraduacion());
                ps.setString(4, a.getCampus() == null ? "SEVILLA" : a.getCampus().name());
                ps.setString(5, a.getCiudad());
                ps.setString(6, a.getTelefono());
                ps.setString(7, a.getTrabajoActual());
                ps.setInt(8, id);
                ps.executeUpdate();
            }
        } else if (usuario instanceof Pdi) {
            Pdi p = (Pdi) usuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE pdi SET area_trabajo=?, campus=? WHERE id_usuario=?")) {
                ps.setString(1, nvl(p.getAreaTrabajo()).isEmpty() ? "General" : p.getAreaTrabajo());
                ps.setString(2, p.getCampus() == null ? "SEVILLA" : p.getCampus().name());
                ps.setInt(3, id);
                ps.executeUpdate();
            }
        } else if (usuario instanceof Ptgas) {
            Ptgas pt = (Ptgas) usuario;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE ptgas SET departamento=? WHERE id_usuario=?")) {
                ps.setString(1, nvl(pt.getDepartamento()).isEmpty() ? "General" : pt.getDepartamento());
                ps.setInt(2, id);
                ps.executeUpdate();
            }
        }
        // El administrador no tiene campos editables relevantes (nivel fijo).
    }

    private String obtenerRol(Connection con, int id) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT rol FROM usuario WHERE id_usuario=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    // --- Utilidades ----------------------------------------------------------

    private void ejecutar(Connection con, String sql, int idParam) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idParam);
            ps.executeUpdate();
        }
    }

    private Campus parseCampus(String value) {
        if (value == null || value.trim().length() == 0) {
            return Campus.SEVILLA;
        }
        try {
            return Campus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Campus.SEVILLA;
        }
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private void rollback(Connection con) {
        if (con != null) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
        }
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
