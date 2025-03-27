package telran.monitoring;

import java.sql.ResultSet;
import java.util.NoSuchElementException;

public class DataSourceSqlEmail extends DataSourceSql {

    private static final String EMAIL_ADDRESS = "email_address";
    private static final String NOTIFICATION_GROUPS_TABLE = "notification_groups";
    private static final String NOTIFICATIONS_GROUP_ID = "notification_group_id";
    private static final String PATIENTS_TABLE = "patients";
    private static final String PATIENT_ID = "patient_id";

    private static final String STATEMENT_STRING = "select %s from %s where " +
            "%s = (select %s from patients where %s = ?)"
                    .formatted(EMAIL_ADDRESS, NOTIFICATION_GROUPS_TABLE, NOTIFICATIONS_GROUP_ID,
                            PATIENTS_TABLE, PATIENT_ID);

    protected DataSourceSqlEmail() {
        super(STATEMENT_STRING);

    }

    @Override
    protected String resultSetProcessing(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                String email = resultSet.getString(EMAIL_ADDRESS);
                logger.log("fine", "email address received from DB is " + email);
                return email;
            } else {
                throw new NoSuchElementException("no data provided for patient");
            }
        } catch (Exception e) {
            logger.log("severe", "error: " + e);
            throw new RuntimeException(e);
        }
    }
}
