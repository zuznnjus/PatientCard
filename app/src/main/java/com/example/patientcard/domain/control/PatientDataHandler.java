package com.example.patientcard.domain.control;

import com.example.patientcard.domain.utils.GraphPoint;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Resource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class PatientDataHandler {

    private final Patient patient;
    private final HapiFhirHandler hapiFhirHandler;
    private List<Resource> patientResources = new ArrayList<>();

    public PatientDataHandler(Patient patient, HapiFhirHandler hapiFhirHandler) {
        this.patient = patient;
        this.hapiFhirHandler = hapiFhirHandler;
    }

    public Patient getPatient() {
        return patient;
    }

    public HapiFhirHandler getHapiFhirHandler() {
        return hapiFhirHandler;
    }

    public List<Resource> getPatientResources() {
        return patientResources;
    }

    public void loadPatientResources() {
        patientResources = hapiFhirHandler.getObservations(patient);
        patientResources.addAll(hapiFhirHandler.getMedicationRequests(patient));
        patientResources.sort(Comparator.comparing(this::getResourceSortingKey));
    }

    public List<Resource> getFilteredResources(String begin, String end, boolean getObservation, boolean getMedication) {
        if (StringUtils.isBlank(begin) && StringUtils.isBlank(end)) {
            return patientResources.stream()
                    .filter(resource -> isOfMatchingType(resource, getObservation, getMedication))
                    .collect(Collectors.toList());
        }

        LocalDate beginDate;
        LocalDate endDate;

        if (StringUtils.isNotBlank(begin)) {
            beginDate = parseStringToDate(begin);
        } else {
            beginDate = LocalDate.of(1900, 1, 1);
        }
        if (StringUtils.isNotBlank(end)) {
            endDate = parseStringToDate(end);
        } else {
            endDate = LocalDate.now();
        }

        return patientResources.stream()
                .filter(resource -> isOfMatchingType(resource, getObservation, getMedication))
                .filter(resource -> isBetweenGivenDates(resource, beginDate, endDate))
                .collect(Collectors.toList());
    }

    public List<GraphPoint> getGraphData(String observationCode) {
        return getObservationsOfCode(observationCode).stream()
                .map(this::getGraphPoint)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

    }

    private Date getResourceSortingKey(Resource resource) {
        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;
            return observation.getIssued();
        } else if (resource instanceof MedicationRequest) {
            MedicationRequest medicationRequest = (MedicationRequest) resource;
            return medicationRequest.getAuthoredOn();
        }
        return Calendar.getInstance().getTime();
    }

    private LocalDate parseStringToDate(String date) {
        String[] split = date.split("-");
        return LocalDate.of(Integer.parseInt(split[2]), Integer.parseInt(split[1]), Integer.parseInt(split[0]));
    }

    private boolean isOfMatchingType(Resource resource, boolean getObservation, boolean getMedication) {
        return (resource instanceof Observation && getObservation)
                || (resource instanceof MedicationRequest && getMedication);
    }

    private boolean isBetweenGivenDates(Resource resource, LocalDate beginDate, LocalDate endDate) {
        LocalDate convertedDate = LocalDate.now();

        if (resource instanceof Observation) {
            Observation observation = (Observation) resource;
            convertedDate = observation.getIssued()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (resource instanceof MedicationRequest) {
            MedicationRequest medicationRequest = (MedicationRequest) resource;
            convertedDate = medicationRequest.getAuthoredOn()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        return convertedDate.isAfter(beginDate) && convertedDate.isBefore(endDate);
    }

    private List<Observation> getObservationsOfCode(String code) {
        return patientResources.stream()
                .filter(Observation.class::isInstance)
                .map(Observation.class::cast)
                .filter(observation -> isMatchingCode(code, observation))
                .collect(Collectors.toList());
    }

    private boolean isMatchingCode(String code, Observation observation) {
        return Optional.ofNullable(observation.getCode().getCodingFirstRep().getDisplay())
                .filter(code::equals)
                .isPresent();
    }

    private Optional<GraphPoint> getGraphPoint(Observation observation) {
        Date date = observation.getIssued();
        BigDecimal value;
        Optional<Quantity> quantity = Optional.ofNullable(observation.getValueQuantity());
        if (quantity.isPresent()) {
            value = quantity.get().getValue();
            return Optional.of(new GraphPoint(date, value));
        }
        return Optional.empty();
    }
}
