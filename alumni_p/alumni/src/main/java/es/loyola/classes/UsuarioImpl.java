package es.loyola.classes;

import java.util.Date;

public class UsuarioImpl implements Usuario {
    private static int nextId = 1;

    private Integer id;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private Credenciales credenciales;
    private boolean sesionIniciada;
    private String estadoCuenta; // ACTIVA, SUSPENDIDA, ANONIMIZADA, PENDIENTE_ACTIVACION
    private Date fechaUltimoAcceso;

    public UsuarioImpl(String nombre, String apellidos, String email, String telefono, Credenciales credenciales) {
        this(nextId++, nombre, apellidos, email, telefono, credenciales);
    }

    public UsuarioImpl(Integer id, String nombre, String apellidos, String email, String telefono, Credenciales credenciales) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefono = telefono;
        this.credenciales = credenciales;
        this.estadoCuenta = "ACTIVA";
        this.fechaUltimoAcceso = new Date();
        if (id != null && id >= nextId) {
            nextId = id + 1;
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getUsuario() {
        return credenciales == null ? null : credenciales.getUsuario();
    }

    public Credenciales getCredenciales() {
        return credenciales;
    }

    public void setCredenciales(Credenciales credenciales) {
        this.credenciales = credenciales;
    }

    public boolean iniciarSesion(String usuario, String contrasenia) {
        sesionIniciada = credenciales != null && credenciales.validar(usuario, contrasenia);
        if (sesionIniciada) {
            fechaUltimoAcceso = new Date();
        }
        return sesionIniciada;
    }

    public void cerrarSesion() {
        sesionIniciada = false;
    }

    public void modificarPerfil(String nombre, String apellidos, String email) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
    }

    public boolean isSesionIniciada() {
        return sesionIniciada;
    }

    public String getEstadoCuenta() {
        return estadoCuenta == null ? "ACTIVA" : estadoCuenta;
    }

    public void setEstadoCuenta(String estado) {
        this.estadoCuenta = estado;
    }

    public Date getFechaUltimoAcceso() {
        return fechaUltimoAcceso;
    }

    public void setFechaUltimoAcceso(Date fecha) {
        this.fechaUltimoAcceso = fecha;
    }
}
