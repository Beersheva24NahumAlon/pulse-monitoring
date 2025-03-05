package telran.monitoring;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.SensorData;

public class SensorDataMessageBox extends MessageBoxDynamoDB<SensorData> {

    public SensorDataMessageBox(String messageBox) {
        super(messageBox);
    }
    
    @Override
    public HashMap<String, AttributeValue> getMap(SensorData sensorData) {
        return new HashMap<>() {
            {
                put("patientId", AttributeValue.builder().n(sensorData.patientId() + "").build());
                put("value", AttributeValue.builder().n(sensorData.value() + "").build());
                put("timestamp", AttributeValue.builder().n(sensorData.timestamp() + "").build());
            }
        };
    }
}
