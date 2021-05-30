package com.example.patientcard.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.patientcard.R;
import com.example.patientcard.domain.webservice.RestClient;

import org.hl7.fhir.r4.model.Patient;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PatientActivity extends AppCompatActivity {
    private final Calendar calendar = Calendar.getInstance();
    private final String format = "dd/MM/yyyy";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
    private EditText clickedDate;
    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Intent intent = getIntent();
        String patientId = intent.getStringExtra(MainActivity.PATIENT_ID_MESSAGE);

        EditText editStartDate = findViewById(R.id.editTextStartDate);
        EditText editEndDate = findViewById(R.id.editTextEndDate);
        editEndDate.setText(dateFormat.format(calendar.getTime()));
        calendar.add(Calendar.YEAR, -1);
        editStartDate.setText(dateFormat.format(calendar.getTime()));

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate(clickedDate);
        };

        editStartDate.setOnClickListener(view -> {
            clickedDate = (EditText) view;
            new DatePickerDialog(PatientActivity.this, date, calendar
                    .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        editEndDate.setOnClickListener(view -> {
            clickedDate = (EditText) view;
            new DatePickerDialog(PatientActivity.this,
                    date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        createGetPatientThread(patientId).start();
    }

    private void updateDate(EditText editText) {
        editText.setText(dateFormat.format(calendar.getTime()));
    }

    private Thread createGetPatientThread(String patientId) {
        return new Thread(() -> {
            patient = getPatient(patientId);
        });
    }

    private Patient getPatient(String patientId) {
        return (Patient) RestClient
                .getGenericClient()
                .read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();
//        return (Patient) RestClient
//                .getGenericClient()
//                .read()
//                .resource(Patient.class)
//                .withUrl("/Patient?identifier=" + patientId + "/$everything")
//                .execute();
    }
}