package es.uloyola.pdm.Alumni_android.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import es.uloyola.pdm.Alumni_android.R;
import es.uloyola.pdm.Alumni_android.classes.Usuario;
import es.uloyola.pdm.Alumni_android.util.AvatarUtil;

/** Lista de usuarios del panel admin con botones Editar / Eliminar. */
public class UsuarioAdminAdapter extends RecyclerView.Adapter<UsuarioAdminAdapter.VH> {

    public interface Listener {
        void onEditar(Usuario u);
        void onEliminar(Usuario u);
    }

    private final List<Usuario> data = new ArrayList<>();
    private final Listener listener;

    public UsuarioAdminAdapter(Listener l) { this.listener = l; }

    public void setAll(List<Usuario> n) {
        data.clear();
        if (n != null) data.addAll(n);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_usuario_admin, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int p) { h.bind(data.get(p)); }
    @Override public int getItemCount() { return data.size(); }

    class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView nombre, email, rol;
        Button editar, eliminar;
        VH(View v) {
            super(v);
            avatar = v.findViewById(R.id.ivAvatar);
            nombre = v.findViewById(R.id.tvNombre);
            email = v.findViewById(R.id.tvEmail);
            rol = v.findViewById(R.id.tvRol);
            editar = v.findViewById(R.id.btnEditar);
            eliminar = v.findViewById(R.id.btnEliminar);
        }
        void bind(Usuario u) {
            AvatarUtil.pintarEn(avatar, u.getNombreCompleto(), 48);
            nombre.setText(u.getNombreCompleto());
            email.setText(u.getEmail() != null ? u.getEmail() : "");
            rol.setText(u.getRol() != null ? u.getRol() : "");
            editar.setOnClickListener(x -> { if (listener != null) listener.onEditar(u); });
            eliminar.setOnClickListener(x -> { if (listener != null) listener.onEliminar(u); });
        }
    }
}
