package es.loyola.classes;

import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
import java.util.List;

public interface Ptgas extends Usuario {
    String getTitulacion();
    void setTitulacion(String titulacion);
    Campus getCampus();
    void setCampus(Campus campus);
    Facultad getFacultad();
    void setFacultad(Facultad facultad);
    String getAreaActual();
    void setAreaActual(String areaActual);
    Boolean getEnProyectoInvestigacion();
    void setEnProyectoInvestigacion(Boolean enProyectoInvestigacion);
    String getDepartamento();
    void setDepartamento(String departamento);
    List<String> getProyectos();
    void setProyectos(List<String> proyectos);
    void publicarEvento();
    void publicarActividad();
    void modificarEvento();
    void editarPerfil();
    void usarBuscador();
}
