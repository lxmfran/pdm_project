package es.loyola.jobs;

import es.loyola.classes.Notificacion;
import es.loyola.classes.Usuario;
import es.loyola.dao.ManagerAuditoria;
import es.loyola.dao.ManagerNotificaciones;
import es.loyola.dao.ManagerUsuarios;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Scheduler interno del backend (RF-12, RN-12).
 *
 * Arranca con el contexto del servlet y registra dos tareas periodicas:
 *   1) Recordatorio de inactividad: cada 24 h busca cuentas sin acceso > 365 dias
 *      y crea una notificacion + entrada de auditoria por cada una.
 *   2) Heartbeat de auditoria: marca en log que el scheduler sigue vivo.
 *
 * El listener se desregistra automaticamente al apagar Tomcat, parando el pool
 * de ejecucion para no dejar hilos huerfanos.
 *
 * Nota: este job replica la logica de InactividadServlet.doPost pero arrancada
 * automaticamente por el servidor; el endpoint manual sigue disponible para
 * disparos puntuales por parte de un Administrador.
 */
@WebListener
public class JobsContextListener implements ServletContextListener {

    private static final int DIAS_INACTIVIDAD = 365;

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "loyola-alumni-jobs");
            t.setDaemon(true);
            return t;
        });

        // 1) Job diario de recordatorios de inactividad: arranca a la proxima
        //    03:00 local y se repite cada 24 h (alineado con RNF-4 ventana valle).
        long delayInicial = segundosHasta(3, 0);
        scheduler.scheduleAtFixedRate(this::ejecutarRecordatorioInactividad,
                delayInicial, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);

        sce.getServletContext().log("JobsContextListener: scheduler de inactividad armado. "
                + "Primer disparo en " + delayInicial + " s (ventana 03:00).");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
            sce.getServletContext().log("JobsContextListener: scheduler detenido.");
        }
    }

    /** Logica del job: detecta inactivos, crea notificacion y registra auditoria. */
    private void ejecutarRecordatorioInactividad() {
        ManagerUsuarios usuariosManager = new ManagerUsuarios();
        ManagerAuditoria auditoria = new ManagerAuditoria();
        ManagerNotificaciones notif = new ManagerNotificaciones();
        try {
            List<Usuario> inactivos = usuariosManager.findInactivos(DIAS_INACTIVIDAD);
            int procesados = 0;
            for (Usuario u : inactivos) {
                String mensaje = "Hola " + (u.getNombre() == null ? u.getUsuario() : u.getNombre())
                        + ", tu cuenta de Loyola Alumni lleva mas de " + DIAS_INACTIVIDAD
                        + " dias inactiva. Vuelve a entrar para mantenerla activa.";
                notif.crear(u.getUsuario(), Notificacion.TIPO_INACTIVIDAD,
                        "Recordatorio de inactividad", mensaje);
                auditoria.registrar("SISTEMA", "ADMIN", "NOTIFICAR_INACTIVIDAD",
                        "Usuario", u.getUsuario(), "OK",
                        "Job InactividadScheduler. Umbral=" + DIAS_INACTIVIDAD + " dias.");
                procesados++;
            }
            // Auditoria del ciclo completo (siempre, aunque procesados=0)
            auditoria.registrar("SISTEMA", "ADMIN", "JOB_INACTIVIDAD",
                    "Sistema", null, "OK",
                    "Ciclo completado. Procesados=" + procesados + ".");
        } catch (Throwable t) {
            try {
                auditoria.registrar("SISTEMA", "ADMIN", "JOB_INACTIVIDAD",
                        "Sistema", null, "ERROR", t.getClass().getSimpleName()
                                + ": " + t.getMessage());
            } catch (Throwable ignored) { /* sin auditoria si la BD esta caida */ }
        }
    }

    /** Segundos desde ahora hasta el proximo h:m local. */
    private long segundosHasta(int hora, int minuto) {
        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        next.set(Calendar.HOUR_OF_DAY, hora);
        next.set(Calendar.MINUTE, minuto);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }
        long diffMs = next.getTimeInMillis() - now.getTimeInMillis();
        return Math.max(1, diffMs / 1000);
    }
}
