package telran.monitoring;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import telran.monitoring.api.PulseRange;

public class RangeProviderClientHttpImpl implements RangeProviderClient {
    String baseUrl = getBaseUrl();
    HttpClient client = HttpClient.newHttpClient();

    @Override
    public PulseRange getRange(long patientId) {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(baseUrl + "?id=%d".formatted(patientId)))
            .build();
        try {
            HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() > 399) {
                throw new Exception(response.body());
            } 
            PulseRange res = PulseRange.of(response.body());
            return res;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }

    private String getBaseUrl() {
        String baseUrl = System.getenv("RANGE_PROVIDER_URL");
        if (baseUrl == null) {
            throw new RuntimeException("env RANGE_PROVIDER_URL must be defined");
        }
        return baseUrl;
    }
}
