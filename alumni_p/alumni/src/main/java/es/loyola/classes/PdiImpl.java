package es.loyola.classes;

import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;

public class PdiImpl extends UsuarioImpl implements Pdi {
    private String titulacion;
    private Campus campus;
    private Facultad facultad;
    private String areaTrabajo;

    public PdiImpl(String nombre, String apellidos, String email, String telefono,
                   Credenciales credenciales, String titulacion, Campus campus, Facultad facultad) {
        this(nombre, apellidos, email, telefono, credenciales, titulacion, campus, facultad, "");
    }

    public PdiImpl(String nombre, String apellidos, String email, String telefono,
                   Credenciales credenciales, String titulacion, Campus campus, Facultad facultad,
                   String areaTrabajo) {
        super(nombre, apellidos, email, telefono, credenciales);
        this.titulacion = titulacion;
        this.campus = campus;
        this.facultad = facultad;
        this.areaTrabajo = areaTrabajo;
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

    public String getAreaTrabajo() {
        return areaTrabajo;
    }

    public void setAreaTrabajo(String areaTrabajo) {
        this.areaTrabajo = areaTrabajo;
    }

    public void buscarAlumni() {
    }

    public void modificarPerfil() {
    }

    public void solicitarEvento() {
    }

    public void inscribirseEvento() {
    }
}
