package es.loyola.dao;

import es.loyola.classes.Alumni;
import es.loyola.classes.Inscripcion;
import es.loyola.classes.InscripcionImpl;
import es.loyola.classes.Pdi;
import es.loyola.classes.Usuario;
import es.loyola.dao.ManagerEventos.InscripcionResultado;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DAO de inscripciones sobre la tabla 'inscripcion' (RNF-9, RF-10, CU-004).
 *
 * Centraliza la transaccion de inscribir/cancelar aplicando las reglas de
 * negocio RN-6 a RN-9: recurso publicado, plazo abierto, aforo disponible,
 * no duplicidad y rol autorizado (Alumni o PDI).
 */
public class ManagerInscripciones {

    /**
     * Inscribe a un usuario en un evento o actividad de forma transaccional.
     *
     * @param idEvento    identificador del recurso (tabla evento)
     * @param usuario     usuario autenticado a inscribir
     * @param esActividad true si el recurso esperado es una actividad
     */
    public InscripcionResultado inscribir(Integer idEvento, Usuario usuario, boolean esActividad) {
        if (usuario == null || usuario.getId() == null) {
            return InscripcionResultado.notFound("Usuario no encontrado.");
        }
        if (!(usuario instanceof Alumni) && !(usuario instanceof Pdi)) {
            return InscripcionResultado.bad("Solo Alumni o PDI pueden inscribirse (RN-1, RF-10).");
        }
        if (idEvento == null) {
            return InscripcionResultado.bad("Debe indicar el identificador del recurso.");
        }
        int idUsuario = usuario.getId();
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            boolean rowEsActividad;
            String estado;
            int capacidad;
            int aforo;
            Timestamp fechaLimite;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT es_actividad, estado, capacidad_maxima, aforo_actual, fecha_limite_inscripcion "
                            + "FROM evento WHERE id_evento = ? FOR UPDATE")) {
                ps.setInt(1, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        con.rollback();
                        return InscripcionResultado.notFound(
                                (esActividad ? "Actividad" : "Evento") + " no encontrado.");
                    }
                    rowEsActividad = rs.getBoolean("es_actividad");
                    estado = rs.getString("estado");
                    capacidad = rs.getInt("capacidad_maxima");
                    aforo = rs.getInt("aforo_actual");
                    fechaLimite = rs.getTimestamp("fecha_limite_inscripcion");
                }
            }

            if (rowEsActividad != esActividad) {
                con.rollback();
                return InscripcionResultado.bad("El recurso indicado no es "
                        + (esActividad ? "una actividad." : "un evento."));
            }
            if (!"PUBLICADO".equalsIgnoreCase(estado)) {
                con.rollback();
                return InscripcionResultado.bad(
                        "El recurso no esta disponible para inscripcion (estado " + estado + ").");
            }
            if (fechaLimite != null && new Date().after(new Date(fechaLimite.getTime()))) {
                con.rollback();
                return InscripcionResultado.bad("El plazo de inscripcion ha finalizado (RN-7).");
            }

            // Comprobar inscripcion existente (RN-9)
            boolean existe = false;
            boolean cancelada = false;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT cancelada FROM inscripcion WHERE id_usuario=? AND id_evento=?")) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idEvento);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existe = true;
                        cancelada = rs.getBoolean("cancelada");
                    }
                }
            }
            if (existe && !cancelada) {
                con.rollback();
                return InscripcionResultado.conflict("El usuario ya esta inscrito en este recurso (RN-9).");
            }
            if (capacidad > 0 && aforo >= capacidad) {
                con.rollback();
                return InscripcionResultado.conflict("Se ha alcanzado el aforo maximo (RN-8).");
            }

            if (existe) {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE inscripcion SET cancelada=FALSE, fecha_cancelacion=NULL, "
                                + "fecha_inscripcion=NOW() WHERE id_usuario=? AND id_evento=?")) {
                    ps.setInt(1, idUsuario);
                    ps.setInt(2, idEvento);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO inscripcion (id_usuario, id_evento) VALUES (?,?)")) {
                    ps.setInt(1, idUsuario);
                    ps.setInt(2, idEvento);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE evento SET aforo_actual = aforo_actual + 1 WHERE id_evento=?")) {
                ps.setInt(1, idEvento);
                ps.executeUpdate();
            }

            con.commit();
            return InscripcionResultado.ok();
        } catch (SQLException e) {
            rollback(con);
            throw new DataAccessException("Error al procesar la inscripcion.", e);
        } finally {
            cerrar(con);
        }
    }

    /**
     * Cancela la inscripcion activa de un usuario en un recurso (RF-10).
     */
    public InscripcionResultado cancelar(Integer idEvento, String usuarioLogin) {
        if (idEvento == null || usuarioLogin == null) {
            return InscripcionResultado.bad("Debe indicar el recurso y el usuario.");
        }
        Connection con = null;
        try {
            con = Database.getConnection();
            con.setAutoCommit(false);

            Integer idUsuario = ManagerEventos.resolverId(con, usuarioLogin);
            if (idUsuario == null) {
                con.rollback();
                return InscripcionResultado.notFound("Usuario no encontrado.");
            }

            int filas;
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE inscripcion SET cancelada=TRUE, fecha_cancelacion=NOW() "
                            + "WHERE id_usuario=? AND id_evento=? AND cancelada=FALSE")) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idEvento);
                filas = ps.executeUpdate();
            }
            if (filas == 0) {
                con.rollback();
                return InscripcionResultado.notFound("No habia una inscripcion activa para este usuario.");
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE evento SET aforo_actual = GREATEST(aforo_actual - 1, 0) WHERE id_evento=?")) {
                ps.setInt(1, idEvento);
                ps.executeUpdate();
            }
            con.commit();
            return InscripcionResultado.ok();
        } catch (SQLException e) {
            rollback(con);
            throw new DataAccessException("Error al cancelar la inscripcion.", e);
        } finally {
            cerrar(con);
        }
    }

    /** Lista las inscripciones (activas y canceladas) de un usuario. */
    public List<Inscripcion> findByUsuario(String usuarioLogin) {
        List<Inscripcion> out = new ArrayList<Inscripcion>();
        if (usuarioLogin == null) {
            return out;
        }
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT i.id_inscripcion, i.id_evento, i.fecha_inscripcion, i.cancelada, "
                             + "       e.es_actividad, u.rol "
                             + "FROM inscripcion i "
                             + "JOIN credenciales c ON c.id_usuario = i.id_usuario "
                             + "JOIN usuario u ON u.id_usuario = i.id_usuario "
                             + "JOIN evento e ON e.id_evento = i.id_evento "
                             + "WHERE c.usuario_login = ? ORDER BY i.fecha_inscripcion DESC")) {
            ps.setString(1, usuarioLogin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getBoolean("es_actividad")
                            ? Inscripcion.TIPO_ACTIVIDAD : Inscripcion.TIPO_EVENTO;
                    InscripcionImpl i = new InscripcionImpl(rs.getInt("id_inscripcion"),
                            tipo, rs.getInt("id_evento"), usuarioLogin, rs.getString("rol"));
                    i.setEstado(rs.getBoolean("cancelada")
                            ? Inscripcion.ESTADO_CANCELADA : Inscripcion.ESTADO_CONFIRMADA);
                    Timestamp fecha = rs.getTimestamp("fecha_inscripcion");
                    if (fecha != null) {
                        i.setFechaInscripcion(new Date(fecha.getTime()));
                    }
                    out.add(i);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al listar inscripciones del usuario.", e);
        }
        return out;
    }

    /** Numero total de inscripciones activas (no canceladas). */
    public int total() {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM inscripcion WHERE cancelada = FALSE");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al contar inscripciones.", e);
        }
    }

    /**
     * En el modelo relacional la trazabilidad de inscripciones se conserva via
     * la clave foranea a 'usuario' (que solo se anonimiza, nunca se borra: RN-10).
     * Por tanto, no hay que reescribir las inscripciones: este metodo es un no-op.
     */
    public void anonimizar(String usuario, String reemplazo) {
        // No-op: la FK a usuario garantiza la trazabilidad tras anonimizar la cuenta.
    }

    // --- Utilidades ----------------------------------------------------------

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
