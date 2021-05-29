package com.example.patientcard.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;

import com.example.patientcard.R;
import com.example.patientcard.adapters.PatientListAdapter;
import com.example.patientcard.domain.webservice.RestClient;

import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, AdapterView.OnItemClickListener {

    PatientListAdapter patientListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SearchView patientSearchView = findViewById(R.id.searchViewPatientName);
        patientSearchView.setOnQueryTextListener(this);

        RecyclerView recyclerViewPatientList = findViewById(R.id.recyclerViewPatientList);
        recyclerViewPatientList.setLayoutManager(new LinearLayoutManager(this));
        patientListAdapter = new PatientListAdapter(this, new ArrayList<>());
        recyclerViewPatientList.setAdapter(patientListAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //TODO
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.length() > 0) {
            createSearchPatientsThread(newText).start();
        }
        return false;
    }

    private Thread createSearchPatientsThread(String searchName) {
        return new Thread(() -> {
            org.hl7.fhir.r4.model.Bundle results = getSearchResult(searchName);
            runOnUiThread(() -> patientListAdapter.updateData(results.getEntry()));
        });
    }

    private org.hl7.fhir.r4.model.Bundle getSearchResult(String searchName) {
        return (org.hl7.fhir.r4.model.Bundle) RestClient
                .getGenericClient()
                .search()
                .forResource(Patient.class)
                .count(100)
                .where(Patient.FAMILY.matches().value(searchName))
                .encodedJson()
                .execute();
    }
}