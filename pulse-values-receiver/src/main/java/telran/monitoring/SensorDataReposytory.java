package telran.monitoring;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.SensorData;
import telran.monitoring.repository.RepositoryDynamoDB;

public class SensorDataReposytory extends RepositoryDynamoDB {

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
