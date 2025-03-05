package telran.monitoring;

import java.util.HashMap;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import telran.monitoring.messagebox.MessageBox;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest.Builder;

public abstract class MessageBoxDynamoDB<T> implements MessageBox<T> {
    DynamoDbClient client;
    Builder request;

    public MessageBoxDynamoDB(String messageBox) {
        client = DynamoDbClient.builder().build();
        request = PutItemRequest.builder().tableName(messageBox);
    }

    @Override
    public void put(T object) {
        client.putItem(request.item(getMap(object)).build());
    }

    abstract HashMap<String, AttributeValue> getMap(T object);
}
