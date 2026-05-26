package es.loyola.dao;

import es.loyola.classes.Alumni;
import es.loyola.classes.AlumniImpl;
import es.loyola.classes.CredencialesImpl;
import es.loyola.classes.Evento;
import es.loyola.classes.EventoImpl;
import es.loyola.classes.OrganizadorImpl;
import es.loyola.classes.Usuario;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
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
 * DAO de Eventos sobre la tabla 'evento' de la BD (RNF-9).
 *
 * El esquema fusiona Eventos y Actividades en una sola tabla con el flag
 * 'es_actividad'. Esta clase opera siempre con es_actividad = FALSE.
 */
public class ManagerEventos {

    private static final String SELECT_BASE =
            "SELECT e.*, (SELECT c.usuario_login FROM credenciales c "
          + "             WHERE c.id_usuario = e.id_responsable) AS responsable_login "
          + "FROM evento e ";

    /**
     * Resultado de una operacion de inscripcion con motivo y codigo HTTP.
     */
    public static class InscripcionResultado {
        public final boolean ok;
        public final String motivo;
        public final int status;

        private InscripcionResultado(boolean ok, String motivo, int status) {
            this.ok = ok;
            this.motivo = motivo;
            this.status = status;
        }

        public static InscripcionResultado ok() {
            return new InscripcionResultado(true, "Operacion realizada correctamente.", 200);
        }

        public static InscripcionResultado bad(String motivo) {
            return new InscripcionResultado(false, motivo, 400);
        }

        public static InscripcionResultado notFound(String motivo) {
            return new InscripcionResultado(false, motivo, 404);
        }

        public static InscripcionResultado conflict(String motivo) {
            return new InscripcionResultado(false, motivo, 409);
        }
    }

    public List<Evento> findAll() {
        return query(SELECT_BASE + "WHERE e.es_actividad = FALSE ORDER BY e.fecha_inicio");
    }

    public List<Evento> findVisibles() {
        return query(SELECT_BASE
                + "WHERE e.es_actividad = FALSE AND e.estado = 'PUBLICADO' ORDER BY e.fecha_inicio");
    }

    public Evento findById(Integer id) {
        if (id == null) {
            return null;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     SELECT_BASE + "WHERE e.id_evento = ? AND e.es_actividad = FALSE")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Evento evento = mapEvento(rs);
                cargarInscritos(con, evento);
                return evento;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al buscar el evento.", e);
        }
    }

    public Evento save(Evento evento) {
        Connection con = null;
        try {
            con = Database.getConnection();
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO evento (nombre, descripcion, es_actividad, fecha_inicio, "
                            + "fecha_limite_inscripcion, ubicacion, capacidad_maxima, aforo_actual, "
                            + "estado, id_responsable) VALUES (?,?,FALSE,?,?,?,?,0,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                Date fechaInicio = evento.getFechaEvento() == null ? new Date() : evento.getFechaEvento();
                Date fechaLimite = evento.getFechaLimiteInscripcion() == null
                        ? fechaInicio : evento.getFechaLimiteInscripcion();
                int capacidad = evento.getCapacidadMaxima() == null || evento.getCapacidadMaxima() <= 0
                        ? 100 : evento.getCapacidadMaxima();
                ps.setString(1, nvl(evento.getNombreEvento()));
                ps.setString(2, nvl(evento.getDescripcionEvento()));
                ps.setTimestamp(3, new Timestamp(fechaInicio.getTime()));
                ps.setTimestamp(4, new Timestamp(fechaLimite.getTime()));
                ps.setString(5, nvl(evento.getLugarEvento()));
                ps.setInt(6, capacidad);
                ps.setString(7, estadoValido(evento.getEstado()));
                Integer responsable = resolverId(con, evento.getPropietario());
                if (responsable == null) {
                    ps.setNull(8, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(8, responsable);
                }
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    evento.setId(keys.getInt(1));
                }
            }
            return evento;
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar el evento.", e);
        } finally {
            cerrar(con);
        }
    }

    public Evento update(Evento evento) {
        if (evento == null || evento.getId() == null) {
            return null;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE evento SET nombre=?, descripcion=?, fecha_inicio=?, "
                             + "fecha_limite_inscripcion=?, ubicacion=?, capacidad_maxima=?, estado=? "
                             + "WHERE id_evento=? AND es_actividad=FALSE")) {
            Date fechaInicio = evento.getFechaEvento() == null ? new Date() : evento.getFechaEvento();
            Date fechaLimite = evento.getFechaLimiteInscripcion() == null
                    ? fechaInicio : evento.getFechaLimiteInscripcion();
            int capacidad = evento.getCapacidadMaxima() == null || evento.getCapacidadMaxima() <= 0
                    ? 100 : evento.getCapacidadMaxima();
            ps.setString(1, nvl(evento.getNombreEvento()));
            ps.setString(2, nvl(evento.getDescripcionEvento()));
            ps.setTimestamp(3, new Timestamp(fechaInicio.getTime()));
            ps.setTimestamp(4, new Timestamp(fechaLimite.getTime()));
            ps.setString(5, nvl(evento.getLugarEvento()));
            ps.setInt(6, capacidad);
            ps.setString(7, estadoValido(evento.getEstado()));
            ps.setInt(8, evento.getId());
            ps.executeUpdate();
            return evento;
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar el evento.", e);
        }
    }

    public boolean deleteById(Integer id) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "DELETE FROM evento WHERE id_evento=? AND es_actividad=FALSE")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al eliminar el evento.", e);
        }
    }

    /** Inscribe a un usuario aplicando las reglas RN-6..RN-9 (delega en ManagerInscripciones). */
    public InscripcionResultado inscribir(Integer eventoId, Usuario usuario, ManagerInscripciones inscripciones) {
        return inscripciones.inscribir(eventoId, usuario, false);
    }

    /** Cancela la inscripcion de un usuario en un evento (RF-10). */
    public InscripcionResultado cancelarInscripcion(Integer eventoId, String usuario,
                                                    ManagerInscripciones inscripciones) {
        return inscripciones.cancelar(eventoId, usuario);
    }

    public int total() {
        return contar("SELECT COUNT(*) FROM evento WHERE es_actividad = FALSE");
    }

    public int totalPublicados() {
        return contar("SELECT COUNT(*) FROM evento WHERE es_actividad = FALSE AND estado = 'PUBLICADO'");
    }

    // --- Mapeo y utilidades --------------------------------------------------

    private List<Evento> query(String sql) {
        List<Evento> out = new ArrayList<Evento>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Evento evento = mapEvento(rs);
                cargarInscritos(con, evento);
                out.add(evento);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar eventos.", e);
        }
        return out;
    }

    private Evento mapEvento(ResultSet rs) throws SQLException {
        Timestamp inicio = rs.getTimestamp("fecha_inicio");
        Timestamp limite = rs.getTimestamp("fecha_limite_inscripcion");
        Timestamp creacion = rs.getTimestamp("fecha_creacion");
        EventoImpl evento = new EventoImpl(
                rs.getInt("id_evento"),
                rs.getString("nombre"),
                rs.getString("descripcion"),
                rs.getString("ubicacion"),
                "",
                new OrganizadorImpl("Universidad Loyola", "LOYOLA"),
                creacion == null ? null : new Date(creacion.getTime()),
                limite == null ? null : new Date(limite.getTime()),
                inicio == null ? null : new Date(inicio.getTime()),
                rs.getInt("capacidad_maxima"),
                new ArrayList<Alumni>());
        evento.setEstado(rs.getString("estado"));
        evento.setPropietario(rs.getString("responsable_login"));
        return evento;
    }

    /** Pobla la lista de alumni inscritos (no cancelados) de un evento. */
    private void cargarInscritos(Connection con, Evento evento) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT c.usuario_login FROM inscripcion i "
                        + "JOIN credenciales c ON c.id_usuario = i.id_usuario "
                        + "JOIN alumni a ON a.id_usuario = i.id_usuario "
                        + "WHERE i.id_evento = ? AND i.cancelada = FALSE")) {
            ps.setInt(1, evento.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    evento.getListaAlumni().add(alumniLigero(rs.getString("usuario_login")));
                }
            }
        }
    }

    /**
     * Alumni minimo (solo login) para serializar la lista de inscritos.
     * La credencial usa un marcador con prefijo de hash para evitar que
     * CredencialesImpl ejecute bcrypt en cada carga (RNF-2).
     */
    static Alumni alumniLigero(String login) {
        return new AlumniImpl(login, "", "", "",
                new CredencialesImpl(login, "sha256$placeholder$placeholder", ""),
                "", 0, Facultad.INGENIERIA, Campus.SEVILLA, null, "", "");
    }

    static Integer resolverId(Connection con, String login) throws SQLException {
        if (login == null || login.trim().length() == 0) {
            return null;
        }
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT id_usuario FROM credenciales WHERE usuario_login = ?")) {
            ps.setString(1, login.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Integer.valueOf(rs.getInt(1)) : null;
            }
        }
    }

    private int contar(String sql) {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al contar eventos.", e);
        }
    }

    static String estadoValido(String estado) {
        if (estado == null) {
            return "PUBLICADO";
        }
        String e = estado.toUpperCase();
        if (e.equals("PUBLICADO") || e.equals("CANCELADO") || e.equals("BORRADOR") || e.equals("FINALIZADO")) {
            return e;
        }
        if (e.equals("ELIMINADO")) {
            return "CANCELADO";
        }
        return "PUBLICADO";
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
