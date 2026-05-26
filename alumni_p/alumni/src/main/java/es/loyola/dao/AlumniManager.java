package es.loyola.dao;

import es.loyola.classes.Alumni;
import es.loyola.classes.Trabajo;
import es.loyola.classes.Usuario;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
import es.loyola.db.Mappers;
import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de Alumni sobre la base de datos relacional (RNF-9).
 *
 * Reutiliza {@link ManagerUsuarios} para la hidratacion completa de los objetos
 * Alumni (con trabajo, hobbies y preferencias de privacidad) y anade la busqueda
 * avanzada con filtros cruzados (RF-3, CU-008).
 */
public class AlumniManager {

    private final ManagerUsuarios usuarios = new ManagerUsuarios();

    public List<Alumni> findAll() {
        List<Alumni> out = new ArrayList<Alumni>();
        for (Usuario u : usuarios.findAll()) {
            if (u instanceof Alumni) {
                out.add((Alumni) u);
            }
        }
        return out;
    }

    /**
     * En la version con BD no existe cache en memoria: la BD es siempre la
     * fuente de verdad. Se conserva el metodo por compatibilidad de API.
     */
    public void refreshFromUsuarios() {
        // No-op: sin cache en memoria.
    }

    public Alumni findByUsuario(String usuario) {
        Usuario u = usuarios.findByUsuario(usuario);
        return u instanceof Alumni ? (Alumni) u : null;
    }

    /**
     * Busqueda avanzada de Alumni con filtros cruzados (RF-3, CU-002, CU-008).
     */
    public List<Alumni> search(String texto, String titulacion, Integer promocion, Facultad facultad,
                               Campus campus, String ciudad, String trabajo, String hobbies) {
        StringBuilder sql = new StringBuilder(
                "SELECT c.usuario_login FROM usuario u "
                        + "JOIN credenciales c ON c.id_usuario = u.id_usuario "
                        + "JOIN alumni a ON a.id_usuario = u.id_usuario "
                        + "WHERE u.rol = 'ALUMNI' AND u.estado <> 'ANONIMIZADO' ");
        List<Object> params = new ArrayList<Object>();

        if (notBlank(texto)) {
            sql.append("AND (u.nombre LIKE ? OR u.apellidos LIKE ? OR u.email LIKE ? "
                    + "OR a.titulacion LIKE ? OR a.facultad LIKE ? "
                    + "OR a.ciudad_residencia LIKE ? OR a.trabajo_actual LIKE ?) ");
            String like = "%" + texto.trim() + "%";
            for (int i = 0; i < 7; i++) {
                params.add(like);
            }
        }
        if (notBlank(titulacion)) {
            sql.append("AND a.titulacion LIKE ? ");
            params.add("%" + titulacion.trim() + "%");
        }
        if (promocion != null) {
            sql.append("AND a.ano_graduacion = ? ");
            params.add(promocion);
        }
        if (facultad != null) {
            sql.append("AND a.facultad LIKE ? ");
            params.add("%" + Mappers.facultadKeyword(facultad) + "%");
        }
        if (campus != null) {
            sql.append("AND a.campus = ? ");
            params.add(campus.name());
        }
        if (notBlank(ciudad)) {
            sql.append("AND a.ciudad_residencia LIKE ? ");
            params.add("%" + ciudad.trim() + "%");
        }
        if (notBlank(trabajo)) {
            sql.append("AND (a.trabajo_actual LIKE ? "
                    + "OR EXISTS (SELECT 1 FROM trabajo t WHERE t.id_alumni = a.id_usuario "
                    + "AND (t.descripcion LIKE ? OR t.empresa LIKE ?))) ");
            String like = "%" + trabajo.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (notBlank(hobbies)) {
            sql.append("AND EXISTS (SELECT 1 FROM hobbie h WHERE h.id_alumni = a.id_usuario "
                    + "AND h.nombre LIKE ?) ");
            params.add("%" + hobbies.trim() + "%");
        }
        sql.append("ORDER BY u.apellidos, u.nombre");

        List<String> logins = new ArrayList<String>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logins.add(rs.getString("usuario_login"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar alumni.", e);
        }

        List<Alumni> resultados = new ArrayList<Alumni>();
        for (String login : logins) {
            Alumni a = findByUsuario(login);
            if (a != null) {
                resultados.add(a);
            }
        }
        return resultados;
    }

    /**
     * Actualiza el perfil de un Alumni: datos base, tabla alumni, credenciales,
     * hobbies, trabajo actual y preferencias de privacidad campo a campo.
     */
    public Alumni update(Alumni alumni) {
        if (alumni == null || alumni.getId() == null) {
            return null;
        }
        int id = alumni.getId();
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE usuario SET nombre=?, apellidos=?, email=? WHERE id_usuario=?")) {
                ps.setString(1, nvl(alumni.getNombre()));
                ps.setString(2, nvl(alumni.getApellidos()));
                ps.setString(3, nvl(alumni.getEmail()));
                ps.setInt(4, id);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE alumni SET titulacion=?, facultad=?, ano_graduacion=?, campus=?, "
                            + "ciudad_residencia=?, telefono=?, trabajo_actual=? WHERE id_usuario=?")) {
                ps.setString(1, nvl(alumni.getTitulacion()));
                ps.setString(2, alumni.getFacultad() == null ? "INGENIERIA" : alumni.getFacultad().name());
                ps.setInt(3, alumni.getAnioGraduacion() == null ? 2024 : alumni.getAnioGraduacion());
                ps.setString(4, alumni.getCampus() == null ? "SEVILLA" : alumni.getCampus().name());
                ps.setString(5, alumni.getCiudad());
                ps.setString(6, alumni.getTelefono());
                ps.setString(7, alumni.getTrabajoActual());
                ps.setInt(8, id);
                ps.executeUpdate();
            }

            if (alumni.getCredenciales() != null && alumni.getCredenciales().getContrasenia() != null) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE credenciales SET hash_contrasena=? WHERE id_usuario=?")) {
                    ps.setString(1, alumni.getCredenciales().getContrasenia());
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }
            }

            // Hobbies: se reescriben a partir de la cadena separada por comas
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM hobbie WHERE id_alumni=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            if (notBlank(alumni.getHobbies())) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO hobbie (id_alumni, nombre) VALUES (?,?)")) {
                    for (String h : alumni.getHobbies().split("\\s*,\\s*")) {
                        if (h.trim().length() > 0) {
                            ps.setInt(1, id);
                            ps.setString(2, h.trim());
                            ps.addBatch();
                        }
                    }
                    ps.executeBatch();
                }
            }

            // Trabajo actual: se reemplaza el empleo vigente conservando el historico
            Trabajo trabajo = alumni.getTrabajo();
            if (trabajo != null && notBlank(trabajo.getDescripcion())) {
                try (PreparedStatement ps = con.prepareStatement(
                        "DELETE FROM trabajo WHERE id_alumni=? AND fecha_fin IS NULL")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO trabajo (id_alumni, descripcion, empresa, ciudad, fecha_inicio) "
                                + "VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, trabajo.getDescripcion());
                    ps.setString(3, nvl(trabajo.getLugar()).isEmpty() ? "Sin especificar" : trabajo.getLugar());
                    ps.setString(4, trabajo.getCiudadTrabajo());
                    java.util.Date inicio = trabajo.getFechaInicio() == null
                            ? new java.util.Date() : trabajo.getFechaInicio();
                    ps.setDate(5, new Date(inicio.getTime()));
                    ps.executeUpdate();
                }
            }

            // Preferencias de privacidad campo a campo (RI-9, RF-11)
            upsertPref(con, id, "email", alumni.getMostrarEmail());
            upsertPref(con, id, "telefono", alumni.getMostrarTelefono());
            upsertPref(con, id, "ciudad", alumni.getMostrarCiudad());
            upsertPref(con, id, "trabajo", alumni.getMostrarTrabajo());
            upsertPref(con, id, "hobbies", alumni.getMostrarHobbies());
            upsertPref(con, id, "contacto", alumni.getMostrarContacto());

            con.commit();
            return alumni;
        } catch (SQLException e) {
            rollback(con);
            throw new DataAccessException("Error al actualizar el perfil del alumni.", e);
        } finally {
            cerrar(con);
        }
    }

    /**
     * Baja de un Alumni. Por cumplimiento RGPD (RN-10) realiza anonimizacion,
     * no borrado fisico.
     */
    public boolean deleteByUsuario(String usuario) {
        return usuarios.anonimizar(usuario);
    }

    // --- Utilidades ----------------------------------------------------------

    private void upsertPref(Connection con, int idAlumni, String campo, Boolean visible) throws SQLException {
        boolean valor = visible != null && visible;
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO preferencia_privacidad (id_alumni, campo, es_visible) VALUES (?,?,?) "
                        + "ON DUPLICATE KEY UPDATE es_visible = VALUES(es_visible)")) {
            ps.setInt(1, idAlumni);
            ps.setString(2, campo);
            ps.setBoolean(3, valor);
            ps.executeUpdate();
        }
    }

    private boolean notBlank(String value) {
        return value != null && value.trim().length() > 0;
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
