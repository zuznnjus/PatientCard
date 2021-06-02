package com.example.patientcard.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientcard.R;
import com.example.patientcard.dialog.DateDialog;
import com.example.patientcard.activity.PatientResourceActivity;
import com.example.patientcard.adapters.ObservationMedicationListAdapter;
import com.example.patientcard.domain.control.PatientDataHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class PatientResourcesFragment extends Fragment implements ObservationMedicationListAdapter.ItemClickListener {

    private final PatientDataHandler patientDataHandler;
    private DateDialog beginDate;
    private DateDialog endDate;
    private CheckBox checkBoxObservation;
    private CheckBox checkBoxMedication;
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

        checkBoxObservation = view.findViewById(R.id.checkBoxObservation);
        checkBoxMedication = view.findViewById(R.id.checkBoxMedication);
        checkBoxObservation.setOnCheckedChangeListener(
                (buttonView, isChecked) -> getFilteredResourcesThread(isChecked, checkBoxMedication.isChecked()).start());
        checkBoxMedication.setOnCheckedChangeListener(
                (buttonView, isChecked) -> getFilteredResourcesThread(checkBoxObservation.isChecked(), isChecked).start());

        setObservationMedicationListAdapter(view);
    }

    @Override
    public void onItemClick(View view, int position) {
        Resource resource = observationMedicationListAdapter.getResourceAtPosition(position);
        String resourceId = resource.getIdElement().getIdPart();

        Intent intent = new Intent(getContext(), PatientResourceActivity.class);
        intent.putExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE, patientDataHandler.getHapiFhirHandler());
        if (resource instanceof Observation) {
            intent.putExtra(IntentMessageCodes.OBSERVATION_ID_MESSAGE, resourceId);
        } else if (resource instanceof MedicationRequest) {
            intent.putExtra(IntentMessageCodes.MEDICATION_ID_MESSAGE, resourceId);
        }
        startActivity(intent);
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
                getFilteredResourcesThread(checkBoxObservation.isChecked(), checkBoxMedication.isChecked()).start();
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

    private Thread getFilteredResourcesThread(boolean getObservation, boolean getMedication) {
        return new Thread(() -> {
            String beginDateString = beginDate.getEditText().getText().toString();
            String endDateString = endDate.getEditText().getText().toString();
            List<Resource> filteredResources = patientDataHandler.getFilteredResources(beginDateString, endDateString, getObservation, getMedication);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> observationMedicationListAdapter.updateData(filteredResources));
            }
        });
    }

}
