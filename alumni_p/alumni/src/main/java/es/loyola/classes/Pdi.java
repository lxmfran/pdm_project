package es.loyola.classes;

import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;

public interface Pdi extends Usuario {
    String getTitulacion();
    void setTitulacion(String titulacion);
    Campus getCampus();
    void setCampus(Campus campus);
    Facultad getFacultad();
    void setFacultad(Facultad facultad);
    String getAreaTrabajo();
    void setAreaTrabajo(String areaTrabajo);
    void buscarAlumni();
    void modificarPerfil();
    void solicitarEvento();
    void inscribirseEvento();
}
