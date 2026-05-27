package es.uloyola.pdm.Alumni_android.model;

import java.util.List;

public class InscripcionesResponse extends GenericResponse {
    private List<Inscripcion> inscripciones;
    public List<Inscripcion> getInscripciones() { return inscripciones; }
    public void setInscripciones(List<Inscripcion> v) { this.inscripciones = v; }
}
