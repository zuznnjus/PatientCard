package com.example.patientcard.domain.webservice;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientcard.R;
import com.example.patientcard.activity.DateDialog;
import com.example.patientcard.adapters.ObservationMedicationListAdapter;
import com.example.patientcard.domain.control.PatientDataHandler;

import org.hl7.fhir.r4.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class PatientResourcesFragment extends Fragment implements ObservationMedicationListAdapter.ItemClickListener {

    private final PatientDataHandler patientDataHandler;
    private DateDialog beginDate;
    private DateDialog endDate;
    private ObservationMedicationListAdapter observationMedicationListAdapter;

    public PatientResourcesFragment(PatientDataHandler patientDataHandler) {
        this.patientDataHandler = patientDataHandler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_resources, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editStartDate = view.findViewById(R.id.editTextStartDate);
        EditText editEndDate = view.findViewById(R.id.editTextEndDate);
        beginDate = new DateDialog(view.getContext(), editStartDate);
        endDate = new DateDialog(view.getContext(), editEndDate);
        editStartDate.addTextChangedListener(createTextListener());
        editEndDate.addTextChangedListener(createTextListener());

        setObservationMedicationListAdapter(view);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private TextWatcher createTextListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                getFilteredResourcesThread().start();
            }
        };
    }

    private void setObservationMedicationListAdapter(View view) {
        RecyclerView recyclerViewObservationMedication = view.findViewById(R.id.recyclerViewObservationMedication);
        recyclerViewObservationMedication.setLayoutManager(new LinearLayoutManager(view.getContext()));
        observationMedicationListAdapter = new ObservationMedicationListAdapter(view.getContext(), new ArrayList<>());
        observationMedicationListAdapter.setClickListener(this);
        recyclerViewObservationMedication.setAdapter(observationMedicationListAdapter);
        observationMedicationListAdapter.updateData(patientDataHandler.getPatientResources());
    }

    private Thread getFilteredResourcesThread() {
        return new Thread(() -> {
            String beginDateString = beginDate.getEditText().getText().toString();
            String endDateString = endDate.getEditText().getText().toString();
            List<Resource> filteredResources = patientDataHandler.getResourcesBetweenGivenDates(beginDateString, endDateString);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> observationMedicationListAdapter.updateData(filteredResources));
            }
        });
    }

}
