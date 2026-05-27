package es.uloyola.pdm.Alumni_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.model.Propuesta;

public class PropuestaAdapter extends RecyclerView.Adapter<PropuestaAdapter.VH> {

    public interface OnDecision { void onDecision(Propuesta p, String decision); }

    private final List<Propuesta> data = new ArrayList<>();
    private final OnDecision listener;

    public PropuestaAdapter(OnDecision l) { this.listener = l; }

    public void setAll(List<Propuesta> n) {
        data.clear();
        if (n != null) data.addAll(n);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_propuesta, parent, false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int p) { h.bind(data.get(p)); }
    @Override public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        TextView nombre, solicitante, estado, descripcion;
        Button btnA, btnR, btnP;
        VH(View v) {
            super(v);
            nombre = v.findViewById(R.id.tvNombre);
            solicitante = v.findViewById(R.id.tvSolicitante);
            estado = v.findViewById(R.id.tvEstado);
            descripcion = v.findViewById(R.id.tvDescripcion);
            btnA = v.findViewById(R.id.btnAprobar);
            btnR = v.findViewById(R.id.btnRechazar);
            btnP = v.findViewById(R.id.btnPublicar);
        }
        void bind(Propuesta p) {
            nombre.setText("[" + p.getTipo() + "] " + p.getNombre());
            solicitante.setText("De " + p.getSolicitante() + " (" + p.getRolSolicitante() + ")");
            estado.setText("Estado: " + p.getEstado());
            descripcion.setText(p.getDescripcion());
            String est = p.getEstado() == null ? "" : p.getEstado().toUpperCase();
            boolean pendiente = "PENDIENTE".equals(est);
            boolean aprobada = "APROBADA".equals(est);
            btnA.setVisibility(pendiente ? View.VISIBLE : View.GONE);
            btnR.setVisibility(pendiente ? View.VISIBLE : View.GONE);
            btnP.setVisibility((pendiente || aprobada) ? View.VISIBLE : View.GONE);
            btnA.setOnClickListener(x -> listener.onDecision(p, "APROBAR"));
            btnR.setOnClickListener(x -> listener.onDecision(p, "RECHAZAR"));
            btnP.setOnClickListener(x -> listener.onDecision(p, "PUBLICAR"));
        }
    }
}
