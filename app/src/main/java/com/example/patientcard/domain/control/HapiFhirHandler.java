package com.example.patientcard.domain.control;

import com.example.patientcard.domain.webservice.RestClient;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class HapiFhirHandler implements Serializable {

    public List<Patient> getAllPatients() {
        Bundle bundle = RestClient.getGenericClient()
                .search()
                .forResource(Patient.class)
                .encodedJson()
                .returnBundle(Bundle.class)
                .execute();
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient.class::isInstance)
                .map(Patient.class::cast)
                .sorted(Comparator.comparing(patient -> patient.getName().get(0).getFamily()))
                .collect(Collectors.toList());
    }

    public List<Patient> getSearchedPatients(String searchName) {
        return RestClient.getGenericClient()
                .search()
                .forResource(Patient.class)
                .count(100)
                .where(Patient.FAMILY.matches().value(searchName))
                .encodedJson()
                .returnBundle(Bundle.class)
                .execute()
                .getEntry()
                .stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient.class::isInstance)
                .map(Patient.class::cast)
                .sorted(Comparator.comparing(patient -> patient.getName().get(0).getFamily()))
                .collect(Collectors.toList());
    }

    public Patient getPatientById(String patientId) {
        return RestClient.getGenericClient()
                .read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();
    }

    public List<Resource> getObservations(Patient patient) {
        Bundle bundle = RestClient.getGenericClient()
                .search()
                .forResource(Observation.class)
                .where(Observation.SUBJECT.hasId(patient.getIdElement().getIdPart()))
                .returnBundle(Bundle.class)
                .execute();
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Observation.class::isInstance)
                .map(Observation.class::cast)
                .collect(Collectors.toList());
    }

    public List<Resource> getMedicationRequests(Patient patient) {
        Bundle bundle = RestClient.getGenericClient()
                .search()
                .forResource(MedicationRequest.class)
                .where(MedicationRequest.SUBJECT.hasId(patient.getIdElement().getIdPart()))
                .returnBundle(Bundle.class)
                .execute();
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(MedicationRequest.class::isInstance)
                .map(MedicationRequest.class::cast)
                .collect(Collectors.toList());
    }

    public Resource getResourceById(String resourceId, Class<? extends Resource> resourceType) {
        return RestClient.getGenericClient()
                .read()
                .resource(resourceType)
                .withId(resourceId)
                .execute();
    }

    public Resource getResourceByUrl(String resourceUrl, Class<? extends Resource> type) {
        return RestClient.getGenericClient()
                .read()
                .resource(type)
                .withUrl(resourceUrl)
                .execute();
    }

    public void updateResource(Resource resource) {
        RestClient.getGenericClient()
                .update()
                .resource(resource)
                .execute();
    }

    public List<Resource> getResourceHistory(String url, Class<? extends Resource> type) {
        Bundle bundle = RestClient.getGenericClient()
                .search()
                .byUrl(url)
                .encodedJson()
                .returnBundle(Bundle.class)
                .execute();
        return getPagedEntries(bundle).stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(type::isInstance)
                .map(type::cast)
                .sorted(Comparator.comparing(this::getResourceVersionSortingKey).reversed())
                .collect(Collectors.toList());
    }

    private List<Bundle.BundleEntryComponent> getPagedEntries(Bundle bundle) {
        List<Bundle.BundleEntryComponent> entries = bundle.getEntry();
        while (bundle.getLink(Bundle.LINK_NEXT) != null) {
            bundle = RestClient.getGenericClient()
                    .loadPage()
                    .next(bundle)
                    .execute();
            entries.addAll(bundle.getEntry());
        }
        return entries;
    }

    private String getResourceVersionSortingKey(Resource resource) {
        return resource.getMeta().getVersionId();
    }
}
