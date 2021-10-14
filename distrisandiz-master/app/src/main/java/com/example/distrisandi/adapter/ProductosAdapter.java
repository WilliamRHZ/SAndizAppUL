package com.example.distrisandi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.distrisandi.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductosViewHolder> {
    private List<Producto> productoList;
    private ProductosOnClick mListener;

    public ProductosAdapter(List<Producto> productoList, ProductosOnClick mListener){
        this.productoList = productoList;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public ProductosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductosViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductosViewHolder holder, int position) {
        Producto item = productoList.get(holder.getAdapterPosition());

        holder.tCantidad.setText(item.getCantidad());
        holder.tDescripcion.setText(item.getDescripcion());
        holder.tPrecioUnitario.setText("$"+item.getPrecioUnitario());
        holder.tTotal.setText("$"+item.getTotal());

        holder.tCantidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClick(holder.getAdapterPosition());
            }
        });
    }

    public List<Producto> getProductoList(){
        return this.productoList;
    }

    public void setProductoList(List<Producto> productoList){
        this.productoList = productoList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return productoList.size();
    }

    public interface ProductosOnClick{
        void onClick(int position);
    }

    public class ProductosViewHolder extends RecyclerView.ViewHolder{
        final TextView tCantidad, tDescripcion, tPrecioUnitario, tTotal;

        public ProductosViewHolder(@NonNull View itemView) {
            super(itemView);
            tCantidad = itemView.findViewById(R.id.tCantidad);
            tDescripcion = itemView.findViewById(R.id.tDescripcion);
            tPrecioUnitario = itemView.findViewById(R.id.tPrecioUnitario);
            tTotal = itemView.findViewById(R.id.tTotal);
        }
    }
}
