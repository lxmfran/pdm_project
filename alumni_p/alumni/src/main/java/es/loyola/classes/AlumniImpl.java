package es.loyola.classes;

import es.loyola.enums.Campus;
import es.loyola.enums.Facultad;

public class AlumniImpl extends UsuarioImpl implements Alumni {
    private String titulacion;
    private String ciudadResidencia;
    private String hobbies;
    private Integer anioGraduacion;
    private Facultad facultad;
    private Campus campus;
    private Trabajo trabajo;
    private String trabajoActual;
    private String fotoPerfil;

    // Privacidad granular: por defecto sólo nombre y promoción públicos (RN-3)
    private Boolean mostrarContacto;
    private Boolean mostrarEmail;
    private Boolean mostrarTelefono;
    private Boolean mostrarCiudad;
    private Boolean mostrarTrabajo;
    private Boolean mostrarHobbies;

    public AlumniImpl(String nombre, String apellidos, String email, String telefono, Credenciales credenciales,
                      String titulacion, Integer anio, Facultad facultad, Campus campus,
                      Trabajo trabajo, String ciudad, String hobbies) {
        this(nombre, apellidos, email, telefono, credenciales, titulacion, anio, facultad, campus,
                trabajo, ciudad, hobbies, "");
    }

    public AlumniImpl(String nombre, String apellidos, String email, String telefono, Credenciales credenciales,
                      String titulacion, Integer anio, Facultad facultad, Campus campus,
                      Trabajo trabajo, String ciudad, String hobbies, String fotoPerfil) {
        super(nombre, apellidos, email, telefono, credenciales);
        this.titulacion = titulacion;
        this.anioGraduacion = anio;
        this.facultad = facultad;
        this.campus = campus;
        this.trabajo = trabajo;
        this.trabajoActual = trabajo == null ? "" : trabajo.getDescripcion();
        this.ciudadResidencia = ciudad;
        this.hobbies = hobbies;
        this.fotoPerfil = fotoPerfil;
        // Privacidad por defecto: contacto general visible, datos sensibles ocultos (RN-3)
        this.mostrarContacto = Boolean.TRUE;
        this.mostrarEmail = Boolean.FALSE;
        this.mostrarTelefono = Boolean.FALSE;
        this.mostrarCiudad = Boolean.TRUE;
        this.mostrarTrabajo = Boolean.TRUE;
        this.mostrarHobbies = Boolean.TRUE;
    }

    public Facultad getFacultad() {
        return facultad;
    }

    public void setFacultad(Facultad facultad) {
        this.facultad = facultad;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    public Trabajo getTrabajo() {
        return trabajo;
    }

    public void setTrabajo(Trabajo trabajo) {
        this.trabajo = trabajo;
        this.trabajoActual = trabajo == null ? "" : trabajo.getDescripcion();
    }

    public String getTrabajoActual() {
        return trabajoActual;
    }

    public void setTrabajoActual(String trabajoActual) {
        this.trabajoActual = trabajoActual;
    }

    public String getCiudadResidencia() {
        return ciudadResidencia;
    }

    public void setCiudadResidencia(String ciudad) {
        this.ciudadResidencia = ciudad;
    }

    public String getCiudad() {
        return ciudadResidencia;
    }

    public void setCiudad(String ciudad) {
        this.ciudadResidencia = ciudad;
    }

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getTitulacion() {
        return titulacion;
    }

    public void setTitulacion(String titulacion) {
        this.titulacion = titulacion;
    }

    public Integer getAnioGraduacion() {
        return anioGraduacion;
    }

    public void setAnioGraduacion(Integer anio) {
        this.anioGraduacion = anio;
    }

    public Integer getPromocion() {
        return anioGraduacion;
    }

    public void setPromocion(Integer anio) {
        this.anioGraduacion = anio;
    }

    public Boolean getMostrarContacto() {
        return mostrarContacto;
    }

    public void setMostrarContacto(Boolean mostrarContacto) {
        this.mostrarContacto = mostrarContacto;
    }

    public Boolean getMostrarEmail() {
        return mostrarEmail == null ? Boolean.FALSE : mostrarEmail;
    }

    public void setMostrarEmail(Boolean valor) {
        this.mostrarEmail = valor;
    }

    public Boolean getMostrarTelefono() {
        return mostrarTelefono == null ? Boolean.FALSE : mostrarTelefono;
    }

    public void setMostrarTelefono(Boolean valor) {
        this.mostrarTelefono = valor;
    }

    public Boolean getMostrarCiudad() {
        return mostrarCiudad == null ? Boolean.TRUE : mostrarCiudad;
    }

    public void setMostrarCiudad(Boolean valor) {
        this.mostrarCiudad = valor;
    }

    public Boolean getMostrarTrabajo() {
        return mostrarTrabajo == null ? Boolean.TRUE : mostrarTrabajo;
    }

    public void setMostrarTrabajo(Boolean valor) {
        this.mostrarTrabajo = valor;
    }

    public Boolean getMostrarHobbies() {
        return mostrarHobbies == null ? Boolean.TRUE : mostrarHobbies;
    }

    public void setMostrarHobbies(Boolean valor) {
        this.mostrarHobbies = valor;
    }

    public void setUsuario(String usuario) {
        getCredenciales().setUsuario(usuario);
    }

    public void setContrasenia(String contrasenia) {
        getCredenciales().setContrasenia(contrasenia);
    }

    public void buscarAlumni() {
    }

    public void inscribirseEvento() {
    }

    public void modificarDatos() {
    }
}
