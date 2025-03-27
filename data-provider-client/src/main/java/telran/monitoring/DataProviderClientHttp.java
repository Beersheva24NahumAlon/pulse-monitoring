package telran.monitoring;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.*;

import telran.monitoring.logging.Logger;

record DataTimestamp(String data, long timestamp) {
}

public class DataProviderClientHttp implements DataProviderClient {
    private static final int DEFAULT_CACHE_CAPACITY = 7;
    private static final int DEFAULT_REFRESH_TIME = 3600 * 24 * 1000;

    Logger logger = loggers[0];
    Map<String, String> env = System.getenv();
    String baseUrl;
    HttpClient httpClient = HttpClient.newHttpClient();
    int cacheCapacity = getCacheCapacity();
    long refreshTime = getRefreshTime();
    LinkedHashMap<Long, DataTimestamp> cache = new LinkedHashMap<>(cacheCapacity + 1, 1f, true) {
        @Override
        protected boolean removeEldestEntry(Entry<Long, DataTimestamp> entry) {
            return size() >= cacheCapacity;
        }
    };

    public DataProviderClientHttp(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private long getRefreshTime() {
        long res = DEFAULT_REFRESH_TIME;
        String refreshTimeStr = env.get("REFRESH_TIME");
        if (refreshTimeStr != null) {
            try {
                res = Long.parseLong(refreshTimeStr);
                logger.log("config", "value of refresh time: %d".formatted(res));
            } catch (NumberFormatException e) {
                logger.log("warnibg", "value of refresh time in env is wrong, set default value (%d)".formatted(res));
            }
        } else {
            logger.log("config", "value of refresh time is not set in env, set default value (%d)".formatted(res));
        }
        return res;
    }

    private int getCacheCapacity() {
        int res = DEFAULT_CACHE_CAPACITY;
        String cacheCapacityStr = env.get("REFRESH_TIME");
        if (cacheCapacityStr != null) {
            try {
                res = Integer.parseInt(cacheCapacityStr);
                logger.log("config", "value of cache capacity: %d".formatted(res));
            } catch (NumberFormatException e) {
                logger.log("warnibg", "value of cache capacity in env is wrong, set default value (%d)".formatted(res));
            }
        } else {
            logger.log("config", "value of cache capacity is not set in env, set default value (%d)".formatted(res));
        }
        return res;

    }

    @Override
    public String getDataForPatient(long patientId) {
        String res = getDataFromCache(patientId);
        if (res == null) {
            res = httpRequest(patientId);
            setDataToCache(res, patientId);
            logger.log("fine", "new value %s for patient %d added to cache"
                    .formatted(res, patientId));
        } else {
            logger.log("fine", "value %s for patient %d found in cache"
                    .formatted(res, patientId));
        }
        return res;
    }

    private void setDataToCache(String res, long patientId) {
        cache.put(patientId, new DataTimestamp(res, System.currentTimeMillis()));
    }

    private String getDataFromCache(long patientId) {
        DataTimestamp dt = cache.get(patientId);
        String res = null;
        if (dt != null && System.currentTimeMillis() - dt.timestamp() < refreshTime) {
            res = dt.data();
        }
        return res;
    }

    private String getURI(long patientId) {
        String uri = baseUrl + "?id=" + patientId;
        logger.log("fine", "URI is " + uri);
        return uri;
    }

    private String httpRequest(long patientId) {
        HttpRequest request = HttpRequest.newBuilder().GET().uri(URI.create(getURI(patientId))).build();
        try {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() > 399) {
                throw new Exception(response.body());
            }
            String result = response.body();
            logger.log("fine", "data received from data provider API service is " + result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
