package es.loyola.dao;

import es.loyola.classes.Actividad;
import es.loyola.classes.ActividadImpl;
import es.loyola.classes.Alumni;
import es.loyola.classes.Hobbie;
import es.loyola.classes.HobbieImpl;
import es.loyola.classes.OrganizadorImpl;
import es.loyola.classes.Usuario;
import es.loyola.dao.ManagerEventos.InscripcionResultado;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
import es.loyola.db.Mappers;
import es.loyola.enums.ExperienciaHobbies;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO de Actividades sobre la tabla 'evento' de la BD (RNF-9).
 *
 * Opera siempre con es_actividad = TRUE. Comparte la tabla con los Eventos pero
 * mantiene su propio modelo de dominio (Actividad).
 */
public class ActividadManager {

    private static final String SELECT_BASE =
            "SELECT e.*, (SELECT c.usuario_login FROM credenciales c "
          + "             WHERE c.id_usuario = e.id_responsable) AS responsable_login "
          + "FROM evento e ";

    public List<Actividad> findAll() {
        return query(SELECT_BASE + "WHERE e.es_actividad = TRUE ORDER BY e.fecha_inicio");
    }

    public List<Actividad> findVisibles() {
        return query(SELECT_BASE
                + "WHERE e.es_actividad = TRUE AND e.estado = 'PUBLICADO' ORDER BY e.fecha_inicio");
    }

    public Actividad findById(Integer id) {
        if (id == null) {
            return null;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE e.id_evento = ? AND e.es_actividad = TRUE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Actividad actividad = mapActividad(rs);
                cargarInscritos(con, actividad);
                return actividad;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar la actividad.", e);
        }
    }

    public Actividad save(Actividad actividad) {
        Connection con = null;
        try {
            con = Database.getConnection();
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO evento (nombre, descripcion, es_actividad, nivel, hobby_relacionado, "
                            + "fecha_inicio, fecha_limite_inscripcion, ubicacion, capacidad_maxima, "
                            + "aforo_actual, estado, id_responsable) "
                            + "VALUES (?,?,TRUE,?,?,?,?,?,?,0,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                Date fechaInicio = actividad.getFecha() == null ? new Date() : actividad.getFecha();
                Date fechaLimite = actividad.getFechaLimiteInscripcion() == null
                        ? fechaInicio : actividad.getFechaLimiteInscripcion();
                int capacidad = actividad.getMaxPlazas() == null || actividad.getMaxPlazas() <= 0
                        ? 100 : actividad.getMaxPlazas();
                ps.setString(1, nvl(actividad.getNombreActividad()));
                ps.setString(2, nvl(actividad.getDescripcion()));
                ps.setString(3, Mappers.nivelHaciaBd(actividad.getNivelParticipacion()));
                ps.setString(4, actividad.getHobbiePracticado() == null
                        ? null : actividad.getHobbiePracticado().getNombreHobbie());
                ps.setTimestamp(5, new Timestamp(fechaInicio.getTime()));
                ps.setTimestamp(6, new Timestamp(fechaLimite.getTime()));
                ps.setString(7, nvl(actividad.getLugarActividad()));
                ps.setInt(8, capacidad);
                ps.setString(9, estadoHaciaBd(actividad.getEstado()));
                Integer responsable = ManagerEventos.resolverId(con, actividad.getPropietario());
                if (responsable == null) {
                    ps.setNull(10, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(10, responsable);
                }
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    actividad.setId(keys.getInt(1));
                }
            }
            return actividad;
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar la actividad.", e);
        } finally {
            cerrar(con);
        }
    }

    public Actividad update(Actividad actividad) {
        if (actividad == null || actividad.getId() == null) {
            return null;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE evento SET nombre=?, descripcion=?, nivel=?, hobby_relacionado=?, "
                             + "fecha_inicio=?, fecha_limite_inscripcion=?, ubicacion=?, "
                             + "capacidad_maxima=?, estado=? WHERE id_evento=? AND es_actividad=TRUE")) {
            Date fechaInicio = actividad.getFecha() == null ? new Date() : actividad.getFecha();
            Date fechaLimite = actividad.getFechaLimiteInscripcion() == null
                    ? fechaInicio : actividad.getFechaLimiteInscripcion();
            int capacidad = actividad.getMaxPlazas() == null || actividad.getMaxPlazas() <= 0
                    ? 100 : actividad.getMaxPlazas();
            ps.setString(1, nvl(actividad.getNombreActividad()));
            ps.setString(2, nvl(actividad.getDescripcion()));
            ps.setString(3, Mappers.nivelHaciaBd(actividad.getNivelParticipacion()));
            ps.setString(4, actividad.getHobbiePracticado() == null
                    ? null : actividad.getHobbiePracticado().getNombreHobbie());
            ps.setTimestamp(5, new Timestamp(fechaInicio.getTime()));
            ps.setTimestamp(6, new Timestamp(fechaLimite.getTime()));
            ps.setString(7, nvl(actividad.getLugarActividad()));
            ps.setInt(8, capacidad);
            ps.setString(9, estadoHaciaBd(actividad.getEstado()));
            ps.setInt(10, actividad.getId());
            ps.executeUpdate();
            return actividad;
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar la actividad.", e);
        }
    }

    public boolean deleteById(Integer id) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM evento WHERE id_evento=? AND es_actividad=TRUE")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al eliminar la actividad.", e);
        }
    }

    public InscripcionResultado inscribir(Integer actividadId, Usuario usuario,
                                          ManagerInscripciones inscripciones) {
        return inscripciones.inscribir(actividadId, usuario, true);
    }

    public InscripcionResultado cancelarInscripcion(Integer actividadId, String usuario,
                                                    ManagerInscripciones inscripciones) {
        return inscripciones.cancelar(actividadId, usuario);
    }

    public int total() {
        return contar("SELECT COUNT(*) FROM evento WHERE es_actividad = TRUE");
    }

    public int totalPublicadas() {
        return contar("SELECT COUNT(*) FROM evento WHERE es_actividad = TRUE AND estado = 'PUBLICADO'");
    }

    // --- Mapeo y utilidades --------------------------------------------------

    private List<Actividad> query(String sql) {
        List<Actividad> out = new ArrayList<Actividad>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Actividad actividad = mapActividad(rs);
                cargarInscritos(con, actividad);
                out.add(actividad);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar actividades.", e);
        }
        return out;
    }

    private Actividad mapActividad(ResultSet rs) throws SQLException {
        ExperienciaHobbies nivel = Mappers.nivelDesdeBd(rs.getString("nivel"));
        String hobbyNombre = rs.getString("hobby_relacionado");
        Hobbie hobbie = hobbyNombre == null ? null : new HobbieImpl(hobbyNombre, 1, nivel);
        Timestamp inicio = rs.getTimestamp("fecha_inicio");
        Timestamp limite = rs.getTimestamp("fecha_limite_inscripcion");
        Timestamp creacion = rs.getTimestamp("fecha_creacion");

        ActividadImpl actividad = new ActividadImpl(
                rs.getInt("id_evento"),
                rs.getString("ubicacion"),
                rs.getString("nombre"),
                hobbie,
                nivel,
                creacion == null ? null : new Date(creacion.getTime()),
                new ArrayList<Alumni>(),
                limite == null ? null : new Date(limite.getTime()),
                rs.getInt("capacidad_maxima"),
                new OrganizadorImpl("Universidad Loyola", "LOYOLA"));
        actividad.setDescripcion(nvl(rs.getString("descripcion")));
        actividad.setFecha(inicio == null ? null : new Date(inicio.getTime()));
        actividad.setFechaLimiteInscripcion(limite == null ? null : new Date(limite.getTime()));
        actividad.setEstado(estadoDesdeBd(rs.getString("estado")));
        actividad.setPropietario(rs.getString("responsable_login"));
        return actividad;
    }

    private void cargarInscritos(Connection con, Actividad actividad) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT c.usuario_login FROM inscripcion i "
                        + "JOIN credenciales c ON c.id_usuario = i.id_usuario "
                        + "JOIN alumni a ON a.id_usuario = i.id_usuario "
                        + "WHERE i.id_evento = ? AND i.cancelada = FALSE")) {
            ps.setInt(1, actividad.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    actividad.getAlumniInscritos().add(
                            ManagerEventos.alumniLigero(rs.getString("usuario_login")));
                }
            }
        }
    }

    /** Estado de BD (masculino) -> estado del modelo Actividad (femenino). */
    private String estadoDesdeBd(String db) {
        if (db == null) {
            return Actividad.ESTADO_PUBLICADA;
        }
        switch (db.toUpperCase()) {
            case "PUBLICADO":
                return Actividad.ESTADO_PUBLICADA;
            case "CANCELADO":
            case "FINALIZADO":
                return Actividad.ESTADO_CANCELADA;
            case "BORRADOR":
                return Actividad.ESTADO_BORRADOR;
            default:
                return Actividad.ESTADO_PUBLICADA;
        }
    }

    /** Estado del modelo Actividad -> estado valido de la columna evento.estado. */
    private String estadoHaciaBd(String estado) {
        if (estado == null) {
            return "PUBLICADO";
        }
        switch (estado.toUpperCase()) {
            case "PUBLICADA":
            case "PUBLICADO":
                return "PUBLICADO";
            case "CANCELADA":
            case "CANCELADO":
            case "ELIMINADA":
            case "ELIMINADO":
                return "CANCELADO";
            case "BORRADOR":
                return "BORRADOR";
            default:
                return "PUBLICADO";
        }
    }

    private int contar(String sql) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al contar actividades.", e);
        }
    }

    private static String nvl(String value) {
        return value == null ? "" : value;
    }

    private void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
