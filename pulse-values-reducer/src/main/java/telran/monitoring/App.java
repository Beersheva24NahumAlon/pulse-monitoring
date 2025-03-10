package telran.monitoring;

import java.util.List;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.monitoring.api.LatestValueSaver;
import telran.monitoring.api.ReducePulseData;
import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import telran.monitoring.messagebox.MessageBox;

public class App {
    private static final int DEFAULT_COUNT_REDUCER = 3;
    private static final String DEFAULT_MESSAGE_BOX_CLASS = "telran.monitoring.ReducePulseDataMessageBox";
    private static final String DEFAULT_MESSAGE_BOX = "avg_pulse_values";

    Logger logger = new LoggerStandard("pulse-values-reducer");
    LatestValueSaver lastValues = new LatestValuesSeverMap();
    Map<String, String> env = System.getenv();
    int countReducer = getCountReducer();

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(r -> {
            SensorData sensorData = getSensorData(r);
            logger.log("finest", sensorData.toString());
            computeSensorData(sensorData, countReducer);
        });
    }

    private int getCountReducer() {
        int res = DEFAULT_COUNT_REDUCER;
        String countStr = env.getOrDefault("COUNT_REDUCER", String.valueOf(DEFAULT_COUNT_REDUCER));
        try {
            res = Integer.parseInt(countStr);
            logger.log("fine", "Count of reducer defined as value %d".formatted(res));
        } catch (Exception e) {
            logger.log("warn", "Environment variable COUNT_REDUCER is not interger type, value has set to default (%d)"
                    .formatted(DEFAULT_COUNT_REDUCER));
        }
        return res;
    }

    private void computeSensorData(SensorData sensorData, int countReducer) {
        long patientId = sensorData.patientId();
        int value = sensorData.value();
        List<SensorData> list = lastValues.getAllValues(patientId);
        if (list.size() == countReducer) {
            int avgValue = (int) Math.round(list.stream().mapToInt(e -> e.value()).average().getAsDouble());
            logger.log("fine", "avg for patient %d is %d".formatted(patientId, avgValue));
            ReducePulseData reducePulseData = new ReducePulseData(patientId, avgValue, System.currentTimeMillis());
            saveReducePulseData(reducePulseData);
            lastValues.clearValues(patientId);
            logger.log("fine", "last values for patient %d are cleared".formatted(patientId));
        }
        lastValues.addValue(sensorData);
        logger.log("fine", "last value (%d) for patient %d is added".formatted(value, patientId));
    }

    @SuppressWarnings("unchecked")
    private void saveReducePulseData(ReducePulseData reducePulseData) {
        BasicConfigurator.configure();
        try {
            MessageBox<ReducePulseData> messageBox = MessageBoxFactory.getMessageBox(getMessageBoxClass(),
                    getMessageBox());
            messageBox.put(reducePulseData);
            logger.log("fine", "data to save: %s".formatted(reducePulseData.toString()));
        } catch (Exception e) {
            logger.log("error", e.toString());
        }
    }

    private SensorData getSensorData(DynamodbStreamRecord r) {
        Map<String, AttributeValue> map = r.getDynamodb().getNewImage();
        long patientId = Long.parseLong(map.get("patientId").getN());
        int value = Integer.parseInt(map.get("value").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        SensorData sensorData = new SensorData(patientId, value, timestamp);
        return sensorData;
    }

    private String getMessageBoxClass() {
        return env.getOrDefault("MESSAGE_BOX_CLASS", DEFAULT_MESSAGE_BOX_CLASS);
    }

    private String getMessageBox() {
        return env.getOrDefault("MESSAGE_BOX_NAME", DEFAULT_MESSAGE_BOX);
    }
}
