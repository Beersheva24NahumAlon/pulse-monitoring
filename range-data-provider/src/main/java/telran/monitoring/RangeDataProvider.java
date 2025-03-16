package telran.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import telran.monitoring.logging.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import telran.monitoring.api.PulseRange;

public class RangeDataProvider implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String DEFAULT_DB_USERNAME = "postgres";
    private static final String DB_HOST = "patient-db.c8ri40yyunrd.us-east-1.rds.amazonaws.com";
    private static final String DB_PORT = "5432";
    private static final String DB_NAME = "patients_db";
    private static final String DEFAULT_DB_CONNECTION_STRING = "jdbc:postgresql://%s:%s/%s".formatted(DB_HOST, DB_PORT, DB_NAME);

    Logger logger;
    Map<String, String> env = System.getenv();
    String username = getUserName();
    String password = getPassword();
    String connectionString = getConnectionString();
    DataSource dataSource = new DataSource(connectionString, username, password, logger);

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        Map<String, String> path = input.getQueryStringParameters();

        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
                .withHeaders(headers);
        try {
            String patientIdStr = "";
            if (path == null || (patientIdStr = path.get("id")) == null) {
                throw new IllegalArgumentException("id parameter must exist");
            }
            long patientId = 0;
            try {
                patientId = Long.parseLong(patientIdStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("patient id must be a number");
            }
            PulseRange range = dataSource.getRange(patientId);
            String responseBody = "{ \"min\": %d, \"max\": %d }".formatted(range.min(), range.max());
            return response
                    .withStatusCode(200)
                    .withBody(responseBody);
        } catch (NoSuchElementException e) {
            return response
                    .withBody(e.toString())
                    .withStatusCode(404);
        } catch (IllegalArgumentException e) {
            return response
                    .withBody(e.toString())
                    .withStatusCode(400);
        } catch (Exception e) {
            return response
                    .withBody(e.toString())
                    .withStatusCode(500);
        }
    }

    private String getConnectionString() {
        String res = env.getOrDefault("DB_CONNETION_STRING", DEFAULT_DB_CONNECTION_STRING);
        return res;
    }

    private String getPassword() {
        String res = env.get("DB_PASSWORD");
        if (res == null) {
            throw new RuntimeException("password must be specified in env variable");
        }
        return res;
    }

    private String getUserName() {
        String res = env.getOrDefault("DB_USERNAME", DEFAULT_DB_USERNAME);
        return res;
    }


}
