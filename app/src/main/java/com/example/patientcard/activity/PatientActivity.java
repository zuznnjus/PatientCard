package com.example.patientcard.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patientcard.R;
import com.example.patientcard.adapters.ObservationMedicationListAdapter;
import com.example.patientcard.domain.control.HapiFhirHandler;

import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientActivity extends AppCompatActivity implements ObservationMedicationListAdapter.ItemClickListener {

    private HapiFhirHandler hapiFhirHandler;

    private TextView name;
    private TextView gender;
    private TextView birthDate;
    private TextView identifier;

    private DateDialog beginDate;
    private DateDialog endDate;
    private Patient patient;
    private ObservationMedicationListAdapter observationMedicationListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Intent intent = getIntent();
        String patientId = intent.getStringExtra(MainActivity.PATIENT_ID_MESSAGE);
        hapiFhirHandler = (HapiFhirHandler) intent.getSerializableExtra(MainActivity.HAPI_FHIR_HANDLER_MESSAGE);

        name = findViewById(R.id.textViewPatientName);
        gender = findViewById(R.id.textViewGender);
        birthDate = findViewById(R.id.textViewBirthDate);
        identifier = findViewById(R.id.textViewIdentifier);

        EditText editStartDate = findViewById(R.id.editTextStartDate);
        EditText editEndDate = findViewById(R.id.editTextEndDate);
        beginDate = new DateDialog(this, editStartDate);
        endDate = new DateDialog(this, editEndDate);

        setObservationMedicationListAdapter(patientId);
    }

    @Override
    public void onItemClick(View view, int position) {

    }

    private void setObservationMedicationListAdapter(String patientId) {
        RecyclerView recyclerViewObservationMedication = findViewById(R.id.recyclerViewObservationMedication);
        recyclerViewObservationMedication.setLayoutManager(new LinearLayoutManager(this));
        observationMedicationListAdapter = new ObservationMedicationListAdapter(this, new ArrayList<>());
        observationMedicationListAdapter.setClickListener(this);
        recyclerViewObservationMedication.setAdapter(observationMedicationListAdapter);

        createGetPatientThread(patientId).start();
    }

    private Thread createGetPatientThread(String patientId) {
        return new Thread(() -> {
            patient = hapiFhirHandler.getPatientById(patientId);
            createGetPatientObservationMedicationThread(patient).start();
            runOnUiThread(() -> {
                name.setText(patient.getName().get(0).getNameAsSingleString()
                        .replaceAll("\\d", ""));
                gender.setText(patient.getGender().toString());
                birthDate.setText(patient.getBirthDateElement().asStringValue());
                identifier.setText(patient.getIdElement().getIdPart());
            });
        });
    }

    private Thread createGetPatientObservationMedicationThread(Patient patient) {
        return new Thread(() -> {
            List<Observation> observations = hapiFhirHandler.getObservations(patient);
            runOnUiThread(() -> observationMedicationListAdapter.updateData(observations));
        });
    }
}