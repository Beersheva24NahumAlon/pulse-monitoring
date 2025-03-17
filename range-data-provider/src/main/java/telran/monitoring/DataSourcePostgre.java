package telran.monitoring;

import java.sql.*;
import java.util.Map;
import java.util.NoSuchElementException;

import telran.monitoring.api.PulseRange;
import telran.monitoring.logging.Logger;

public class DataSourcePostgre implements DataSource {
    private static final String DEFAULT_DB_USERNAME = "postgres";
    private static final String DEFAULT_DB_HOST = "patient-db.c8ri40yyunrd.us-east-1.rds.amazonaws.com";
    private static final String DEFAULT_DB_PORT = "5432";
    private static final String DEFAULT_DB_NAME = "patients_db";;
    private static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    PreparedStatement statement;
    Map<String, String> env;
    Logger logger;

    public DataSourcePostgre(Logger logger, Map<String, String> env) {
        this.logger = logger;
        this.env = env;
        try { 
            Class.forName(getDriverClassName());
            Connection connection = DriverManager.getConnection(getConnectionString(), getUserName(), getPassword());
            statement = connection.prepareStatement(
                    "select min_pulse_value, max_pulse_value from groups " +
                            "where id = (select group_id from patients where patient_id = ?)");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getDbName() {
        return env.getOrDefault("DB_NAME", DEFAULT_DB_NAME);
    }

    private String getDbHost() {
        return env.getOrDefault("DB_HOST", DEFAULT_DB_HOST);
    }

    public PulseRange getRange(long patientId) {
        try {
            statement.setLong(1, patientId);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                int min =  res.getInt("min_pulse_value");
                int max =  res.getInt("max_pulse_value");
                return new PulseRange(min, max);
            } else {
                throw new NoSuchElementException("patient with id %d has not found in database".formatted(patientId));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getDriverClassName() {
        return env.getOrDefault("DRIVER_CLASS_NAME", DEFAULT_DRIVER_CLASS_NAME);
    }

    private String getConnectionString() {
        return "jdbc:postgresql://%s:%s/%s".formatted(getDbHost(), getDbPort(), getDbName());
    }

    private String getDbPort() {
        return env.getOrDefault("DB_PORT", DEFAULT_DB_PORT);
    }

    private String getPassword() {
        String res = env.get("DB_PASSWORD");
        if (res == null) {
            throw new RuntimeException("password must be specified in env variable");
        }
        return res;
    }

    private String getUserName() {
        String res = env.getOrDefault("DB_USERNAME", DEFAULT_DB_USERNAME);
        return res;
    }
}
