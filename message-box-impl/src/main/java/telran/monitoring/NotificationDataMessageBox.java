package telran.monitoring;

import java.util.HashMap;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import telran.monitoring.api.NotificationData;

public class NotificationDataMessageBox extends MessageBoxDynamoDB<NotificationData>{
    public NotificationDataMessageBox(String messageBox) {
        super(messageBox);
    }

    @Override
    public HashMap<String, AttributeValue> getMap(NotificationData notificationData) {
        return new HashMap<>() {
            {
                put("patientId", AttributeValue.builder().n(notificationData.patientId() + "").build());
                put("email", AttributeValue.builder().s(notificationData.email()).build());
                put("notificationText", AttributeValue.builder().s(notificationData.notificationText()).build());
                put("timestamp", AttributeValue.builder().n(notificationData.timestamp() + "").build());
            }
        };
    }
}
