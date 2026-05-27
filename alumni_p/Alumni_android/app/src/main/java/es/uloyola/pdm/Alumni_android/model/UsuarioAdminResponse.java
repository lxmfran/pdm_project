package es.uloyola.pdm.Alumni_android.model;

import es.uloyola.pdm.Alumni_android.classes.Usuario;

public class UsuarioAdminResponse extends GenericResponse {
    private Usuario usuario;
    private Boolean contrasenaTemporalGenerada;
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario v) { this.usuario = v; }
    public Boolean getContrasenaTemporalGenerada() { return contrasenaTemporalGenerada; }
    public void setContrasenaTemporalGenerada(Boolean v) { this.contrasenaTemporalGenerada = v; }
}
