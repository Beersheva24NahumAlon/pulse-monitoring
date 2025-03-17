package telran.monitoring;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.monitoring.api.AbnormalPulseData;
import telran.monitoring.api.PulseRange;
import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import telran.monitoring.messagebox.MessageBox;

public class AbnormalValuesRecognizer {
    private static final String DEFAULT_MESSAGE_BOX_CLASS = "telran.monitoring.AbnormalPulseDataMessageBox";
    private static final String DEFAULT_MESSAGE_BOX = "abnormal-pulse-values";
    private static final String DEFAULT_RANGE_PROVIDER_CLASS = "telran.monitoring.RangeProviderClientMapImpl";
    
    Logger logger = new LoggerStandard("abnormal-values-recognizer");
    Map<String, String> env = System.getenv();
    RangeProviderClient rangeProviderClient = getRangeProviderClient(getRangeProviderClientClass());

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(r -> {
            SensorData sensorData = getSensorData(r);
            long patientId = sensorData.patientId();
            PulseRange range;
            try {
                range = rangeProviderClient.getRange(patientId);
                logger.log("finest", "data for computing: %s".formatted(sensorData.toString()));
                computeSensorData(sensorData, range);
            } catch (Exception e) {
                logger.log("error", "error - " + e.toString());
            }
        });
    }

    private void computeSensorData(SensorData sensorData, PulseRange range) {
        long patientId = sensorData.patientId();
        int value = sensorData.value();
        int max = range.max();
        int min = range.min();
        if (value < min || value > max) {
            logger.log("fine", "abnormal pulse value (%d) recognized for patient number %d (range: [%d - %d])"
                    .formatted(value, patientId, min, max));
            AbnormalPulseData abnormalPulseData = new AbnormalPulseData(patientId, value, range, sensorData.timestamp());
            logger.log("fine", "abnormal pulse data to save: %s".formatted(abnormalPulseData.toString()));
            saveAbnormalPulseData(abnormalPulseData);
        } else {
            logger.log("finest", "value of pulse (%d) is in range [%d - %d] for patient number %d"
                    .formatted(value, min, max, patientId));
        }
    }

    @SuppressWarnings("unchecked")
    private void saveAbnormalPulseData(AbnormalPulseData abnormalPulseData) {
        BasicConfigurator.configure();
        try {
            MessageBox<AbnormalPulseData> messageBox = MessageBoxFactory.getMessageBox(getMessageBoxClass(),
                    getMessageBox());
            messageBox.put(abnormalPulseData);
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

    private String getRangeProviderClientClass() {
        return env.getOrDefault("RANGE_PROVIDER_CLASS", DEFAULT_RANGE_PROVIDER_CLASS);
    }

    private RangeProviderClient getRangeProviderClient(String className) {
        RangeProviderClient res = new RangeProviderClientMapImpl();
        try {
            res = (RangeProviderClient) Class.forName(className).getConstructor().newInstance();
            logger.log("config", "created object of class %s".formatted(className));
        } catch (Exception e) {
            logger.log("warning", "class %s has not found, created object of class by default (%s)"
                    .formatted(className, DEFAULT_RANGE_PROVIDER_CLASS));
        }
        return res;
    }
}
