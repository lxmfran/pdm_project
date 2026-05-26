package es.loyola.classes;

import es.loyola.security.PasswordUtil;

public class CredencialesImpl implements Credenciales {
    private String usuario;
    private String contrasenia; // almacenada como hash salado sha256$...
    private String dni;

    public CredencialesImpl(String usuario, String contrasenia) {
        this(usuario, contrasenia, "");
    }

    public CredencialesImpl(String usuario, String contrasenia, String dni) {
        this.usuario = usuario;
        // Hashea inmediatamente. Si llega ya un hash (bcrypt o sha256), se respeta tal cual.
        if (contrasenia == null) {
            this.contrasenia = null;
        } else if (PasswordUtil.esHash(contrasenia)) {
            this.contrasenia = contrasenia;
        } else {
            this.contrasenia = PasswordUtil.hash(contrasenia);
        }
        this.dni = dni;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    /**
     * Si la contraseña llega en claro, la hashea automáticamente.
     */
    public void setContrasenia(String contrasenia) {
        if (contrasenia == null) {
            this.contrasenia = null;
            return;
        }
        this.contrasenia = PasswordUtil.esHash(contrasenia)
                ? contrasenia
                : PasswordUtil.hash(contrasenia);
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public boolean validar(String usuario, String contrasenia) {
        if (this.usuario == null || !this.usuario.equalsIgnoreCase(usuario)) {
            return false;
        }
        return PasswordUtil.verify(contrasenia, this.contrasenia);
    }

    public void cambiarContrasena(String nuevaContrasena) {
        if (!PasswordUtil.isStrong(nuevaContrasena)) {
            throw new IllegalArgumentException(
                    "La contrasena no cumple la politica de seguridad (minimo 8 caracteres, con letra y digito).");
        }
        this.contrasenia = PasswordUtil.hash(nuevaContrasena);
    }
}
