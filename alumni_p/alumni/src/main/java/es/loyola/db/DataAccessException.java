package es.loyola.db;

/**
 * Excepcion no comprobada que envuelve los errores de acceso a datos (SQLException).
 * Permite que los DAO mantengan su API publica sin declarar 'throws', y que los
 * servlets la capturen mediante su bloque catch(Exception) generico (RNF-5).
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public DataAccessException(String mensaje) {
        super(mensaje);
    }
}
