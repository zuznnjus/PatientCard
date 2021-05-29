package com.example.patientcard.domain.webservice;

import android.content.Context;
import android.util.Log;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class RestClient {
    private static final String TAG = "REST CLIENT";
    private static final String BASE_URL = "http://192.168.1.10:8080/baseR4";
    private static IGenericClient iGenericClient;
    private static FhirContext fhirContext;

    public static FhirContext getFhirContext() {
        if (fhirContext == null) {
            fhirContext = FhirContext.forR4();
            Map<String, String> proxy = getProxyDetails();
            if (proxy.containsKey("server") && proxy.containsKey("port")) {
                fhirContext.getRestfulClientFactory()
                        .setProxy(proxy.get("server"), Integer.parseInt(proxy.get("port")));
            }
        }
        return fhirContext;
    }

    public static IGenericClient getGenericClient() {
        if (fhirContext == null) {
            fhirContext = getFhirContext();
        }
        if (iGenericClient == null) {
            iGenericClient = fhirContext.newRestfulGenericClient(BASE_URL);
        }
        return iGenericClient;
    }

    private static Map<String, String> getProxyDetails() {
        try {
            return ImmutableMap.<String, String>builder()
                    .put("server", System.getProperty("http.proxyHost"))
                    .put("port", System.getProperty("http.proxyPort"))
                    .build();
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return Collections.emptyMap();
    }
}
