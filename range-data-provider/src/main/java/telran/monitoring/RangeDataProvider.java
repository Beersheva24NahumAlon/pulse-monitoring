package telran.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import telran.monitoring.api.PulseRange;

public class RangeDataProvider implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final String DEFAULT_DATA_SOURCE_CLASS = "telran.monitoring.DataSourceMap";

    Logger logger = new LoggerStandard("email-data-provider");
    Map<String, String> env = System.getenv();
    DataSource dataSource = getDataSourceObject();

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
            logger.log("fine", "response code: 200, response body: %s".formatted(responseBody));
            return response
                    .withStatusCode(200)
                    .withBody(responseBody);
        } catch (NoSuchElementException e) {
            logger.log("error", "response code: 404, response body: %s".formatted(e.toString()));
            return response
                    .withBody(e.toString())
                    .withStatusCode(404);
        } catch (IllegalArgumentException e) {
            logger.log("error", "response code: 400, response body: %s".formatted(e.toString()));
            return response
                    .withBody(e.toString())
                    .withStatusCode(400);
        } catch (Exception e) {
            logger.log("error", "response code: 500, response body: %s".formatted(e.toString()));
            return response
                    .withBody(e.toString())
                    .withStatusCode(500);
        }
    }

    private DataSource getDataSourceObject() {
        String className = env.getOrDefault("DATA_SOURCE_CLASS", DEFAULT_DATA_SOURCE_CLASS);
        logger.log("config", "Data source class: %s".formatted(className));
        try {
            return (DataSource) Class.forName(className)
            .getConstructor(Logger.class, Map.class).newInstance(logger, env);
        } catch (Exception e) {
            logger.log("error", e.toString());
            throw new RuntimeException(e);
        }
    }
}
