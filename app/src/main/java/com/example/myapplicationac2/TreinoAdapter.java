package com.example.myapplicationac2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TreinoAdapter extends RecyclerView.Adapter<TreinoAdapter.ViewHolder> {

    public List<Treino> treinos;

    public interface OnItemClickListener {
        void onItemClick(Treino treino);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Treino treino);
    }

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    public TreinoAdapter(List<Treino> treinos, OnItemClickListener listener, OnItemLongClickListener longClickListener) {
        this.treinos = treinos;
        this.listener = listener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treino, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Treino treino = treinos.get(position);
        holder.tvNome.setText(treino.getNomeTreino());
        holder.tvTipo.setText("Tipo: " + treino.getTipoTreino());
        holder.tvData.setText("Data: " + treino.getDataTreino());
        holder.tvDuracao.setText("Duração: " + treino.getDuracaoTreino() + " min");
        holder.tvIntensidade.setText("Intensidade: " + treino.getIntensidadeTreino());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(treino);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(treino);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return treinos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNome, tvTipo, tvData, tvDuracao, tvIntensidade;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNome = itemView.findViewById(R.id.tvNome);
            tvTipo = itemView.findViewById(R.id.tvTipo);
            tvData = itemView.findViewById(R.id.tvData);
            tvDuracao = itemView.findViewById(R.id.tvDuracao);
            tvIntensidade = itemView.findViewById(R.id.tvIntensidade);
        }
    }
}
