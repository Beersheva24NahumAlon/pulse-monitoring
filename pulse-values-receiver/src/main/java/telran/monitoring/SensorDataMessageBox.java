package telran.monitoring;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.SensorData;
import telran.monitoring.messagebox.MessageBoxDynamoDB;

public class SensorDataMessageBox extends MessageBoxDynamoDB {

    @Override
    public HashMap<String, AttributeValue> getMap(Object object) {
        SensorData sensorData = (SensorData) object;
        return new HashMap<>() {
            {
                put("patientId", AttributeValue.builder().n(sensorData.patientId() + "").build());
                put("value", AttributeValue.builder().n(sensorData.value() + "").build());
                put("timestamp", AttributeValue.builder().n(sensorData.timestamp() + "").build());
            }
        };
    }

    @Override
    public String getMessageBox() {
        return "pulse_values";
    }
}
