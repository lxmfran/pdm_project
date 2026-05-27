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
import es.uloyola.pdm.Alumni_android.model.Inscripcion;

public class InscripcionAdapter extends RecyclerView.Adapter<InscripcionAdapter.VH> {

    public interface OnCancelar { void onCancelar(Inscripcion i); }

    private final List<Inscripcion> data = new ArrayList<>();
    private final OnCancelar listener;

    public InscripcionAdapter(OnCancelar l) { this.listener = l; }

    public void setAll(List<Inscripcion> n) {
        data.clear();
        if (n != null) data.addAll(n);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inscripcion, parent, false);
        return new VH(v);
    }
    @Override public void onBindViewHolder(@NonNull VH h, int p) { h.bind(data.get(p)); }
    @Override public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        TextView recurso, fecha, estado;
        Button cancelar;
        VH(View v) {
            super(v);
            recurso = v.findViewById(R.id.tvRecurso);
            fecha = v.findViewById(R.id.tvFecha);
            estado = v.findViewById(R.id.tvEstado);
            cancelar = v.findViewById(R.id.btnCancelar);
        }
        void bind(Inscripcion i) {
            recurso.setText(i.getTipo() + " #" + i.getRecursoId() + "  (ticket " + i.getTicket() + ")");
            fecha.setText("Inscrito: " + (i.getFechaInscripcion() != null ? i.getFechaInscripcion() : "-"));
            estado.setText("Estado: " + i.getEstado());
            cancelar.setVisibility(i.estaActiva() ? View.VISIBLE : View.GONE);
            cancelar.setOnClickListener(x -> { if (listener != null) listener.onCancelar(i); });
        }
    }
}
