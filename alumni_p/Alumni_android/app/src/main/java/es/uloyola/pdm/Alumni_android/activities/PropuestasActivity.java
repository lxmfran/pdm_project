package es.uloyola.pdm.Alumni_android.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.adapters.PropuestaAdapter;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.DecisionPropuestaRequest;
import es.uloyola.pdm.Alumni_android.model.GenericResponse;
import es.uloyola.pdm.Alumni_android.model.Propuesta;
import es.uloyola.pdm.Alumni_android.model.PropuestasResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PropuestasActivity extends AppCompatActivity implements PropuestaAdapter.OnDecision {

    private PropuestaAdapter adapter;
    private ProgressBar pb;
    private TextView tvInfo, tvVacio;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lista_recursos);
        setTitle(R.string.menu_propuestas);

        RecyclerView rv = findViewById(R.id.rv);
        pb = findViewById(R.id.progressBar);
        tvInfo = findViewById(R.id.tvInfo);
        tvVacio = findViewById(R.id.tvVacio);
        adapter = new PropuestaAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        cargar();
    }

    @Override protected void onResume() { super.onResume(); cargar(); }

    private void cargar() {
        pb.setVisibility(View.VISIBLE);
        RetrofitClient.getService().listarPropuestas(null).enqueue(new Callback<PropuestasResponse>() {
            @Override public void onResponse(Call<PropuestasResponse> c, Response<PropuestasResponse> r) {
                pb.setVisibility(View.GONE);
                PropuestasResponse b = r.body();
                if (b == null || !b.isSuccess()) { Toast.makeText(PropuestasActivity.this, "No se pudo cargar", Toast.LENGTH_SHORT).show(); return; }
                adapter.setAll(b.getPropuestas());
                tvInfo.setText(adapter.getItemCount() + " propuestas");
                tvVacio.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<PropuestasResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(PropuestasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDecision(Propuesta p, String decision) {
        if ("RECHAZAR".equals(decision)) {
            // Pedir motivo
            EditText et = new EditText(this);
            et.setHint("Motivo del rechazo");
            new AlertDialog.Builder(this)
                    .setTitle("Rechazar propuesta")
                    .setView(et)
                    .setPositiveButton("Rechazar", (d, w) -> enviar(new DecisionPropuestaRequest(p.getId(), "RECHAZAR", et.getText().toString().trim())))
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            enviar(new DecisionPropuestaRequest(p.getId(), decision, ""));
        }
    }

    private void enviar(DecisionPropuestaRequest req) {
        PvoService api = RetrofitClient.getService();
        api.decidirPropuesta(req).enqueue(new Callback<GenericResponse>() {
            @Override public void onResponse(Call<GenericResponse> c, Response<GenericResponse> r) {
                GenericResponse b = r.body();
                if (b != null && b.isSuccess()) { Toast.makeText(PropuestasActivity.this, "Hecho", Toast.LENGTH_SHORT).show(); cargar(); }
                else Toast.makeText(PropuestasActivity.this, b != null && b.getError() != null ? b.getError() : "Error", Toast.LENGTH_LONG).show();
            }
            @Override public void onFailure(Call<GenericResponse> c, Throwable t) {
                Toast.makeText(PropuestasActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
