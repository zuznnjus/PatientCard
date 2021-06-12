package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.example.patientcard.R;
import com.example.patientcard.domain.control.HapiFhirHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Quantity;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ResourceVersionActivity extends AppCompatActivity {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final String OBSERVATION = "Observation";
    private static final String MEDICATION_REQUEST = "MedicationRequest";
    private static final String MEDICATION_TYPE = "Medication request";

    private HapiFhirHandler hapiFhirHandler;

    private TextView textViewId;
    private TextView textViewType;
    private TextView textViewCode;
    private TextView textViewDate;
    private TextView textViewValue;
    private TextView textViewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_version);

        textViewId = findViewById(R.id.textViewResourceVersionId);
        textViewType = findViewById(R.id.textViewResourceVersionType);
        textViewCode = findViewById(R.id.textViewResourceVersionCode);
        textViewDate = findViewById(R.id.textViewResourceVersionDate);
        textViewValue = findViewById(R.id.textViewResourceVersionValueQuantity);
        textViewNote = findViewById(R.id.textViewResourceVersionNote);

        String resourceVersionUrl = getIntent().getStringExtra(IntentMessageCodes.RESOURCE_VERSION_URL_MESSAGE);
        hapiFhirHandler = (HapiFhirHandler) getIntent().getSerializableExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE);

        getResource(resourceVersionUrl);

        Button buttonOk = findViewById(R.id.buttonResourceVersionOk);
        buttonOk.setOnClickListener(v -> finish());
    }

    private void getResource(String resourceVersionUrl) {
        new Thread(() -> {
            if (resourceVersionUrl.contains(OBSERVATION)) {
                Observation observation = (Observation) hapiFhirHandler.getResourceByUrl(resourceVersionUrl, Observation.class);
                runOnUiThread(() -> setTextsForObservation(observation));
            } else if (resourceVersionUrl.contains(MEDICATION_REQUEST)) {
                MedicationRequest medicationRequest = (MedicationRequest) hapiFhirHandler.getResourceByUrl(resourceVersionUrl, MedicationRequest.class);
                runOnUiThread(() -> setTextsForMedicationRequest(medicationRequest));
            }
        }).start();
    }

    private void setTextsForObservation(Observation observation) {
        String id = observation.getIdElement().getIdPart();
        String code = observation.getCode().getCodingFirstRep().getDisplay();
        String date = DATE_FORMATTER.format(observation.getIssued());
        String value = StringUtils.EMPTY;
        String note = observation.getNoteFirstRep().getText();
        try {
            Quantity quantity = observation.getValueQuantity();
            value = String.format(Locale.getDefault(), "%.2f %s", quantity.getValue(), quantity.getUnit());
        } catch (Exception ignored) {
        }

        setTexts(id, OBSERVATION, code, date, value, note);
    }

    private void setTexts(String id, String type, String code, String date, String value, String note) {
        textViewId.setText(id);
        textViewType.setText(type);
        textViewCode.setText(code);
        textViewDate.setText(date);
        textViewValue.setText(value);
        textViewNote.setText(note);
    }

    private void setTextsForMedicationRequest(MedicationRequest medicationRequest) {
        String id = medicationRequest.getIdElement().getIdPart();
        String code = medicationRequest.getMedicationCodeableConcept().getCodingFirstRep().getDisplay();
        String date = DATE_FORMATTER.format(medicationRequest.getAuthoredOn());
        String value = StringUtils.EMPTY;
        String note = medicationRequest.getNoteFirstRep().getText();

        setTexts(id, MEDICATION_TYPE, code, date, value, note);
    }
}