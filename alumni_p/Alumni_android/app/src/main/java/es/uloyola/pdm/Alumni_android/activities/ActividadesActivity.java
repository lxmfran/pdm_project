package es.uloyola.pdm.Alumni_android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.adapters.EventoAdapter;
import es.uloyola.pdm.Alumni_android.conexionServer.PvoService;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.ActividadesResponse;
import es.uloyola.pdm.Alumni_android.model.Evento;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActividadesActivity extends AppCompatActivity implements EventoAdapter.OnClick {

    private EventoAdapter adapter;
    private ProgressBar pb;
    private TextView tvInfo, tvVacio;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_lista_recursos);
        setTitle(R.string.menu_actividades);

        RecyclerView rv = findViewById(R.id.rv);
        pb = findViewById(R.id.progressBar);
        tvInfo = findViewById(R.id.tvInfo);
        tvVacio = findViewById(R.id.tvVacio);

        adapter = new EventoAdapter(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        cargar();
    }

    @Override protected void onResume() { super.onResume(); cargar(); }

    private void cargar() {
        pb.setVisibility(View.VISIBLE);
        PvoService api = RetrofitClient.getService();
        api.listarActividades(new HashMap<>()).enqueue(new Callback<ActividadesResponse>() {
            @Override public void onResponse(Call<ActividadesResponse> c, Response<ActividadesResponse> r) {
                pb.setVisibility(View.GONE);
                ActividadesResponse b = r.body();
                if (b == null || !b.isSuccess()) {
                    Toast.makeText(ActividadesActivity.this, "No se pudo cargar", Toast.LENGTH_SHORT).show();
                    return;
                }
                adapter.setAll(b.getActividades());
                int total = b.getTotal() == null ? 0 : b.getTotal();
                tvInfo.setText(total + " actividades");
                tvVacio.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void onFailure(Call<ActividadesResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(ActividadesActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEventoClick(Evento e) {
        Intent i = new Intent(this, DetalleEventoActivity.class);
        i.putExtra("id", e.getId());
        i.putExtra("tipo", "actividad");
        startActivity(i);
    }
}
