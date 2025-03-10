package telran.monitoring;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.ReducePulseData;

public class ReducePulseDataMessageBox extends MessageBoxDynamoDB<ReducePulseData> {

    public ReducePulseDataMessageBox(String messageBox) {
        super(messageBox);
    }
    
    @Override
    public HashMap<String, AttributeValue> getMap(ReducePulseData reducePulseData) {
        return new HashMap<>() {
            {
                put("patientId", AttributeValue.builder().n(reducePulseData.patientId() + "").build());
                put("avgValue", AttributeValue.builder().n(reducePulseData.avgValue() + "").build());
                put("timestamp", AttributeValue.builder().n(reducePulseData.timestamp() + "").build());
            }
        };
    }
}
