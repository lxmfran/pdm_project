package es.uloyola.pdm.Alumni_android.classes;

/**
 * Usuario
 * -------
 * POJO que mapea el JSON 'usuario' que devuelve el backend (LoginServlet,
 * PerfilServlet, etc.). Los nombres de los campos coinciden EXACTAMENTE
 * con los del JSON para que Gson los rellene automaticamente.
 *
 * Algunos campos solo aparecen segun el rol (perfilAlumni para alumni,
 * areaTrabajo para PDI, departamento para PTGAS, permisos para admin):
 * si no estan en el JSON, Gson los deja a null/0 y no pasa nada.
 */
public class Usuario {

    private Integer id;
    private String  usuario;     // login
    private String  rol;         // ALUMNI / PDI / PTGAS / ADMIN
    private String  nombre;
    private String  apellidos;
    private String  email;
    private String  telefono;
    private String  dni;
    private String  estadoCuenta;
    private String  ultimoAcceso;

    // Campos especificos por rol (los que no apliquen llegan a null)
    private String  titulacion;
    private String  campus;
    private String  facultad;
    private String  areaTrabajo;     // PDI
    private String  areaActual;      // PTGAS
    private Boolean enProyectoInvestigacion;
    private String  departamento;    // PTGAS

    // --- getters / setters --------------------------------------------------

    public Integer getId()                  { return id; }
    public void    setId(Integer id)        { this.id = id; }

    public String getUsuario()              { return usuario; }
    public void   setUsuario(String v)      { this.usuario = v; }

    public String getRol()                  { return rol; }
    public void   setRol(String v)          { this.rol = v; }

    public String getNombre()               { return nombre; }
    public void   setNombre(String v)       { this.nombre = v; }

    public String getApellidos()            { return apellidos; }
    public void   setApellidos(String v)    { this.apellidos = v; }

    public String getEmail()                { return email; }
    public void   setEmail(String v)        { this.email = v; }

    public String getTelefono()             { return telefono; }
    public void   setTelefono(String v)     { this.telefono = v; }

    public String getDni()                  { return dni; }
    public void   setDni(String v)          { this.dni = v; }

    public String getEstadoCuenta()         { return estadoCuenta; }
    public void   setEstadoCuenta(String v) { this.estadoCuenta = v; }

    public String getUltimoAcceso()         { return ultimoAcceso; }
    public void   setUltimoAcceso(String v) { this.ultimoAcceso = v; }

    public String getTitulacion()           { return titulacion; }
    public void   setTitulacion(String v)   { this.titulacion = v; }

    public String getCampus()               { return campus; }
    public void   setCampus(String v)       { this.campus = v; }

    public String getFacultad()             { return facultad; }
    public void   setFacultad(String v)     { this.facultad = v; }

    public String getAreaTrabajo()          { return areaTrabajo; }
    public void   setAreaTrabajo(String v)  { this.areaTrabajo = v; }

    public String getAreaActual()           { return areaActual; }
    public void   setAreaActual(String v)   { this.areaActual = v; }

    public Boolean getEnProyectoInvestigacion()        { return enProyectoInvestigacion; }
    public void    setEnProyectoInvestigacion(Boolean v) { this.enProyectoInvestigacion = v; }

    public String getDepartamento()         { return departamento; }
    public void   setDepartamento(String v) { this.departamento = v; }

    /** Nombre completo para mostrar en pantalla. */
    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null)    sb.append(nombre);
        if (apellidos != null) sb.append(sb.length() > 0 ? " " : "").append(apellidos);
        if (sb.length() == 0 && usuario != null) sb.append(usuario);
        return sb.toString();
    }
}
