package telran.monitoring;

import java.util.Map;

import org.bson.Document;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;

import telran.monitoring.logging.Logger;

public class DataSourceMongo implements DataSource {
    private static final String DEFAULT_MONGODB_USERNAME = "root";
    private static final String DEFAULT_MONGODB_CLUSTER = "Cluster0";

    MongoCollection<Document> collection;
    Map<String, String> env;
    Logger logger;
    String databaseName = "pulse_monitoring";
    String collectionName = "avg_pulse_values";

    public DataSourceMongo(Logger logger, Map<String, String> env) {
        this.env = env;
        this.logger = logger;
        this.collection = MongoClients
                .create("mongodb+srv://%s:%s@%s.dvztv.mongodb.net/?retryWrites=true&w=majority&appName=%s"
                .formatted(getMongoUser(), getMongoPassword(), getMongoCluster(), getMongoCluster()))
                .getDatabase(databaseName)
                .getCollection(collectionName);
    }

    @Override
    public void put(Document doc) {
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
