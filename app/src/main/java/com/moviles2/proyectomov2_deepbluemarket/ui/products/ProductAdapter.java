package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.models.Producto;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Producto producto);
    }

    public interface OnProductActionListener {
        void onEdit(Producto producto);
        void onDelete(Producto producto);
        void onVerOfertas(Producto producto);
    }

    private final Context context;
    private final List<Producto> productos;
    private final OnProductClickListener clickListener;
    private OnProductActionListener actionListener; // null = modo normal

    // Constructor modo normal (ProductListActivity)
    public ProductAdapter(Context context, List<Producto> productos, OnProductClickListener listener) {
        this.context = context;
        this.productos = productos;
        this.clickListener = listener;
    }

    // Activa modo "mis productos" con botones editar/eliminar/ver ofertas
    public void setActionListener(OnProductActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto p = productos.get(position);
        holder.tvTitulo.setText(p.getTitulo());
        holder.tvCategoria.setText(p.getCategoria());
        holder.tvPrecio.setText(String.format("Bs. %.2f", p.getPrecio()));

        if (p.getImagenUrl() != null && !p.getImagenUrl().isEmpty()) {
            Glide.with(context).load(p.getImagenUrl()).centerCrop()
                    .placeholder(R.drawable.ic_launcher_background).into(holder.ivImagen);
        } else {
            holder.ivImagen.setImageResource(R.drawable.ic_launcher_background);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onProductClick(p));

        // Mostrar botones solo en modo "mis productos"
        if (actionListener != null) {
            holder.btnEditar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnVerOfertas.setVisibility(View.VISIBLE);
            holder.btnEditar.setOnClickListener(v -> actionListener.onEdit(p));
            holder.btnEliminar.setOnClickListener(v -> actionListener.onDelete(p));
            holder.btnVerOfertas.setOnClickListener(v -> actionListener.onVerOfertas(p));
        } else {
            holder.btnEditar.setVisibility(View.GONE);
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnVerOfertas.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return productos.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        TextView tvTitulo, tvCategoria, tvPrecio;
        ImageButton btnEditar, btnEliminar;
        Button btnVerOfertas;

        ViewHolder(View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivProductImage);
            tvTitulo = itemView.findViewById(R.id.tvProductTitulo);
            tvCategoria = itemView.findViewById(R.id.tvProductCategoria);
            tvPrecio = itemView.findViewById(R.id.tvProductPrecio);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
            btnVerOfertas = itemView.findViewById(R.id.btnVerOfertas);
        }
    }
}