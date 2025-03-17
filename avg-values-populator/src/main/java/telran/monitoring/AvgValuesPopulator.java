package telran.monitoring;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.monitoring.api.ReducePulseData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;

public class AvgValuesPopulator {

    private static final String DEFAULT_DATA_SOURCE_CLASS = "telran.monitoring.DataSourceTest";

    Logger logger = new LoggerStandard("avg-values-populator");
    Map<String, String> env = System.getenv();
    DataSource dataSource = getDataSourceClass();

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(r -> {
            ReducePulseData avgPulseData = getAvgPulseData(r);
            logger.log("finest", "data for saving: %s".formatted(avgPulseData.toString()));
            saveAvgPulseValueToDB(avgPulseData);
        });
    }

    private DataSource getDataSourceClass() {
        String className = env.getOrDefault("DATA_SOURCE_CLASS", DEFAULT_DATA_SOURCE_CLASS);
        try {
            return (DataSource) Class.forName(className).getConstructor(Logger.class, Map.class).newInstance(logger, env);
        } catch (Exception e) {
            logger.log("error", e.toString());
            throw new RuntimeException(e);
        }
    }

    private void saveAvgPulseValueToDB(ReducePulseData avgPulseData) {
        try {
            logger.log("fine", "saving to db: %s".formatted(avgPulseData.toString()));
            dataSource.put(avgPulseData);
        } catch (Exception e) {
            logger.log("error", e.toString());
        }
    }

    private ReducePulseData getAvgPulseData(DynamodbStreamRecord r) {
        Map<String, AttributeValue> map = r.getDynamodb().getNewImage();
        long patientId = Long.parseLong(map.get("patientId").getN());
        int avgValue = Integer.parseInt(map.get("avgValue").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        ReducePulseData avgPulseData = new ReducePulseData(patientId, avgValue, timestamp);
        return avgPulseData;
    }

}
