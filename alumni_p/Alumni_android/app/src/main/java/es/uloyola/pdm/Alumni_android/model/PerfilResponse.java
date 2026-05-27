package es.uloyola.pdm.Alumni_android.model;

public class PerfilResponse extends GenericResponse {
    private AlumniResumen perfil;
    public AlumniResumen getPerfil() { return perfil; }
    public void setPerfil(AlumniResumen v) { this.perfil = v; }
}
