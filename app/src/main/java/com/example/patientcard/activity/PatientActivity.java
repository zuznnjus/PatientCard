package com.example.patientcard.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.patientcard.R;
import com.example.patientcard.adapters.PatientPagerAdapter;
import com.example.patientcard.domain.control.HapiFhirHandler;
import com.example.patientcard.domain.control.PatientDataHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.hl7.fhir.r4.model.Patient;

public class PatientActivity extends AppCompatActivity {

    private static final String[] TAB_NAMES = new String[] {"PATIENT", "OBSERVATIONS / MEDICATION REQUESTS"};
    private HapiFhirHandler hapiFhirHandler;
    private PatientDataHandler patientDataHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Intent intent = getIntent();
        String patientId = intent.getStringExtra(IntentMessageCodes.PATIENT_ID_MESSAGE);
        hapiFhirHandler = (HapiFhirHandler) intent.getSerializableExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE);

        init(patientId);
    }

    private void init(String patientId) {
        new Thread(() -> {
            Patient patient = hapiFhirHandler.getPatientById(patientId);
            patientDataHandler = new PatientDataHandler(patient, hapiFhirHandler);
            patientDataHandler.loadPatientResources();
            runOnUiThread(() -> {
                TabLayout tabLayout = findViewById(R.id.tabLayout);
                ViewPager2 viewPager = findViewById(R.id.viewPager);
                PatientPagerAdapter adapter = new PatientPagerAdapter(this, patientDataHandler);
                viewPager.setAdapter(adapter);
                new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(TAB_NAMES[position])).attach();
            });
        }).start();
    }
}