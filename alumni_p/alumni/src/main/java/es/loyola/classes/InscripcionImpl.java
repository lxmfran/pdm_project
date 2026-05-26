package es.loyola.classes;

import java.util.Date;

public class InscripcionImpl implements Inscripcion {
    private Integer id;
    private String tipoRecurso;
    private Integer recursoId;
    private String usuario;
    private String rolUsuario;
    private String ticket;
    private Date fechaInscripcion;
    private String estado;

    public InscripcionImpl(Integer id, Date fechaInscripcion, String estado) {
        this.id = id;
        this.fechaInscripcion = fechaInscripcion;
        this.estado = estado == null ? ESTADO_CONFIRMADA : estado;
        this.ticket = generarTicket();
    }

    public InscripcionImpl(Integer id, String tipoRecurso, Integer recursoId, String usuario, String rolUsuario) {
        this.id = id;
        this.tipoRecurso = tipoRecurso;
        this.recursoId = recursoId;
        this.usuario = usuario;
        this.rolUsuario = rolUsuario;
        this.fechaInscripcion = new Date();
        this.estado = ESTADO_CONFIRMADA;
        this.ticket = generarTicket();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) {
        this.id = id;
        this.ticket = generarTicket();
    }

    public String getTipoRecurso() { return tipoRecurso; }
    public void setTipoRecurso(String tipoRecurso) { this.tipoRecurso = tipoRecurso; }

    public Integer getRecursoId() { return recursoId; }
    public void setRecursoId(Integer recursoId) { this.recursoId = recursoId; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getRolUsuario() { return rolUsuario; }
    public void setRolUsuario(String rol) { this.rolUsuario = rol; }

    public String getTicket() { return ticket; }
    public void setTicket(String ticket) { this.ticket = ticket; }

    public Date getFechaInscripcion() { return fechaInscripcion; }
    public void setFechaInscripcion(Date fechaInscripcion) { this.fechaInscripcion = fechaInscripcion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String generarTicket() {
        String prefijo = (tipoRecurso != null && tipoRecurso.startsWith("ACT")) ? "ACT" : "EVT";
        return "INS-" + prefijo + "-" + (id == null ? System.currentTimeMillis() : id);
    }

    public void cancelar() {
        this.estado = ESTADO_CANCELADA;
    }
}
