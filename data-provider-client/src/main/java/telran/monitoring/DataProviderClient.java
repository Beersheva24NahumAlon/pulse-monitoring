package telran.monitoring;

import telran.monitoring.logging.*;

public interface DataProviderClient {
    static Logger[] loggers = new Logger[1];

    String getDataForPatient(long patientId);

    static DataProviderClient getDataProviderClient(String className, Logger logger, String connectionString) {
        loggers[0] = logger;
        try {
            return (DataProviderClient) Class.forName(className).getConstructor(String.class).newInstance(connectionString);
        } catch (Exception e) {
            logger.log("error", "error: " + e.toString());
            throw new RuntimeException(e);
        }
    }
}
