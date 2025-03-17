package telran.monitoring;

import java.time.Instant;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import telran.monitoring.api.ReducePulseData;
import telran.monitoring.logging.Logger;

public class DataSourceMongo implements DataSource {
    private static final String DEFAULT_MONGODB_USERNAME = "root";
    private static final String DEFAULT_MONGODB_CLUSTER = "Cluster0";
    private static final String DEFAULT_COLLECTION_NAME = "avg_pulse_values";
    private static final String DEFAULT_DATABASE_NAME = "pulse_monitoring";

    MongoCollection<Document> collection;
    Map<String, String> env;
    Logger logger;

    public DataSourceMongo(Logger logger, Map<String, String> env) {
        this.env = env;
        this.logger = logger;
        this.collection = MongoClients
                .create("mongodb+srv://%s:%s@%s.dvztv.mongodb.net/?retryWrites=true&w=majority&appName=%s"
                        .formatted(getMongoUser(), getMongoPassword(), getMongoCluster(), getMongoCluster()))
                .getDatabase(getDatabaseName())
                .getCollection(getCollectionName());
    }

    private String getCollectionName() {
        return env.getOrDefault("COLLECTION_NAME", DEFAULT_COLLECTION_NAME);
    }

    private String getDatabaseName() {
        return env.getOrDefault("DATABASE_NAME", DEFAULT_DATABASE_NAME);
    }

    @Override
    public void put(ReducePulseData avgPulseData) {
        Document doc = new Document()
                .append("_id", new ObjectId())
                .append("patientId", avgPulseData.patientId())
                .append("avgValue", avgPulseData.avgValue())
                .append("timestamp", Instant.ofEpochSecond(avgPulseData.timestamp()).toString());
        collection.insertOne(doc);
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

}
