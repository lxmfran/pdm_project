package es.uloyola.pdm.Alumni_android.model;

/** POJO de alumni (resumen y perfil). Los campos opcionales pueden venir null por privacidad. */
public class AlumniResumen {
    private Integer id;
    private String usuario;
    private String nombre;
    private String apellidos;
    private String titulacion;
    private Integer promocion;
    private Integer anioGraduacion;
    private String facultad;
    private String campus;
    private String fotoPerfil;
    private Boolean mostrarContacto;

    // Privacidad granular (solo en perfil propio / admin)
    private Boolean mostrarEmail;
    private Boolean mostrarTelefono;
    private Boolean mostrarCiudad;
    private Boolean mostrarTrabajo;
    private Boolean mostrarHobbies;

    // Condicionales
    private String email;
    private String telefono;
    private String ciudad;
    private String ciudadResidencia;
    private String hobbies;
    private String trabajoActual;
    private Trabajo trabajo;

    public Integer getId() { return id; }
    public void setId(Integer v) { this.id = v; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String v) { this.usuario = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String v) { this.apellidos = v; }
    public String getTitulacion() { return titulacion; }
    public void setTitulacion(String v) { this.titulacion = v; }
    public Integer getPromocion() { return promocion; }
    public void setPromocion(Integer v) { this.promocion = v; }
    public Integer getAnioGraduacion() { return anioGraduacion; }
    public void setAnioGraduacion(Integer v) { this.anioGraduacion = v; }
    public String getFacultad() { return facultad; }
    public void setFacultad(String v) { this.facultad = v; }
    public String getCampus() { return campus; }
    public void setCampus(String v) { this.campus = v; }
    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String v) { this.fotoPerfil = v; }
    public Boolean getMostrarContacto() { return mostrarContacto; }
    public void setMostrarContacto(Boolean v) { this.mostrarContacto = v; }
    public Boolean getMostrarEmail() { return mostrarEmail; }
    public void setMostrarEmail(Boolean v) { this.mostrarEmail = v; }
    public Boolean getMostrarTelefono() { return mostrarTelefono; }
    public void setMostrarTelefono(Boolean v) { this.mostrarTelefono = v; }
    public Boolean getMostrarCiudad() { return mostrarCiudad; }
    public void setMostrarCiudad(Boolean v) { this.mostrarCiudad = v; }
    public Boolean getMostrarTrabajo() { return mostrarTrabajo; }
    public void setMostrarTrabajo(Boolean v) { this.mostrarTrabajo = v; }
    public Boolean getMostrarHobbies() { return mostrarHobbies; }
    public void setMostrarHobbies(Boolean v) { this.mostrarHobbies = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getCiudad() { return ciudad != null ? ciudad : ciudadResidencia; }
    public void setCiudad(String v) { this.ciudad = v; }
    public String getCiudadResidencia() { return ciudadResidencia; }
    public void setCiudadResidencia(String v) { this.ciudadResidencia = v; }
    public String getHobbies() { return hobbies; }
    public void setHobbies(String v) { this.hobbies = v; }
    public String getTrabajoActual() { return trabajoActual; }
    public void setTrabajoActual(String v) { this.trabajoActual = v; }
    public Trabajo getTrabajo() { return trabajo; }
    public void setTrabajo(Trabajo v) { this.trabajo = v; }

    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null) sb.append(nombre);
        if (apellidos != null) { if (sb.length() > 0) sb.append(' '); sb.append(apellidos); }
        if (sb.length() == 0 && usuario != null) sb.append(usuario);
        return sb.toString();
    }

    public String getDescripcionCorta() {
        StringBuilder sb = new StringBuilder();
        if (titulacion != null) sb.append(titulacion);
        if (promocion != null && promocion > 0) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append("Promocion ").append(promocion);
        }
        if (campus != null) {
            if (sb.length() > 0) sb.append(" · ");
            sb.append(campus);
        }
        return sb.toString();
    }
}
