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
import org.hl7.fhir.r4.model.Patient;

import java.util.List;
import java.util.Optional;

public class PatientListAdapter extends RecyclerView.Adapter<PatientListAdapter.ViewHolder> {

    private final Context context;
    private final List<Patient> patientList;
    private ItemClickListener itemClickListener;

    public PatientListAdapter(Context context, List<Patient> patients) {
        this.context = context;
        this.patientList = patients;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.patientlist_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        if (patient != null) {
            String familyName = patient.getName().get(0).getFamily();
            Optional<String> givenName = Optional.empty();
            if (patient.getName().get(0).getGiven().size() > 0) {
                givenName = Optional.of(patient.getName().get(0).getGiven().get(0).getValueNotNull());
            }

            StringBuilder nameBuilder = new StringBuilder(familyName);
            givenName.ifPresent(given -> nameBuilder.append(" ").append(given));

            holder.textViewPatientName.setText(nameBuilder.toString()
                    .replaceAll("\\d", ""));
        }
    }

    @Override
    public int getItemCount() {
        return patientList.size();
    }

    public String getPatientId(int position) {
        Patient patient = patientList.get(position);
        if (patient != null) {
            return patient.getIdElement().getIdPart();
        }
        return StringUtils.EMPTY;
    }

    public void updateData(List<Patient> newPatientList) {
        patientList.clear();
        patientList.addAll(newPatientList);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewPatientName;

        ViewHolder(View itemView) {
            super(itemView);
            textViewPatientName = itemView.findViewById(R.id.textViewPatientName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
