package telran.monitoring.messagebox;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

abstract public class MessageBoxDynamoDB implements MessageBox{
    DynamoDbClient client;

    public MessageBoxDynamoDB() {
        client = DynamoDbClient.builder().build();
    }

    public void put(Object object, String messageBox) {
        PutItemRequest request = PutItemRequest.builder().tableName(getMessageBox()).item(getMap(object)).build();
        client.putItem(request);
    }

    public abstract HashMap<String, AttributeValue> getMap(Object object);

    public abstract String getMessageBox();
}
