package com.example.patientcard.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientcard.R;

import org.hl7.fhir.r4.model.Resource;

import java.text.SimpleDateFormat;
import java.util.List;

public class ResourceHistoryAdapter extends RecyclerView.Adapter<ResourceHistoryAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd hh:mm");
    private final Context context;
    private final List<Resource> resourceHistoryList;
    private ItemClickListener itemClickListener;

    public ResourceHistoryAdapter(Context context, List<Resource> resourceHistoryList) {
        this.context = context;
        this.resourceHistoryList = resourceHistoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.resource_history_item, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Resource resource = resourceHistoryList.get(position);
        holder.textViewVersion.setText(resource.getMeta().getVersionId());
        holder.textViewDateModified.setText(DATE_FORMATTER.format(resource.getMeta().getLastUpdated()));
    }

    @Override
    public int getItemCount() {
        return resourceHistoryList.size();
    }

    public void updateData(List<Resource> resourceHistoryList) {
        this.resourceHistoryList.clear();
        this.resourceHistoryList.addAll(resourceHistoryList);
        notifyDataSetChanged();
    }

    public Resource getResourceAtPosition(int position) {
        return resourceHistoryList.get(position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewVersion;
        TextView textViewDateModified;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.textViewVersion = itemView.findViewById(R.id.textViewVersionId);
            this.textViewDateModified = itemView.findViewById(R.id.textViewDateModified);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public void setClickListener(ResourceHistoryAdapter.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
