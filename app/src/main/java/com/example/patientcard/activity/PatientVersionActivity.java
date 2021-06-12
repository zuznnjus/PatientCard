package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.patientcard.R;
import com.example.patientcard.domain.control.HapiFhirHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.hl7.fhir.r4.model.Patient;

public class PatientVersionActivity extends AppCompatActivity {

    private HapiFhirHandler hapiFhirHandler;

    private TextView identifier;
    private TextView name;
    private TextView gender;
    private TextView birthDate;
    private TextView contact;
    private TextView address;
    private TextView city;
    private TextView country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_version);

        identifier = findViewById(R.id.textViewPatientVersionIdentifier);
        name = findViewById(R.id.textViewPatientVersionName);
        gender = findViewById(R.id.textViewPatientVersionGender);
        birthDate = findViewById(R.id.textViewPatientVersionBirthDate);
        contact = findViewById(R.id.editTextPatientVersionContact);
        address = findViewById(R.id.editTextPatientVersionAddressLine);
        city = findViewById(R.id.editTextPatientVersionAddressCity);
        country = findViewById(R.id.editTextPatientVersionAddressCountry);

        String patientVersionUrl = getIntent().getStringExtra(IntentMessageCodes.RESOURCE_VERSION_URL_MESSAGE);
        hapiFhirHandler = (HapiFhirHandler) getIntent().getSerializableExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE);

        loadPatientData(patientVersionUrl);

        Button buttonOk = findViewById(R.id.buttonPatientVersionOk);
        buttonOk.setOnClickListener(v -> finish());
    }

    private void loadPatientData(String patientVersionUrl) {
        new Thread(() -> {
            Patient patient = (Patient) hapiFhirHandler.getResourceByUrl(patientVersionUrl, Patient.class);
            runOnUiThread(() -> setPatientData(patient));
        }).start();
    }

    private void setPatientData(Patient patient) {
        identifier.setText(patient.getIdElement().getIdPart());
        name.setText(patient.getName().get(0).getNameAsSingleString()
                .replaceAll("\\d", ""));
        gender.setText(patient.getGender().toString());
        birthDate.setText(patient.getBirthDateElement().asStringValue());
        identifier.setText(patient.getIdElement().getIdPart());
        contact.setText(String.format("%s: %s", patient.getTelecomFirstRep().getSystem().toCode(), patient.getTelecomFirstRep().getValue()));
        address.setText(patient.getAddressFirstRep().getLine().toString().replace("[", "").replace("]", ""));
        city.setText(patient.getAddressFirstRep().getCity());
        country.setText(patient.getAddressFirstRep().getCountry());
    }
}