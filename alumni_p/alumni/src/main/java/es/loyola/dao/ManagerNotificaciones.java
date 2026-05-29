package es.loyola.dao;

import es.loyola.classes.Notificacion;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
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
 * DAO de notificaciones internas (RI-11).
 *
 * Persiste en la tabla 'notificacion' definida en el esquema. Si el destinatario
 * no se puede resolver a un usuario existente, la creacion se ignora
 * silenciosamente (no rompe el flujo que genera la notificacion).
 */
public class ManagerNotificaciones {

    private static final String SELECT_BASE =
            "SELECT n.id_notificacion, n.tipo, n.asunto, n.mensaje, n.fecha_envio, n.leida, "
          + "       c.usuario_login AS destinatario "
          + "FROM notificacion n "
          + "JOIN credenciales c ON c.id_usuario = n.id_destinatario ";

    /** Crea y persiste una notificacion. Devuelve el objeto con el id asignado, o null si no se pudo. */
    public Notificacion crear(String destinatarioLogin, String tipo, String asunto, String mensaje) {
        if (destinatarioLogin == null || destinatarioLogin.isEmpty()
            || tipo == null || asunto == null || mensaje == null) {
            return null;
        }
        try (Connection con = Database.getConnection()) {
            Integer idDest = ManagerEventos.resolverId(con, destinatarioLogin);
            if (idDest == null) {
                return null; // destinatario inexistente: no se persiste
            }
            String sql = "INSERT INTO notificacion (id_destinatario, tipo, asunto, mensaje) "
                       + "VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idDest);
                ps.setString(2, tipo);
                ps.setString(3, recortar(asunto, 160));
                ps.setString(4, mensaje);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    Integer id = keys.next() ? keys.getInt(1) : null;
                    return new Notificacion(id, destinatarioLogin, tipo, asunto, mensaje,
                                            new Date(), false);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al crear notificacion.", e);
        }
    }

    /** Devuelve la bandeja del usuario, mas recientes primero. */
    public List<Notificacion> findByDestinatario(String destinatarioLogin) {
        List<Notificacion> out = new ArrayList<Notificacion>();
        if (destinatarioLogin == null || destinatarioLogin.isEmpty()) return out;
        String sql = SELECT_BASE + " WHERE c.usuario_login = ? ORDER BY n.fecha_envio DESC";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, destinatarioLogin);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al cargar notificaciones.", e);
        }
        return out;
    }

    /** Marca una notificacion como leida si pertenece al usuario indicado. */
    public boolean marcarLeida(int idNotificacion, String destinatarioLogin) {
        if (destinatarioLogin == null) return false;
        String sql = "UPDATE notificacion n "
                   + "JOIN credenciales c ON c.id_usuario = n.id_destinatario "
                   + "SET n.leida = TRUE "
                   + "WHERE n.id_notificacion = ? AND c.usuario_login = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idNotificacion);
            ps.setString(2, destinatarioLogin);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al marcar notificacion como leida.", e);
        }
    }

    /** Cuenta no leidas. */
    public int contarNoLeidas(String destinatarioLogin) {
        if (destinatarioLogin == null) return 0;
        String sql = "SELECT COUNT(*) FROM notificacion n "
                   + "JOIN credenciales c ON c.id_usuario = n.id_destinatario "
                   + "WHERE c.usuario_login = ? AND n.leida = FALSE";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, destinatarioLogin);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al contar notificaciones no leidas.", e);
        }
    }

    private Notificacion map(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("fecha_envio");
        return new Notificacion(
                rs.getInt("id_notificacion"),
                rs.getString("destinatario"),
                rs.getString("tipo"),
                rs.getString("asunto"),
                rs.getString("mensaje"),
                ts == null ? null : new Date(ts.getTime()),
                rs.getBoolean("leida")
        );
    }

    private static String recortar(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
