package es.uloyola.pdm.Alumni_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.model.Evento;

/** Adapter compartido para eventos y actividades. */
public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.VH> {

    public interface OnClick { void onEventoClick(Evento e); }

    private final List<Evento> data = new ArrayList<>();
    private final OnClick listener;

    public EventoAdapter(OnClick l) { this.listener = l; }

    public void setAll(List<Evento> n) {
        data.clear();
        if (n != null) data.addAll(n);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evento, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(data.get(pos)); }
    @Override public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        TextView nombre, lugarFecha, aforo;
        VH(View v) {
            super(v);
            nombre = v.findViewById(R.id.tvNombre);
            lugarFecha = v.findViewById(R.id.tvLugarFecha);
            aforo = v.findViewById(R.id.tvAforo);
            v.setOnClickListener(x -> {
                int p = getAdapterPosition();
                if (p != RecyclerView.NO_POSITION && listener != null) listener.onEventoClick(data.get(p));
            });
        }
        void bind(Evento e) {
            nombre.setText(e.getNombre());
            StringBuilder sb = new StringBuilder();
            if (e.getLugar() != null) sb.append(e.getLugar());
            String fecha = e.getFechaEvento();
            if (fecha != null) { if (sb.length() > 0) sb.append(" · "); sb.append(fecha); }
            lugarFecha.setText(sb.toString());
            int cap = e.getCapacidadMaxima() == null ? 0 : e.getCapacidadMaxima();
            aforo.setText(e.getNumInscritos() + "/" + cap + " inscritos · " + (e.getEstado() == null ? "" : e.getEstado()));
        }
    }
}
