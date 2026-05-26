package es.loyola.classes;

public interface Credenciales {
    String getUsuario();
    void setUsuario(String usuario);
    String getContrasenia();
    void setContrasenia(String contrasenia);
    String getDni();
    void setDni(String dni);
    boolean validar(String usuario, String contrasenia);
    void cambiarContrasena(String nuevaContrasena);
}
