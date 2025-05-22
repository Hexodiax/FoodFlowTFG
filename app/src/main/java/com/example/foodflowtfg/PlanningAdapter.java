package com.example.foodflowtfg;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlanningAdapter extends RecyclerView.Adapter<PlanningAdapter.PlanningViewHolder> {

    private final List<String> planningList;
    private final PlanningListener listener;

    public interface PlanningListener {
        void onEditClick(String planningName);
        void onUseClick(String planningName);
        void onDeleteClick(String planningName);
    }

    public PlanningAdapter(List<String> planningList, PlanningListener listener) {
        this.planningList = planningList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlanningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_planning, parent, false);
        return new PlanningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanningViewHolder holder, int position) {
        String name = planningList.get(position);
        holder.planningName.setText(name);

        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(name));
        holder.btnUse.setOnClickListener(v -> listener.onUseClick(name));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(name));
    }

    @Override
    public int getItemCount() {
        return planningList.size();
    }

    static class PlanningViewHolder extends RecyclerView.ViewHolder {
        TextView planningName;
        ImageView btnEdit, btnUse, btnDelete;

        public PlanningViewHolder(@NonNull View itemView) {
            super(itemView);
            planningName = itemView.findViewById(R.id.planningName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnUse = itemView.findViewById(R.id.btnUse);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
