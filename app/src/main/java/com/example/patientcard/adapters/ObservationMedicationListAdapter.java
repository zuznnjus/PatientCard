package com.example.patientcard.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientcard.R;

import org.hl7.fhir.r4.model.Observation;

import java.text.SimpleDateFormat;
import java.util.List;

public class ObservationMedicationListAdapter extends RecyclerView.Adapter<ObservationMedicationListAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private final Context context;
    private final List<Observation> patientObservationList;
    private ItemClickListener itemClickListener;

    public ObservationMedicationListAdapter(Context context, List<Observation> patientObservationList) {
        this.context = context;
        this.patientObservationList = patientObservationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.observation_medication_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Observation observation = patientObservationList.get(position);

        String observationDate = DATE_FORMATTER.format(observation.getIssued());
        String observationCode = observation.getCode().getCodingFirstRep().getDisplay();
        holder.textViewObservationDate.setText(observationDate);
        holder.textViewObservationCode.setText(observationCode);
    }

    @Override
    public int getItemCount() {
        return patientObservationList.size();
    }

    public void updateData(List<Observation> newObservationList) {
        patientObservationList.clear();
        patientObservationList.addAll(newObservationList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView textViewObservationDate;
        TextView textViewObservationCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewObservationDate = itemView.findViewById(R.id.textViewObservationDate);
            textViewObservationCode = itemView.findViewById(R.id.textViewObservationCode);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public void setClickListener(ObservationMedicationListAdapter.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
