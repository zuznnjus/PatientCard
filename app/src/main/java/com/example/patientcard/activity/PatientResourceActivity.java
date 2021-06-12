package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.patientcard.R;
import com.example.patientcard.domain.control.HapiFhirHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;
import com.google.android.material.textfield.TextInputEditText;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Resource;

import java.text.SimpleDateFormat;
import java.util.Collections;
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
    private TextInputEditText textInputNote;
    private Button buttonUpdate;

    private String versionedNote;

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
        textInputNote = findViewById(R.id.textInputNote);
        buttonUpdate = findViewById(R.id.buttonUpdatePatientResource);
        Button buttonHistory = findViewById(R.id.buttonShowResourceHistory);

        Intent intent = getIntent();
        Optional<String> observationId = Optional.ofNullable(intent.getStringExtra(IntentMessageCodes.OBSERVATION_ID_MESSAGE));
        Optional<String> medicationId = Optional.ofNullable(intent.getStringExtra(IntentMessageCodes.MEDICATION_ID_MESSAGE));
        hapiFhirHandler = (HapiFhirHandler) intent.getSerializableExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE);

        observationId.ifPresent(id -> getResourceThread(id, Observation.class).start());
        medicationId.ifPresent(id -> getResourceThread(id, MedicationRequest.class).start());

        textInputNote.addTextChangedListener(createTextListener());
        buttonUpdate.setOnClickListener(v -> updateResource());
        buttonHistory.setOnClickListener(v -> showResourceHistory());
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
            versionedNote = observation.getNoteFirstRep().getText();
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
            versionedNote = medicationRequest.getNoteFirstRep().getText();
        }
        if (versionedNote == null) {
            versionedNote = StringUtils.EMPTY;
        }

        setTexts(id, type, code, date, value);
    }

    private void setTexts(String id, String type, String code, String date, String value) {
        textViewId.setText(id);
        textViewType.setText(type);
        textViewCode.setText(code);
        textViewDate.setText(date);
        textViewValue.setText(value);
        textInputNote.setText(versionedNote);
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
                if (isDataChanged()) {
                    buttonUpdate.setVisibility(View.VISIBLE);
                } else {
                    buttonUpdate.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    private boolean isDataChanged() {
        return !textInputNote.getText().toString().equals(versionedNote);
    }

    private void updateResource() {
        Annotation annotation = new Annotation();
        String updatedNote = textInputNote.getText().toString();
        annotation.setText(updatedNote);

        versionedNote = updatedNote;

        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;
            observation.setNote(Collections.singletonList(annotation));
            updateResource(observation);
        } else if (resource instanceof MedicationRequest) {
            MedicationRequest medicationRequest = (MedicationRequest) resource;
            medicationRequest.setNote(Collections.singletonList(annotation));
            updateResource(medicationRequest);
        }
    }

    private void updateResource(Resource resource) {
        new Thread(() -> {
            hapiFhirHandler.updateResource(resource);
            runOnUiThread(() -> Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show());
        }).start();
    }

    private void showResourceHistory() {
        Intent intent = new Intent(this, ResourceHistoryActivity.class);
        String historyUrl = StringUtils.substringBeforeLast(resource.getId(), "/");
        intent.putExtra(IntentMessageCodes.RESOURCE_HISTORY_URL_MESSAGE, historyUrl);
        intent.putExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE, hapiFhirHandler);
        startActivity(intent);
    }
}