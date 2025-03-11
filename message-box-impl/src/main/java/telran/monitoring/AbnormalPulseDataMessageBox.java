package telran.monitoring;

import java.util.HashMap;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.AbnormalPulseData;

public class AbnormalPulseDataMessageBox extends MessageBoxDynamoDB<AbnormalPulseData>{
    public AbnormalPulseDataMessageBox(String messageBox) {
        super(messageBox);
    }

    @Override
    public HashMap<String, AttributeValue> getMap(AbnormalPulseData abnormalPulseData) {
        return new HashMap<>() {
            {
                put("patientId", AttributeValue.builder().n(abnormalPulseData.patientId() + "").build());
                put("value", AttributeValue.builder().n(abnormalPulseData.value() + "").build());
                put("min", AttributeValue.builder().n(abnormalPulseData.range().min() + "").build());
                put("max", AttributeValue.builder().n(abnormalPulseData.range().max() + "").build());
                put("timestamp", AttributeValue.builder().n(abnormalPulseData.timestamp() + "").build());
            }
        };
    }
}
