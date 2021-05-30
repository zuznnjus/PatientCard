package com.example.patientcard.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

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
    private TextView name;
    private TextView gender;
    private TextView birthDate;
    private TextView identifier;

    private Patient patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // trochę duży ten onCreate, ale na pewno jakoś ładnie go zrobisz
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);

        Intent intent = getIntent();
        String patientId = intent.getStringExtra(MainActivity.PATIENT_ID_MESSAGE);

        name = findViewById(R.id.textViewPatientName);
        gender = findViewById(R.id.textViewGender);
        birthDate = findViewById(R.id.textViewBirthDate);
        identifier = findViewById(R.id.textViewIdentifier);

        EditText editStartDate = findViewById(R.id.editTextStartDate);
        EditText editEndDate = findViewById(R.id.editTextEndDate);
        editEndDate.setText(dateFormat.format(calendar.getTime()));
        calendar.add(Calendar.YEAR, -1); // tak, jestem bardzo dumna z tego XDD
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
            runOnUiThread(() -> {
                name.setText(patient.getName().get(0).getNameAsSingleString()
                        .replaceAll("\\d", ""));
                gender.setText(patient.getGender().toString());
                birthDate.setText(patient.getBirthDateElement().asStringValue());
                identifier.setText(patient.getIdElement().getIdPart());
            });
        });
    }

    private Patient getPatient(String patientId) {
        return (Patient) RestClient
                .getGenericClient()
                .read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();
    }
}