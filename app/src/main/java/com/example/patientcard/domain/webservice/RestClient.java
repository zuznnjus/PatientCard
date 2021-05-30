package com.example.patientcard.domain.webservice;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;

public class RestClient {
    private static final String TAG = "REST CLIENT";
    public static final String BASE_URL = "http://localhost:8080/baseR4";
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
            Map<String, String> proxyParams = new HashMap<>();
            Optional<String> server = Optional.ofNullable(System.getProperty("http.proxyHost"));
            Optional<String> port = Optional.ofNullable(System.getProperty("http.proxyPort"));
            server.ifPresent(serverValue -> proxyParams.put("server", serverValue));
            port.ifPresent(portValue -> proxyParams.put("port", portValue));
            return proxyParams;
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return Collections.emptyMap();
    }
}
