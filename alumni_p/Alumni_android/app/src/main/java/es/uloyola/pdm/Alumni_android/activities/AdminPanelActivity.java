package es.uloyola.pdm.Alumni_android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.conexionServer.RetrofitClient;
import es.uloyola.pdm.Alumni_android.model.DashboardResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPanelActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_panel);
        setTitle(R.string.menu_admin_panel);

        final ProgressBar pb = findViewById(R.id.progressBar);
        final TextView tv = findViewById(R.id.tvMetricas);
        pb.setVisibility(View.VISIBLE);

        RetrofitClient.getService().dashboard().enqueue(new Callback<DashboardResponse>() {
            @Override public void onResponse(Call<DashboardResponse> c, Response<DashboardResponse> r) {
                pb.setVisibility(View.GONE);
                DashboardResponse b = r.body();
                if (b == null || !b.isSuccess() || b.getMetricas() == null) {
                    Toast.makeText(AdminPanelActivity.this, "No se pudo cargar el dashboard", Toast.LENGTH_SHORT).show();
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Integer> e : b.getMetricas().entrySet()) {
                    sb.append(e.getKey()).append(":  ").append(e.getValue()).append('\n');
                }
                tv.setText(sb.toString());
            }
            @Override public void onFailure(Call<DashboardResponse> c, Throwable t) {
                pb.setVisibility(View.GONE);
                Toast.makeText(AdminPanelActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
