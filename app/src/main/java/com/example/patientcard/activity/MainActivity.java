package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;

import com.example.patientcard.R;
import com.example.patientcard.adapters.PatientListAdapter;
import com.example.patientcard.domain.control.HapiFhirHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, PatientListAdapter.ItemClickListener {

    HapiFhirHandler hapiFhirHandler = new HapiFhirHandler();
    PatientListAdapter patientListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchView patientSearchView = findViewById(R.id.searchViewPatientName);
        patientSearchView.setOnQueryTextListener(this);

        setPatientListAdapter();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, PatientActivity.class);
        intent.putExtra(IntentMessageCodes.PATIENT_ID_MESSAGE, patientListAdapter.getPatientId(position));
        intent.putExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE, hapiFhirHandler);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.length() > 0) {
            createSearchPatientsThread(newText).start();
        } else {
            createAllPatientsThread().start();
        }
        return false;
    }

    private void setPatientListAdapter() {
        RecyclerView recyclerViewPatientList = findViewById(R.id.recyclerViewPatientList);
        recyclerViewPatientList.setLayoutManager(new LinearLayoutManager(this));
        patientListAdapter = new PatientListAdapter(this, new ArrayList<>());
        patientListAdapter.setClickListener(this);
        recyclerViewPatientList.setAdapter(patientListAdapter);
        createAllPatientsThread().start();
    }

    private Thread createAllPatientsThread() {
        return new Thread(() -> {
            List<Patient> results = hapiFhirHandler.getAllPatients();
            runOnUiThread(() -> patientListAdapter.updateData(results));
        });
    }

    private Thread createSearchPatientsThread(String searchName) {
        return new Thread(() -> {
            List<Patient> patients = hapiFhirHandler.getSearchedPatients(searchName);
            runOnUiThread(() -> patientListAdapter.updateData(patients));
        });
    }
}