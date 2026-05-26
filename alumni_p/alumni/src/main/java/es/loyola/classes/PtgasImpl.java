package es.loyola.classes;

import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;
import java.util.List;

public class PtgasImpl extends UsuarioImpl implements Ptgas {
    private String titulacion;
    private Campus campus;
    private Facultad facultad;
    private String areaActual;
    private Boolean enProyectoInvestigacion;
    private String departamento;
    private List<String> proyectos;

    public PtgasImpl(String nombre, String apellidos, String email, String telefono,
                     Credenciales credenciales, String titulacion, Campus campus,
                     Facultad facultad, List<String> proyectos) {
        this(nombre, apellidos, email, telefono, credenciales, titulacion, campus, facultad,
                "", Boolean.FALSE, "", proyectos);
    }

    public PtgasImpl(String nombre, String apellidos, String email, String telefono,
                     Credenciales credenciales, String titulacion, Campus campus,
                     Facultad facultad, String areaActual, Boolean enProyectoInvestigacion,
                     String departamento, List<String> proyectos) {
        super(nombre, apellidos, email, telefono, credenciales);
        this.titulacion = titulacion;
        this.campus = campus;
        this.facultad = facultad;
        this.areaActual = areaActual;
        this.enProyectoInvestigacion = enProyectoInvestigacion;
        this.departamento = departamento;
        this.proyectos = proyectos;
    }

    public String getTitulacion() {
        return titulacion;
    }

    public void setTitulacion(String titulacion) {
        this.titulacion = titulacion;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    public String getAreaActual() {
        return areaActual;
    }

    public void setAreaActual(String areaActual) {
        this.areaActual = areaActual;
    }

    public Boolean getEnProyectoInvestigacion() {
        return enProyectoInvestigacion;
    }

    public void setEnProyectoInvestigacion(Boolean enProyectoInvestigacion) {
        this.enProyectoInvestigacion = enProyectoInvestigacion;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public List<String> getProyectos() {
        return proyectos;
    }

    public void setProyectos(List<String> proyectos) {
        this.proyectos = proyectos;
    }

    public void publicarEvento() {
    }

    public void publicarActividad() {
    }

    public void modificarEvento() {
    }

    public void editarPerfil() {
    }

    public void usarBuscador() {
    }
}
