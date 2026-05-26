package es.loyola.classes;

import java.util.Date;

public interface Usuario {
    Integer getId();
    void setId(Integer id);
    String getNombre();
    void setNombre(String nombre);
    String getApellidos();
    void setApellidos(String apellidos);
    String getEmail();
    void setEmail(String email);
    String getTelefono();
    void setTelefono(String telefono);
    String getUsuario();
    Credenciales getCredenciales();
    void setCredenciales(Credenciales credenciales);
    boolean iniciarSesion(String usuario, String contrasenia);
    void cerrarSesion();
    void modificarPerfil(String nombre, String apellidos, String email);

    // Ciclo de vida de la cuenta: ACTIVA, SUSPENDIDA, ANONIMIZADA, PENDIENTE_ACTIVACION (RF-2, RN-10)
    String getEstadoCuenta();
    void setEstadoCuenta(String estado);
    Date getFechaUltimoAcceso();
    void setFechaUltimoAcceso(Date fecha);
}
