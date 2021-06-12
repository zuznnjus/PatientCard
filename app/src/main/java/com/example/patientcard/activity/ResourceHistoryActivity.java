package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.patientcard.R;
import com.example.patientcard.adapters.ResourceHistoryAdapter;
import com.example.patientcard.domain.control.HapiFhirHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import java.util.ArrayList;
import java.util.List;

public class ResourceHistoryActivity extends AppCompatActivity implements ResourceHistoryAdapter.ItemClickListener {

    private static final String PATIENT = "Patient";
    private static final String OBSERVATION = "Observation";
    private static final String MEDICATION_REQUEST = "MedicationRequest";

    private HapiFhirHandler hapiFhirHandler;
    private ResourceHistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resource_history);

        String resourceHistoryUrl = getIntent().getStringExtra(IntentMessageCodes.RESOURCE_HISTORY_URL_MESSAGE);
        hapiFhirHandler = (HapiFhirHandler) getIntent().getSerializableExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE);

        setResourceHistoryAdapter(resourceHistoryUrl);

        Button buttonOk = findViewById(R.id.buttonHistoryOk);
        buttonOk.setOnClickListener(v -> finish());
    }

    private void setResourceHistoryAdapter(String resourceHistoryUrl) {
        RecyclerView recyclerViewResourceHistoryList = findViewById(R.id.recyclerViewResourceHistoryList);
        recyclerViewResourceHistoryList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ResourceHistoryAdapter(this, new ArrayList<>());
        adapter.setClickListener(this);
        recyclerViewResourceHistoryList.setAdapter(adapter);
        getResourceHistory(resourceHistoryUrl);
    }

    @Override
    public void onItemClick(View view, int position) {
        Resource resource = adapter.getResourceAtPosition(position);
        if (resource instanceof Patient) {
            Intent intent = new Intent(this, PatientVersionActivity.class);
            intent.putExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE, hapiFhirHandler);
            intent.putExtra(IntentMessageCodes.RESOURCE_VERSION_URL_MESSAGE, resource.getId());
            startActivity(intent);
        } else if (resource instanceof Observation || resource instanceof MedicationRequest) {
            Intent intent = new Intent(this, ResourceVersionActivity.class);
            intent.putExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE, hapiFhirHandler);
            intent.putExtra(IntentMessageCodes.RESOURCE_VERSION_URL_MESSAGE, resource.getId());
            startActivity(intent);
        }
    }

    private void getResourceHistory(String resourceHistoryUrl) {
        new Thread(() -> {
            List<Resource> resourceHistoryList = new ArrayList<>();
            if (resourceHistoryUrl.contains(PATIENT)) {
                resourceHistoryList.addAll(hapiFhirHandler.getResourceHistory(resourceHistoryUrl, Patient.class));
            } else if (resourceHistoryUrl.contains(OBSERVATION)) {
                resourceHistoryList.addAll(hapiFhirHandler.getResourceHistory(resourceHistoryUrl, Observation.class));
            } else if (resourceHistoryUrl.contains(MEDICATION_REQUEST)) {
                resourceHistoryList.addAll(hapiFhirHandler.getResourceHistory(resourceHistoryUrl, MedicationRequest.class));
            }
            runOnUiThread(() -> adapter.updateData(resourceHistoryList));
        }).start();
    }
}