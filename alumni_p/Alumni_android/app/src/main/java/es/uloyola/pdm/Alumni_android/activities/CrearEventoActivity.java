package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.ActividadResponse;
import es.uloyola.pdm.Alumni_android.model.EventoRequest;
import es.uloyola.pdm.Alumni_android.model.EventoResponse;
import es.uloyola.pdm.Alumni_android.session.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Crear evento o actividad.
 * Si el rol es ALUMNI/PDI el backend lo trata como PROPUESTA pendiente.
 * Si es PTGAS/ADMIN, lo publica directamente.
 */
public class CrearEventoActivity extends AppCompatActivity {

    private EditText etNombre, etDescripcion, etLugar, etPonente, etFecha, etLimite, etCapacidad;
    private RadioGroup rgTipo;
    private ProgressBar pb;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_crear_evento);

        String rol = SessionManager.get().getRol();
        boolean puedePublicar = "PTGAS".equalsIgnoreCase(rol) || "ADMIN".equalsIgnoreCase(rol);
        setTitle(puedePublicar ? R.string.publicar_evento : R.string.menu_proponer_evento);

        etNombre = findViewById(R.id.etNombre);
        etDescripcion = findViewById(R.id.etDescripcion);
        etLugar = findViewById(R.id.etLugar);
        etPonente = findViewById(R.id.etPonente);
        etFecha = findViewById(R.id.etFecha);
        etLimite = findViewById(R.id.etLimite);
        etCapacidad = findViewById(R.id.etCapacidad);
        rgTipo = findViewById(R.id.rgTipo);
        pb = findViewById(R.id.progressBar);
        btnEnviar = findViewById(R.id.btnEnviar);

        ((TextView) findViewById(R.id.tvAyuda)).setText(puedePublicar
                ? "Vas a publicar directamente el recurso."
                : "Tu propuesta quedara pendiente de aprobacion.");
        btnEnviar.setText(puedePublicar ? R.string.publicar_evento : R.string.enviar_propuesta);
        btnEnviar.setOnClickListener(v -> enviar());
    }

    private void enviar() {
        if (etNombre.getText().toString().trim().isEmpty()
                || etDescripcion.getText().toString().trim().isEmpty()
                || etLugar.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Nombre, descripcion y lugar son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean esActividad = rgTipo.getCheckedRadioButtonId() == R.id.rbActividad;

        EventoRequest req = new EventoRequest();
        req.nombre = etNombre.getText().toString().trim();
        req.descripcion = etDescripcion.getText().toString().trim();
        req.lugar = etLugar.getText().toString().trim();
        req.ponente = etPonente.getText().toString().trim();
        String f = etFecha.getText().toString().trim();
        String l = etLimite.getText().toString().trim();
        if (esActividad) req.fecha = f.isEmpty() ? null : f;
        else req.fechaEvento = f.isEmpty() ? null : f;
        req.fechaLimite = l.isEmpty() ? null : l;
        String cap = etCapacidad.getText().toString().trim();
        if (!cap.isEmpty()) {
            try {
                int n = Integer.parseInt(cap);
                if (esActividad) req.maxPlazas = n; else req.capacidadMaxima = n;
            } catch (NumberFormatException ignored) {}
        }

        pb.setVisibility(View.VISIBLE);
        btnEnviar.setEnabled(false);
        PvoService api = RetrofitClient.getService();
        if (esActividad) {
            api.crearActividad(req).enqueue(new Callback<ActividadResponse>() {
                @Override public void onResponse(Call<ActividadResponse> c, Response<ActividadResponse> r) { onOk(r.body() != null && r.body().isSuccess(), r.body() != null ? r.body().getError() : null); }
                @Override public void onFailure(Call<ActividadResponse> c, Throwable t) { onErr(t.getMessage()); }
            });
        } else {
            api.crearEvento(req).enqueue(new Callback<EventoResponse>() {
                @Override public void onResponse(Call<EventoResponse> c, Response<EventoResponse> r) { onOk(r.body() != null && r.body().isSuccess(), r.body() != null ? r.body().getError() : null); }
                @Override public void onFailure(Call<EventoResponse> c, Throwable t) { onErr(t.getMessage()); }
            });
        }
    }

    private void onOk(boolean ok, String error) {
        pb.setVisibility(View.GONE);
        btnEnviar.setEnabled(true);
        if (ok) { Toast.makeText(this, "Enviado correctamente", Toast.LENGTH_SHORT).show(); finish(); }
        else Toast.makeText(this, error != null ? error : "Error", Toast.LENGTH_LONG).show();
    }

    private void onErr(String msg) {
        pb.setVisibility(View.GONE);
        btnEnviar.setEnabled(true);
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_LONG).show();
    }
}
