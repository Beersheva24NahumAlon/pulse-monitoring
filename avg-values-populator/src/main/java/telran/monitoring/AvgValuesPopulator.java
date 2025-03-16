package telran.monitoring;

import java.time.Instant;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import telran.monitoring.api.ReducePulseData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;

public class AvgValuesPopulator {
    private static final String DEFAULT_MONGODB_USERNAME = "root";
    private static final String DEFAULT_MONGODB_CLUSTER = "Cluster0";

    Logger logger = new LoggerStandard("avg-values-populator");
    Map<String, String> env = System.getenv();
    MongoClient mongoClient = MongoClients
            .create("mongodb+srv://%s:%s@%s.dvztv.mongodb.net/?retryWrites=true&w=majority&appName=%s"
                    .formatted(getMongoUser(), getMongoPassword(), getMongoCluster(), getMongoCluster()));
    MongoCollection<Document> collection = mongoClient
            .getDatabase("pulse_monitoring")
            .getCollection("avg_pulse_values");

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(r -> {
            ReducePulseData avgPulseData = getAvgPulseData(r);
            logger.log("finest", "data for saving: %s".formatted(avgPulseData.toString()));
            saveAvgPulseValueToDB(avgPulseData);
        });
    }

    private Object getMongoCluster() {
        String res = env.getOrDefault("MONGODB_CLUSTER", DEFAULT_MONGODB_CLUSTER);
        return res;
    }

    private Object getMongoPassword() {
        String res = env.get("MONGODB_PASSWORD");
        if (res == null) {
            throw new RuntimeException("password must be specified in env variable");
        }
        return res;
    }

    private Object getMongoUser() {
        String res = env.getOrDefault("MONGODB_USERNAME", DEFAULT_MONGODB_USERNAME);
        return res;
    }

    private void saveAvgPulseValueToDB(ReducePulseData avgPulseData) {
        Document doc = new Document()
                .append("_id", new ObjectId())
                .append("patientId", avgPulseData.patientId())
                .append("avgValue", avgPulseData.avgValue())
                .append("timestamp", Instant.ofEpochSecond(avgPulseData.timestamp()).toString());
        try {
            collection.insertOne(doc);
            logger.log("fine", "saving to db: %s".formatted(doc.toString()));
        } catch (Exception e) {
            logger.log("error", e.toString());
        }
    }

    private ReducePulseData getAvgPulseData(DynamodbStreamRecord r) {
        Map<String, AttributeValue> map = r.getDynamodb().getNewImage();
        long patientId = Long.parseLong(map.get("patientId").getN());
        int avgValue = Integer.parseInt(map.get("avgValue").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        ReducePulseData avgPulseData = new ReducePulseData(patientId, avgValue, timestamp);
        return avgPulseData;
    }

}
