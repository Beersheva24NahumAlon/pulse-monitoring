package telran.monitoring;

import java.sql.*;
import java.util.NoSuchElementException;

import telran.monitoring.api.PulseRange;
import telran.monitoring.logging.Logger;

public class DataSource {
    static final String DEFAULT_DRIVER_CLASS_NAME = "org.postgresql.Driver";
    
    PreparedStatement statement;
    Connection connection;
    static Logger logger;
    static {
        String driverClassName = getDriverClassName();
        logger.log("config", "driver class name is %s".formatted(driverClassName));
        try {
            Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource(String connectionString, String username, String password, Logger logger) {
        try {
            DataSource.logger = logger;
            connection = DriverManager.getConnection(connectionString, username, password);
            statement = connection.prepareStatement(
                "select min_pulse_value, max_pulse_value from groups " +
                "where id = (select group_id from patients where patient_id = ?)"
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public PulseRange getRange(long patientId) {
        try {
            statement.setLong(1, patientId);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                int min = res.getInt("min_pulse_value");
                int max = res.getInt("max_pulse_value");
                return new PulseRange(min, max);
            } else {
                throw new NoSuchElementException("patient with id %d has not found in database".formatted(patientId));
            }
        } catch (SQLException e) {
            throw new RuntimeException();
        }  
    } 

    private static String getDriverClassName() {
        String res = System.getenv("DRIVER_CLASS_NAME");
        return res == null ? DEFAULT_DRIVER_CLASS_NAME : res;
    }
}
