package es.loyola.dao;

import es.loyola.classes.PropuestaEvento;
import es.loyola.classes.PropuestaEventoImpl;
import es.loyola.db.DataAccessException;
import es.loyola.db.Database;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO de propuestas de eventos/actividades sobre la tabla 'propuesta_evento'
 * (RNF-9, RF-4, RF-7, RI-6, RN-5).
 *
 * El esquema solo contempla los estados PENDIENTE/APROBADA/RECHAZADA. El estado
 * "PUBLICADA" del modelo Java se persiste como APROBADA con la columna
 * id_evento_generado rellena; al leer, esa columna no nula se interpreta como
 * "PUBLICADA".
 */
public class ManagerPropuestas {

    private static final String SELECT_BASE =
            "SELECT p.*, cs.usuario_login AS solicitante_login, us.rol AS solicitante_rol, "
          + "       ce.usuario_login AS evaluador_login "
          + "FROM propuesta_evento p "
          + "JOIN credenciales cs ON cs.id_usuario = p.id_solicitante "
          + "JOIN usuario us ON us.id_usuario = p.id_solicitante "
          + "LEFT JOIN credenciales ce ON ce.id_usuario = p.id_evaluador ";

    public PropuestaEvento save(PropuestaEvento propuesta) {
        Connection con = null;
        try {
            con = Database.getConnection();
            Integer idSolicitante = ManagerEventos.resolverId(con, propuesta.getSolicitante());
            if (idSolicitante == null) {
                throw new DataAccessException(
                        "El solicitante de la propuesta no existe: " + propuesta.getSolicitante());
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO propuesta_evento (titulo, descripcion, tipo_recurso, fecha_sugerida, "
                            + "ubicacion_sugerida, capacidad_sugerida, id_solicitante, estado) "
                            + "VALUES (?,?,?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                java.util.Date sugerida = propuesta.getFechaEvento() == null
                        ? new java.util.Date() : propuesta.getFechaEvento();
                ps.setString(1, nvl(propuesta.getNombre()));
                ps.setString(2, nvl(propuesta.getDescripcion()));
                ps.setString(3, tipoValido(propuesta.getTipo()));
                ps.setDate(4, new Date(sugerida.getTime()));
                ps.setString(5, propuesta.getLugar());
                if (propuesta.getCapacidadMaxima() == null) {
                    ps.setNull(6, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(6, propuesta.getCapacidadMaxima());
                }
                ps.setInt(7, idSolicitante);
                ps.setString(8, estadoHaciaBd(propuesta.getEstado()));
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    propuesta.setId(keys.getInt(1));
                }
            }
            return propuesta;
        } catch (SQLException e) {
            throw new DataAccessException("Error al guardar la propuesta.", e);
        } finally {
            cerrar(con);
        }
    }

    /**
     * Persiste los cambios de una propuesta evaluada (aprobacion, rechazo,
     * publicacion). Necesario porque, a diferencia de la version en memoria,
     * mutar el objeto no actualiza la BD.
     */
    public PropuestaEvento update(PropuestaEvento propuesta) {
        if (propuesta == null || propuesta.getId() == null) {
            return null;
        }
        Connection con = null;
        try {
            con = Database.getConnection();
            Integer idEvaluador = ManagerEventos.resolverId(con, propuesta.getEvaluador());
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE propuesta_evento SET titulo=?, descripcion=?, tipo_recurso=?, "
                            + "fecha_sugerida=?, ubicacion_sugerida=?, capacidad_sugerida=?, estado=?, "
                            + "motivo_rechazo=?, id_evaluador=?, fecha_decision=?, id_evento_generado=? "
                            + "WHERE id_propuesta=?")) {
                java.util.Date sugerida = propuesta.getFechaEvento() == null
                        ? new java.util.Date() : propuesta.getFechaEvento();
                ps.setString(1, nvl(propuesta.getNombre()));
                ps.setString(2, nvl(propuesta.getDescripcion()));
                ps.setString(3, tipoValido(propuesta.getTipo()));
                ps.setDate(4, new Date(sugerida.getTime()));
                ps.setString(5, propuesta.getLugar());
                if (propuesta.getCapacidadMaxima() == null) {
                    ps.setNull(6, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(6, propuesta.getCapacidadMaxima());
                }
                ps.setString(7, estadoHaciaBd(propuesta.getEstado()));
                ps.setString(8, propuesta.getMotivoDecision());
                if (idEvaluador == null) {
                    ps.setNull(9, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(9, idEvaluador);
                }
                if (propuesta.getFechaDecision() == null) {
                    ps.setNull(10, java.sql.Types.TIMESTAMP);
                } else {
                    ps.setTimestamp(10, new Timestamp(propuesta.getFechaDecision().getTime()));
                }
                if (propuesta.getRecursoPublicadoId() == null) {
                    ps.setNull(11, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(11, propuesta.getRecursoPublicadoId());
                }
                ps.setInt(12, propuesta.getId());
                ps.executeUpdate();
            }
            return propuesta;
        } catch (SQLException e) {
            throw new DataAccessException("Error al actualizar la propuesta.", e);
        } finally {
            cerrar(con);
        }
    }

    public List<PropuestaEvento> findAll() {
        return query(SELECT_BASE + "ORDER BY p.fecha_envio DESC", null);
    }

    public List<PropuestaEvento> findByEstado(String estado) {
        if (estado == null) {
            return findAll();
        }
        if ("PUBLICADA".equalsIgnoreCase(estado)) {
            return query(SELECT_BASE
                    + "WHERE p.id_evento_generado IS NOT NULL ORDER BY p.fecha_envio DESC", null);
        }
        return query(SELECT_BASE
                + "WHERE p.estado = ? AND p.id_evento_generado IS NULL ORDER BY p.fecha_envio DESC",
                estado.toUpperCase());
    }

    public List<PropuestaEvento> findBySolicitante(String solicitante) {
        return query(SELECT_BASE
                + "WHERE cs.usuario_login = ? ORDER BY p.fecha_envio DESC", solicitante);
    }

    public PropuestaEvento findById(Integer id) {
        if (id == null) {
            return null;
        }
        List<PropuestaEvento> lista = query(SELECT_BASE + "WHERE p.id_propuesta = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    public int countPendientes() {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM propuesta_evento WHERE estado = 'PENDIENTE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Error al contar propuestas pendientes.", e);
        }
    }

    /**
     * Crea una propuesta nueva en estado PENDIENTE (objeto en memoria, sin persistir).
     */
    public static PropuestaEvento nuevaPendiente(String tipo, String solicitante, String rolSolicitante,
                                                 String nombre, String descripcion, String lugar) {
        PropuestaEventoImpl propuesta = new PropuestaEventoImpl();
        propuesta.setTipo(tipo);
        propuesta.setSolicitante(solicitante);
        propuesta.setRolSolicitante(rolSolicitante);
        propuesta.setNombre(nombre);
        propuesta.setDescripcion(descripcion);
        propuesta.setLugar(lugar);
        propuesta.setEstado(PropuestaEvento.ESTADO_PENDIENTE);
        return propuesta;
    }

    // --- Mapeo y utilidades --------------------------------------------------

    private List<PropuestaEvento> query(String sql, Object param) {
        List<PropuestaEvento> out = new ArrayList<PropuestaEvento>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setObject(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(mapPropuesta(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error al consultar propuestas.", e);
        }
        return out;
    }

    private PropuestaEvento mapPropuesta(ResultSet rs) throws SQLException {
        PropuestaEventoImpl p = new PropuestaEventoImpl();
        p.setId(rs.getInt("id_propuesta"));
        p.setNombre(rs.getString("titulo"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setTipo(rs.getString("tipo_recurso"));
        p.setLugar(rs.getString("ubicacion_sugerida"));
        Date sugerida = rs.getDate("fecha_sugerida");
        p.setFechaEvento(sugerida == null ? null : new java.util.Date(sugerida.getTime()));
        int cap = rs.getInt("capacidad_sugerida");
        p.setCapacidadMaxima(rs.wasNull() ? null : Integer.valueOf(cap));
        p.setSolicitante(rs.getString("solicitante_login"));
        p.setRolSolicitante(rs.getString("solicitante_rol"));
        p.setMotivoDecision(rs.getString("motivo_rechazo"));
        p.setEvaluador(rs.getString("evaluador_login"));
        Timestamp envio = rs.getTimestamp("fecha_envio");
        p.setFechaEnvio(envio == null ? null : new java.util.Date(envio.getTime()));
        Timestamp decision = rs.getTimestamp("fecha_decision");
        p.setFechaDecision(decision == null ? null : new java.util.Date(decision.getTime()));
        int generado = rs.getInt("id_evento_generado");
        boolean tieneGenerado = !rs.wasNull();
        p.setRecursoPublicadoId(tieneGenerado ? Integer.valueOf(generado) : null);
        // Estado: si ya genero un recurso, se considera PUBLICADA
        if (tieneGenerado) {
            p.setEstado(PropuestaEvento.ESTADO_PUBLICADA);
        } else {
            p.setEstado(rs.getString("estado"));
        }
        return p;
    }

    private static String tipoValido(String tipo) {
        if (tipo != null && "ACTIVIDAD".equalsIgnoreCase(tipo)) {
            return "ACTIVIDAD";
        }
        return "EVENTO";
    }

    /** Estado del modelo -> estado valido de la columna (PENDIENTE/APROBADA/RECHAZADA). */
    private static String estadoHaciaBd(String estado) {
        if (estado == null) {
            return "PENDIENTE";
        }
        switch (estado.toUpperCase()) {
            case "RECHAZADA":
                return "RECHAZADA";
            case "APROBADA":
            case "PUBLICADA":
                return "APROBADA";
            default:
                return "PENDIENTE";
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
