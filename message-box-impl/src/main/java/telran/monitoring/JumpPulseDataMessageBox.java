package telran.monitoring;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.JumpPulseData;

public class JumpPulseDataMessageBox extends MessageBoxDynamoDB<JumpPulseData> {

    public JumpPulseDataMessageBox(String messageBox) {
        super(messageBox);
    }
    
    @Override
    public HashMap<String, AttributeValue> getMap(JumpPulseData jumpPulseData) {
        return new HashMap<>() {
            {
                put("patientId", AttributeValue.builder().n(jumpPulseData.patientId() + "").build());
                put("oldValue", AttributeValue.builder().n(jumpPulseData.oldValue() + "").build());
                put("newValue", AttributeValue.builder().n(jumpPulseData.newValue() + "").build());
                put("timestamp", AttributeValue.builder().n(jumpPulseData.timestamp() + "").build());
            }
        };
    }
}
