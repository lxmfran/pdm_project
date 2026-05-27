package es.uloyola.pdm.Alumni_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import es.uloyola.pdm.Alumni_android.util.AvatarUtil;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.model.AlumniResumen;

public class AlumniAdapter extends RecyclerView.Adapter<AlumniAdapter.VH> {

    public interface OnClick { void onAlumniClick(AlumniResumen a); }

    private final List<AlumniResumen> data = new ArrayList<>();
    private final OnClick listener;

    public AlumniAdapter(OnClick l) { this.listener = l; }

    public void setAll(List<AlumniResumen> nueva) {
        data.clear();
        if (nueva != null) data.addAll(nueva);
        notifyDataSetChanged();
    }

    public void addAll(List<AlumniResumen> mas) {
        if (mas == null || mas.isEmpty()) return;
        int p = data.size();
        data.addAll(mas);
        notifyItemRangeInserted(p, mas.size());
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alumni, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) { h.bind(data.get(pos)); }

    @Override
    public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView nombre, desc, trabajo;
        VH(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivAvatar);
            nombre = itemView.findViewById(R.id.tvNombre);
            desc = itemView.findViewById(R.id.tvDescripcion);
            trabajo = itemView.findViewById(R.id.tvTrabajo);
            itemView.setOnClickListener(v -> {
                int p = getAdapterPosition();
                if (p != RecyclerView.NO_POSITION && listener != null) listener.onAlumniClick(data.get(p));
            });
        }
        void bind(AlumniResumen a) {
            AvatarUtil.pintarEn(avatar, a.getNombreCompleto(), 48);
            nombre.setText(a.getNombreCompleto());
            desc.setText(a.getDescripcionCorta());
            String t = a.getTrabajoActual();
            if ((t == null || t.isEmpty()) && a.getTrabajo() != null) t = a.getTrabajo().getResumen();
            if (t != null && !t.isEmpty()) { trabajo.setText(t); trabajo.setVisibility(View.VISIBLE); }
            else trabajo.setVisibility(View.GONE);
        }
    }
}
