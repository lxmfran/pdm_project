package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.ActividadResponse;
import es.uloyola.pdm.Alumni_android.model.Evento;
import es.uloyola.pdm.Alumni_android.model.EventoResponse;
import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.model.InscripcionRequest;
import es.uloyola.pdm.Alumni_android.session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Pantalla de detalle de evento o actividad, con boton de inscribirse/cancelar. */
public class DetalleEventoActivity extends AppCompatActivity {

    private int id;
    private String tipo;        // "evento" o "actividad"
    private boolean estaInscrito;
    private PvoService api;
    private ProgressBar pb;
    private Button btnInscribir, btnCancelar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_detalle_evento);

        id = getIntent().getIntExtra("id", -1);
        tipo = getIntent().getStringExtra("tipo");
        if (id <= 0 || tipo == null) { finish(); return; }
        setTitle("actividad".equalsIgnoreCase(tipo) ? "Actividad" : "Evento");

        pb = findViewById(R.id.progressBar);
        btnInscribir = findViewById(R.id.btnInscribirse);
        btnCancelar = findViewById(R.id.btnCancelar);
        btnInscribir.setOnClickListener(v -> inscribirse());
        btnCancelar.setOnClickListener(v -> cancelar());

        api = RetrofitClient.getService();
        cargar();
    }

    private void cargar() {
        pb.setVisibility(View.VISIBLE);
        if ("actividad".equalsIgnoreCase(tipo)) {
            api.obtenerActividad(id).enqueue(new Callback<ActividadResponse>() {
                @Override public void onResponse(Call<ActividadResponse> c, Response<ActividadResponse> r) {
                    pb.setVisibility(View.GONE);
                    ActividadResponse b = r.body();
                    if (b != null && b.isSuccess() && b.getActividad() != null) pintar(b.getActividad());
                    else Toast.makeText(DetalleEventoActivity.this, "No se pudo cargar", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<ActividadResponse> c, Throwable t) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(DetalleEventoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            api.obtenerEvento(id).enqueue(new Callback<EventoResponse>() {
                @Override public void onResponse(Call<EventoResponse> c, Response<EventoResponse> r) {
                    pb.setVisibility(View.GONE);
                    EventoResponse b = r.body();
                    if (b != null && b.isSuccess() && b.getEvento() != null) pintar(b.getEvento());
                    else Toast.makeText(DetalleEventoActivity.this, "No se pudo cargar", Toast.LENGTH_SHORT).show();
                }
                @Override public void onFailure(Call<EventoResponse> c, Throwable t) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(DetalleEventoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void pintar(Evento e) {
        ((TextView) findViewById(R.id.tvNombre)).setText(e.getNombre());
        ((TextView) findViewById(R.id.tvEstado)).setText(e.getEstado() != null ? e.getEstado() : "");
        ((TextView) findViewById(R.id.tvDescripcion)).setText(e.getDescripcion() != null ? e.getDescripcion() : "");
        ((TextView) findViewById(R.id.tvLugar)).setText("Lugar: " + (e.getLugar() != null ? e.getLugar() : "-"));
        ((TextView) findViewById(R.id.tvFecha)).setText("Fecha: " + (e.getFechaEvento() != null ? e.getFechaEvento() : "-")
                + (e.getFechaLimite() != null ? "   Limite: " + e.getFechaLimite() : ""));
        int cap = e.getCapacidadMaxima() == null ? 0 : e.getCapacidadMaxima();
        ((TextView) findViewById(R.id.tvAforo)).setText("Inscritos: " + e.getNumInscritos() + " / " + cap);

        // Comprobar si el usuario ya esta inscrito
        String yo = SessionManager.get().getUsuario().getUsuario();
        estaInscrito = e.getInscritos() != null && e.getInscritos().contains(yo);
        btnInscribir.setVisibility(estaInscrito ? View.GONE : View.VISIBLE);
        btnCancelar.setVisibility(estaInscrito ? View.VISIBLE : View.GONE);

        // Solo ALUMNI/PDI pueden inscribirse
        String rol = SessionManager.get().getRol();
        if (!"ALUMNI".equalsIgnoreCase(rol) && !"PDI".equalsIgnoreCase(rol)) {
            btnInscribir.setVisibility(View.GONE);
            btnCancelar.setVisibility(View.GONE);
        }
    }

    private void inscribirse() {
        pb.setVisibility(View.VISIBLE);
        api.inscribirse(new InscripcionRequest(tipo, id)).enqueue(new Callback<GenericResponse>() {
            @Override public void onResponse(Call<GenericResponse> c, Response<GenericResponse> r) {
                pb.setVisibility(View.GONE);
                GenericResponse b = r.body();
                if (b != null && b.isSuccess()) { Toast.makeText(DetalleEventoActivity.this, "Inscrito correctamente", Toast.LENGTH_SHORT).show(); cargar(); }
                else Toast.makeText(DetalleEventoActivity.this, b != null && b.getError() != null ? b.getError() : "Error", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<GenericResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(DetalleEventoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelar() {
        pb.setVisibility(View.VISIBLE);
        api.cancelarInscripcion(new InscripcionRequest(tipo, id)).enqueue(new Callback<GenericResponse>() {
            @Override public void onResponse(Call<GenericResponse> c, Response<GenericResponse> r) {
                pb.setVisibility(View.GONE);
                GenericResponse b = r.body();
                if (b != null && b.isSuccess()) { Toast.makeText(DetalleEventoActivity.this, "Inscripcion cancelada", Toast.LENGTH_SHORT).show(); cargar(); }
                else Toast.makeText(DetalleEventoActivity.this, b != null && b.getError() != null ? b.getError() : "Error", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<GenericResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(DetalleEventoActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
