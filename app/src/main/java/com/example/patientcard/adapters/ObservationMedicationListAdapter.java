package com.example.patientcard.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientcard.R;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ObservationMedicationListAdapter extends RecyclerView.Adapter<ObservationMedicationListAdapter.ViewHolder> {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private final Context context;
    private final List<Resource> patientObservationList;
    private ItemClickListener itemClickListener;

    public ObservationMedicationListAdapter(Context context, List<Resource> patientObservationList) {
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
        Resource resource = patientObservationList.get(position);
        String observationDate = StringUtils.EMPTY;
        String observationCode = StringUtils.EMPTY;
        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;
            observationDate = DATE_FORMATTER.format(observation.getIssued());
            observationCode = observation.getCode().getCodingFirstRep().getDisplay();
        } else if (resource instanceof MedicationRequest) {
            MedicationRequest medicationRequest = (MedicationRequest) resource;
            observationDate = DATE_FORMATTER.format(medicationRequest.getAuthoredOn());
            observationCode = medicationRequest.getMedicationCodeableConcept().getCodingFirstRep().getDisplay();
        }
        holder.textViewObservationDate.setText(observationDate);
        holder.textViewObservationCode.setText(observationCode);
    }

    @Override
    public int getItemCount() {
        return patientObservationList.size();
    }

    public void updateData(List<Resource> newObservationList) {
        patientObservationList.clear();
        patientObservationList.addAll(getSortedResources(newObservationList));
        notifyDataSetChanged();
    }

    private List<Resource> getSortedResources(List<Resource> resources) {
        return resources.stream()
                .sorted(Comparator.comparing(this::getResourceSortingKey))
                .collect(Collectors.toList());
    }

    private Date getResourceSortingKey(Resource resource) {
        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;
            return observation.getIssued();
        } else if (resource instanceof MedicationRequest) {
            MedicationRequest medicationRequest = (MedicationRequest) resource;
            return medicationRequest.getAuthoredOn();
        }
        return Calendar.getInstance().getTime();
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
