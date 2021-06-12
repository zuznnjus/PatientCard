package com.example.patientcard.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.patientcard.R;
import com.example.patientcard.activity.ResourceHistoryActivity;
import com.example.patientcard.domain.control.PatientDataHandler;
import com.example.patientcard.domain.utils.IntentMessageCodes;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PatientDataFragment extends Fragment {

    private final PatientDataHandler patientDataHandler;

    private TextView name;
    private TextView gender;
    private TextView birthDate;
    private TextView identifier;
    private EditText contact;
    private EditText address;
    private EditText city;
    private EditText country;
    private Button buttonUpdate;

    private String versionedContact;
    private String versionedAddress;
    private String versionedCity;
    private String versionedCountry;

    public PatientDataFragment(PatientDataHandler patientDataHandler) {
        this.patientDataHandler = patientDataHandler;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_patient_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = view.findViewById(R.id.textViewPatientName);
        gender = view.findViewById(R.id.textViewGender);
        birthDate = view.findViewById(R.id.textViewBirthDate);
        identifier = view.findViewById(R.id.textViewIdentifier);
        contact = view.findViewById(R.id.editTextContact);
        address = view.findViewById(R.id.editTextAddressLine);
        city = view.findViewById(R.id.editTextAddressCity);
        country = view.findViewById(R.id.editTextAddressCountry);
        buttonUpdate = view.findViewById(R.id.buttonUpdatePatient);
        Button buttonHistory = view.findViewById(R.id.buttonShowPatientHistory);

        loadData();

        contact.addTextChangedListener(createTextListener());
        address.addTextChangedListener(createTextListener());
        city.addTextChangedListener(createTextListener());
        country.addTextChangedListener(createTextListener());
        buttonUpdate.setOnClickListener(v -> updatePatientData());
        buttonHistory.setOnClickListener(v -> showPatientHistory());
    }

    private void loadData() {
        Patient patient = patientDataHandler.getPatient();
        name.setText(patient.getName().get(0).getNameAsSingleString()
                .replaceAll("\\d", ""));
        gender.setText(patient.getGender().toString());
        birthDate.setText(patient.getBirthDateElement().asStringValue());
        identifier.setText(patient.getIdElement().getIdPart());

        versionedContact = String.format("%s: %s", patient.getTelecomFirstRep().getSystem().toCode(), patient.getTelecomFirstRep().getValue());
        versionedAddress = patient.getAddressFirstRep().getLine().toString().replace("[", "").replace("]", "");
        versionedCity = patient.getAddressFirstRep().getCity();
        versionedCountry = patient.getAddressFirstRep().getCountry();

        contact.setText(versionedContact);
        address.setText(versionedAddress);
        city.setText(versionedCity);
        country.setText(versionedCountry);
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
        if (!contact.getText().toString().equals(versionedContact)) {
            return true;
        }
        if (!address.getText().toString().equals(versionedAddress)) {
            return true;
        }
        if (!city.getText().toString().equals(versionedCity)) {
            return true;
        }
        return !country.getText().toString().equals(versionedCountry);
    }

    private void updatePatientData() {
        Patient patient = patientDataHandler.getPatient();
        Address newAddress = patient.getAddressFirstRep();
        if (!contact.getText().toString().equals(versionedContact)) {
            String[] contactSplit = contact.getText().toString().split(": ");
            ContactPoint contactPoint = patient.getTelecomFirstRep();
            contactPoint.setSystem(ContactPoint.ContactPointSystem.fromCode(contactSplit[0]));
            contactPoint.setValue(contactSplit[1]);
            patient.setTelecom(Collections.singletonList(contactPoint));
        }
        if (!address.getText().toString().equals(versionedAddress)) {
            List<StringType> addressLine = Arrays.stream(address.getText().toString().split(" "))
                    .map(StringType::new).collect(Collectors.toList());
            newAddress.setLine(addressLine);
        }
        if (!city.getText().toString().equals(versionedCity)) {
            newAddress.setCity(city.getText().toString());
        }
        if (!country.getText().toString().equals(versionedCountry)) {
            newAddress.setCountry(country.getText().toString());
        }
        patient.setAddress(Collections.singletonList(newAddress));
        new Thread(() -> {
            patientDataHandler.getHapiFhirHandler().updateResource(patient);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showPatientHistory() {
        Intent intent = new Intent(getContext(), ResourceHistoryActivity.class);
        String historyUrl = StringUtils.substringBeforeLast(patientDataHandler.getPatient().getId(), "/");
        intent.putExtra(IntentMessageCodes.RESOURCE_HISTORY_URL_MESSAGE, historyUrl);
        intent.putExtra(IntentMessageCodes.HAPI_FHIR_HANDLER_MESSAGE, patientDataHandler.getHapiFhirHandler());
        if (getActivity() != null) {
            getActivity().startActivity(intent);
        }
    }
}
