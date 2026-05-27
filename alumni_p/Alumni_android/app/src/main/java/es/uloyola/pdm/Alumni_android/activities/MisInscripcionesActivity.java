package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.adapters.InscripcionAdapter;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.model.Inscripcion;
import es.uloyola.pdm.Alumni_android.model.InscripcionRequest;
import es.uloyola.pdm.Alumni_android.model.InscripcionesResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MisInscripcionesActivity extends AppCompatActivity implements InscripcionAdapter.OnCancelar {

    private InscripcionAdapter adapter;
    private ProgressBar pb;
    private TextView tvInfo, tvVacio;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lista_recursos);
        setTitle(R.string.menu_mis_inscripciones);

        RecyclerView rv = findViewById(R.id.rv);
        pb = findViewById(R.id.progressBar);
        tvInfo = findViewById(R.id.tvInfo);
        tvVacio = findViewById(R.id.tvVacio);
        adapter = new InscripcionAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        cargar();
    }

    @Override protected void onResume() { super.onResume(); cargar(); }

    private void cargar() {
        pb.setVisibility(View.VISIBLE);
        RetrofitClient.getService().misInscripciones().enqueue(new Callback<InscripcionesResponse>() {
            @Override public void onResponse(Call<InscripcionesResponse> c, Response<InscripcionesResponse> r) {
                pb.setVisibility(View.GONE);
                InscripcionesResponse b = r.body();
                if (b == null || !b.isSuccess()) { Toast.makeText(MisInscripcionesActivity.this, "No se pudo cargar", Toast.LENGTH_SHORT).show(); return; }
                adapter.setAll(b.getInscripciones());
                tvInfo.setText(adapter.getItemCount() + " inscripciones");
                tvVacio.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<InscripcionesResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(MisInscripcionesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCancelar(Inscripcion i) {
        String tipo = "ACTIVIDAD".equalsIgnoreCase(i.getTipo()) ? "actividad" : "evento";
        PvoService api = RetrofitClient.getService();
        api.cancelarInscripcion(new InscripcionRequest(tipo, i.getRecursoId())).enqueue(new Callback<GenericResponse>() {
            @Override public void onResponse(Call<GenericResponse> c, Response<GenericResponse> r) {
                GenericResponse b = r.body();
                if (b != null && b.isSuccess()) { Toast.makeText(MisInscripcionesActivity.this, "Cancelada", Toast.LENGTH_SHORT).show(); cargar(); }
                else Toast.makeText(MisInscripcionesActivity.this, b != null && b.getError() != null ? b.getError() : "Error", Toast.LENGTH_SHORT).show();
            }
            @Override public void onFailure(Call<GenericResponse> c, Throwable t) {
                Toast.makeText(MisInscripcionesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
