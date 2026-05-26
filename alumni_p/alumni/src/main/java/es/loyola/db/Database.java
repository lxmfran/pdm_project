package es.loyola.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestor de conexiones a la base de datos relacional (RNF-9).
 *
 * Lee la configuracion de {@code db.properties} (en el classpath) y abre
 * conexiones JDBC bajo demanda mediante {@link DriverManager}. Cada operacion
 * de los DAO obtiene su propia conexion y la cierra con try-with-resources.
 *
 * Para una version de produccion se sustituiria por un pool (HikariCP / DataSource
 * JNDI), sin cambiar la API publica de esta clase ni la de los DAO.
 */
public final class Database {

    private static String url;
    private static String user;
    private static String password;
    private static boolean inicializado = false;

    private Database() {
    }

    private static synchronized void init() {
        if (inicializado) {
            return;
        }
        Properties props = new Properties();
        InputStream in = Database.class.getClassLoader().getResourceAsStream("db.properties");
        try {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Se continuara con los valores por defecto
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                }
            }
        }

        url = props.getProperty("db.url",
                "jdbc:mysql://localhost:3306/desarrollo_alumni"
                        + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Europe/Madrid"
                        + "&useSSL=false&allowPublicKeyRetrieval=true");
        user = props.getProperty("db.user", "root");
        password = props.getProperty("db.password", "");
        String driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");

        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException(
                    "No se encontro el driver JDBC de MySQL (" + driver
                            + "). Verifica que mysql-connector-j esta en el WAR.", e);
        }
        inicializado = true;
    }

    /**
     * Abre una nueva conexion a la base de datos. El llamador es responsable
     * de cerrarla (preferiblemente con try-with-resources).
     */
    public static Connection getConnection() {
        if (!inicializado) {
            init();
        }
        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            throw new DataAccessException(
                    "No se pudo conectar con la base de datos 'desarrollo_alumni'. "
                            + "Comprueba que XAMPP/MySQL esta arrancado en localhost:3306.", e);
        }
    }
}
