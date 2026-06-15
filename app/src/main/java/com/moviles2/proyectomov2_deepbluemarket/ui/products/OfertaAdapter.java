package com.moviles2.proyectomov2_deepbluemarket.ui.products;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moviles2.proyectomov2_deepbluemarket.R;
import com.moviles2.proyectomov2_deepbluemarket.network.OfertaService;

import java.util.List;

public class OfertaAdapter extends RecyclerView.Adapter<OfertaAdapter.ViewHolder> {

    public interface OnOfertaActionListener {
        void onAceptar(OfertaService.OfertaConUsuario oferta);
        void onRechazar(OfertaService.OfertaConUsuario oferta);
    }

    private final Context context;
    private final List<OfertaService.OfertaConUsuario> ofertas;
    private final OnOfertaActionListener listener;

    public OfertaAdapter(Context context, List<OfertaService.OfertaConUsuario> ofertas, OnOfertaActionListener listener) {
        this.context = context;
        this.ofertas = ofertas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_oferta, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OfertaService.OfertaConUsuario oferta = ofertas.get(position);

        String nombre = oferta.nombreOfertante != null ? oferta.nombreOfertante : "Usuario desconocido";

        holder.tvNombre.setText(nombre);
        holder.tvMonto.setText(String.format("Bs. %.2f", oferta.monto));

        holder.btnAceptar.setOnClickListener(v -> listener.onAceptar(oferta));
        holder.btnRechazar.setOnClickListener(v -> listener.onRechazar(oferta));
    }

    @Override
    public int getItemCount() {
        return ofertas.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvMonto;
        Button btnAceptar, btnRechazar;

        ViewHolder(View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvOfertaNombre);
            tvMonto = itemView.findViewById(R.id.tvOfertaMonto);
            btnAceptar = itemView.findViewById(R.id.btnAceptarOferta);
            btnRechazar = itemView.findViewById(R.id.btnRechazarOferta);
        }
    }
}