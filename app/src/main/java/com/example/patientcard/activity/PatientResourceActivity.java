package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.patientcard.R;
import com.example.patientcard.domain.control.HapiFhirHandler;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Resource;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Optional;

public class PatientResourceActivity extends AppCompatActivity {

    private static final String OBSERVATION_TYPE = "Observation";
    private static final String MEDICATION_TYPE = "Medication request";
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private TextView textViewId;
    private TextView textViewType;
    private TextView textViewCode;
    private TextView textViewDate;
    private TextView textViewValue;

    private HapiFhirHandler hapiFhirHandler;
    private Resource resource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_resource);

        textViewId = findViewById(R.id.textViewResourceId);
        textViewType = findViewById(R.id.textViewResourceType);
        textViewCode = findViewById(R.id.textViewResourceCode);
        textViewDate = findViewById(R.id.textViewResourceDate);
        textViewValue = findViewById(R.id.textViewResourceValueQuantity);

        Intent intent = getIntent();
        Optional<String> observationId = Optional.ofNullable(intent.getStringExtra(PatientActivity.OBSERVATION_ID_MESSAGE));
        Optional<String> medicationId = Optional.ofNullable(intent.getStringExtra(PatientActivity.MEDICATION_ID_MESSAGE));
        hapiFhirHandler = (HapiFhirHandler) intent.getSerializableExtra(MainActivity.HAPI_FHIR_HANDLER_MESSAGE);

        observationId.ifPresent(id -> getResourceThread(id, Observation.class).start());
        medicationId.ifPresent(id -> getResourceThread(id, MedicationRequest.class).start());
    }

    private Thread getResourceThread(String resourceId, Class<? extends Resource> resourceType) {
        return new Thread(() -> {
            resource = hapiFhirHandler.getResourceById(resourceId, resourceType);
            runOnUiThread(() -> setTexts(resource));
        });
    }

    private void setTexts(Resource resource) {
        String id = resource.getIdElement().getIdPart();
        String type = StringUtils.EMPTY;
        String code = StringUtils.EMPTY;
        String date = StringUtils.EMPTY;
        String value = StringUtils.EMPTY;
        textViewId.setText(resource.getIdElement().getIdPart());
        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;
            type = OBSERVATION_TYPE;
            code = observation.getCode().getCodingFirstRep().getDisplay();
            date = DATE_FORMATTER.format(observation.getIssued());
            try {
                Quantity quantity = observation.getValueQuantity();
                value = String.format(Locale.getDefault(), "%.2f %s", quantity.getValue(), quantity.getUnit());
            } catch (Exception ignored) {
            }
        } else if (resource instanceof MedicationRequest) {
            MedicationRequest medicationRequest = (MedicationRequest) resource;
            type = MEDICATION_TYPE;
            code = medicationRequest.getMedicationCodeableConcept().getCodingFirstRep().getDisplay();
            date = DATE_FORMATTER.format(medicationRequest.getAuthoredOn());
        }

        setTexts(id, type, code, date, value);
    }

    private void setTexts(String id, String type, String code, String date, String value) {
        textViewId.setText(id);
        textViewType.setText(type);
        textViewCode.setText(code);
        textViewDate.setText(date);
        textViewValue.setText(value);
    }
}