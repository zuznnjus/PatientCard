package com.example.patientcard.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.patientcard.R;
import com.example.patientcard.domain.control.PatientDataHandler;

import org.hl7.fhir.r4.model.Patient;

public class PatientDataFragment extends Fragment {

    private final PatientDataHandler patientDataHandler;

    private TextView name;
    private TextView gender;
    private TextView birthDate;
    private TextView identifier;

    public PatientDataFragment(PatientDataHandler patientDataHandler) {
        this.patientDataHandler = patientDataHandler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = view.findViewById(R.id.textViewPatientName);
        gender = view.findViewById(R.id.textViewGender);
        birthDate = view.findViewById(R.id.textViewBirthDate);
        identifier = view.findViewById(R.id.textViewIdentifier);

        init();
    }

    private void init() {
        Patient patient = patientDataHandler.getPatient();
        name.setText(patient.getName().get(0).getNameAsSingleString()
                .replaceAll("\\d", ""));
        gender.setText(patient.getGender().toString());
        birthDate.setText(patient.getBirthDateElement().asStringValue());
        identifier.setText(patient.getIdElement().getIdPart());
    }
}
