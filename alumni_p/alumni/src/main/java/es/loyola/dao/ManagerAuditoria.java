package es.loyola.dao;

import es.loyola.classes.RegistroAuditoria;
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
 * DAO del registro de auditoria sobre la tabla 'registro_auditoria' (RNF-9, RI-8, RN-11).
 *
 * La tabla exige un actor existente (FK NOT NULL a 'usuario'). Si la accion la
 * origina un login que no corresponde a ningun usuario (p. ej. un intento de
 * acceso con un usuario inexistente), el evento no puede persistirse: en ese
 * caso se devuelve un registro transitorio sin guardar.
 */
public class ManagerAuditoria {

    private static final String SELECT_BASE =
            "SELECT r.id_registro, r.fecha, r.accion, r.entidad_afectada, r.id_entidad, "
          + "       r.resultado, r.detalle, c.usuario_login AS actor_login, u.rol AS actor_rol "
          + "FROM registro_auditoria r "
          + "JOIN usuario u ON u.id_usuario = r.id_actor "
          + "LEFT JOIN credenciales c ON c.id_usuario = r.id_actor ";

    /**
     * Registra una accion sensible. Si el actor no se puede resolver a un usuario
     * existente, devuelve un registro transitorio (no persistido).
     */
    public RegistroAuditoria registrar(String actor, String rol, String accion,
                                       String entidad, String entidadId,
                                       String resultado, String detalle) {
        boolean exito = resultado != null
                && ("OK".equalsIgnoreCase(resultado) || "EXITO".equalsIgnoreCase(resultado));
        String detalleCompleto = "[" + (resultado == null ? "INFO" : resultado) + "] "
                + (entidadId != null ? "ref=" + entidadId + " " : "")
                + (detalle == null ? "" : detalle);

        try (Connection con = Database.getConnection()) {
            Integer idActor = ManagerEventos.resolverId(con, actor);
            if (idActor == null) {
                // No se puede persistir sin actor valido: registro transitorio.
                return new RegistroAuditoria(null, actor, rol, accion, entidad, entidadId,
                        resultado, detalle);
            }
            Integer idEntidad = parseEntero(entidadId);
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO registro_auditoria (id_actor, accion, entidad_afectada, "
                            + "id_entidad, resultado, detalle) VALUES (?,?,?,?,?,?)")) {
                ps.setInt(1, idActor);
                ps.setString(2, accion);
                ps.setString(3, entidad);
                if (idEntidad == null) {
                    ps.setNull(4, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(4, idEntidad);
                }
                ps.setString(5, exito ? "EXITO" : "ERROR");
                ps.setString(6, detalleCompleto);
                ps.executeUpdate();
            }
            return new RegistroAuditoria(null, actor, rol, accion, entidad, entidadId,
                    resultado, detalle);
        } catch (SQLException e) {
            throw new DataAccessException("Error al registrar la auditoria.", e);
        }
    }

    public List<RegistroAuditoria> findAll() {
        return consultar(SELECT_BASE + "ORDER BY r.fecha DESC", 0);
    }

    public List<RegistroAuditoria> findRecent(int max) {
        if (max <= 0) {
            return findAll();
        }
        return consultar(SELECT_BASE + "ORDER BY r.fecha DESC LIMIT " + max, 0);
    }

    private List<RegistroAuditoria> consultar(String sql, int ignored) {
        List<RegistroAuditoria> out = new ArrayList<RegistroAuditoria>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Timestamp fecha = rs.getTimestamp("fecha");
                Integer idEntidad = (Integer) rs.getObject("id_entidad");
                out.add(new RegistroAuditoria(
                        rs.getInt("id_registro"),
                        fecha == null ? new Date() : new Date(fecha.getTime()),
                        rs.getString("actor_login"),
                        rs.getString("actor_rol"),
                        rs.getString("accion"),
                        rs.getString("entidad_afectada"),
                        idEntidad == null ? null : String.valueOf(idEntidad),
                        rs.getString("resultado"),
                        rs.getString("detalle")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al consultar la auditoria.", e);
        }
        return out;
    }

    private Integer parseEntero(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
