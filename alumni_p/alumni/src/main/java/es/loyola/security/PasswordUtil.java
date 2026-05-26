package es.loyola.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para almacenamiento seguro de contrasenas (RF-2, RN-1).
 *
 * El esquema de base de datos define la columna {@code hash_contrasena} como bcrypt,
 * por lo que esta clase genera hashes bcrypt mediante jBCrypt.
 *
 * {@link #verify(String, String)} reconoce tres formatos por compatibilidad:
 *   - bcrypt   ($2a$ / $2b$ / $2y$)  -> formato actual de produccion
 *   - sha256$  (hash heredado de la version en memoria)
 *   - texto plano (solo datos heredados sin hashear)
 */
public final class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final String SHA_PREFIX = "sha256$";
    private static final int BCRYPT_ROUNDS = 10;

    private PasswordUtil() {
    }

    /**
     * Genera un hash bcrypt a partir de una contrasena en texto plano.
     */
    public static String hash(String plain) {
        if (plain == null) {
            plain = "";
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Indica si un valor ya esta almacenado como hash (bcrypt o sha256).
     */
    public static boolean esHash(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$")
                || value.startsWith(SHA_PREFIX);
    }

    /**
     * Verifica una contrasena en texto plano contra un hash almacenado.
     */
    public static boolean verify(String plain, String stored) {
        if (stored == null) {
            return false;
        }
        if (plain == null) {
            plain = "";
        }
        if (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$")) {
            try {
                return BCrypt.checkpw(plain, stored);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        if (stored.startsWith(SHA_PREFIX)) {
            return verifySha256(plain, stored);
        }
        // Compatibilidad con datos heredados sin hashear
        return stored.equals(plain);
    }

    /**
     * Valida la politica de contrasenas: minimo 8 caracteres, al menos
     * una letra y un digito.
     */
    public static boolean isStrong(String plain) {
        if (plain == null || plain.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < plain.length(); i++) {
            char c = plain.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    // --- Soporte de hashes sha256 heredados ----------------------------------

    private static boolean verifySha256(String plain, String stored) {
        String[] parts = stored.split("\\$");
        if (parts.length != 3) {
            return false;
        }
        byte[] salt = fromHex(parts[1]);
        byte[] expected = fromHex(parts[2]);
        byte[] actual = digest(plain, salt);
        return constantTimeEquals(expected, actual);
    }

    private static byte[] digest(String plain, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            md.update(plain.getBytes(StandardCharsets.UTF_8));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo no disponible: " + ALGORITHM, e);
        }
    }

    private static byte[] fromHex(String hex) {
        int length = hex.length();
        byte[] out = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return out;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
