package telran.monitoring;

import java.util.Map;

import org.bson.Document;

import telran.monitoring.logging.Logger;

public class DataSourceTest implements DataSource {

    Map<String, String> env;
    Logger logger;

    public DataSourceTest(Logger logger, Map<String, String> env) {
        this.env = env;
        this.logger = logger;
    }

    @Override
    public void put(Document doc) {
        logger.log("info", "test storing to DB: %s".formatted(doc.toString()));
    }
}
