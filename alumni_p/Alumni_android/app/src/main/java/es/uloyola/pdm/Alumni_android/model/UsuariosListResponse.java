package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

import es.uloyola.pdm.Alumni_android.classes.Usuario;

public class UsuariosListResponse extends GenericResponse {
    private List<Usuario> usuarios;
    public List<Usuario> getUsuarios() { return usuarios; }
    public void setUsuarios(List<Usuario> v) { this.usuarios = v; }
}
