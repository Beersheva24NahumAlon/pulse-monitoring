package telran.monitoring;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import telran.monitoring.api.AbnormalPulseData;
import telran.monitoring.api.NotificationData;
import telran.monitoring.api.PulseRange;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import telran.monitoring.messagebox.MessageBox;

public class AbnormalValuesNotifyer {
    private static final String DEFAULT_MESSAGE_BOX_CLASS = "telran.monitoring.NotificationDataMessageBox";
    private static final String DEFAULT_MESSAGE_BOX = "notifications";
    private static final String DEFAULT_EMAIL_PROVIDER_CLASS = "telran.monitoring.EmailProviderClientMapImpl";

    Logger logger = new LoggerStandard("abnormal-values-notifyer");
    Map<String, String> env = System.getenv();
    EmailProviderClient rangeProviderClient = getEmailProviderClient(getEmailProviderClientClass());

    public void handleRequest(final DynamodbEvent event, final Context context) {
        event.getRecords().forEach(r -> {
            AbnormalPulseData abnormalPulseData = getAbnormalPulseData(r);
            long patientId = abnormalPulseData.patientId();
            String email;
            try {
                email = rangeProviderClient.getEmail(patientId);
                logger.log("finest", "data for computing: %s".formatted(abnormalPulseData.toString()));
                computeAbnormalPulseData(abnormalPulseData, email);
            } catch (Exception e) {
                logger.log("error", "error - " + e.toString());
            }
        });
    }

    private void computeAbnormalPulseData(AbnormalPulseData abnormalPulseData, String email) {
        // send email

        // save to dynamodb
        long patientId = abnormalPulseData.patientId();
        int value = abnormalPulseData.value();
        long timestamp = abnormalPulseData.timestamp();
        String text = "Please pay attantion: patient id %d has abnormal pulse value: %d".formatted(patientId, value);
        NotificationData notificationData = new NotificationData(patientId, email, text, timestamp);
        logger.log("fine", "notification data to save: %s".formatted(notificationData.toString()));
        saveNotificationData(notificationData);

        // long patientId = sensorData.patientId();
        // int value = sensorData.value();
        // int max = range.max();
        // int min = range.min();
        // if (value < min || value > max) {
        // logger.log("fine", "abnormal pulse value (%d) recognized for patient number
        // %d (range: [%d - %d])"
        // .formatted(value, patientId, min, max));
        // AbnormalPulseData abnormalPulseData = new AbnormalPulseData(patientId, value,
        // range, sensorData.timestamp());
        // logger.log("fine", "abnormal pulse data to save:
        // %s".formatted(abnormalPulseData.toString()));
        // saveNotificationData(abnormalPulseData);
        // } else {
        // logger.log("finest", "value of pulse (%d) is in range [%d - %d] for patient
        // number %d"
        // .formatted(value, min, max, patientId));
        // }
    }

    @SuppressWarnings("unchecked")
    private void saveNotificationData(NotificationData notificationData) {
        BasicConfigurator.configure();
        try {
            MessageBox<NotificationData> messageBox = MessageBoxFactory.getMessageBox(getMessageBoxClass(),
                    getMessageBox());
            messageBox.put(notificationData);
        } catch (Exception e) {
            logger.log("error", e.toString());
        }
    }

    private AbnormalPulseData getAbnormalPulseData(DynamodbStreamRecord r) {
        Map<String, AttributeValue> map = r.getDynamodb().getNewImage();
        long patientId = Long.parseLong(map.get("patientId").getN());
        int value = Integer.parseInt(map.get("value").getN());
        int min = Integer.parseInt(map.get("min").getN());
        int max = Integer.parseInt(map.get("max").getN());
        long timestamp = Long.parseLong(map.get("timestamp").getN());
        AbnormalPulseData abnormalPulseData = new AbnormalPulseData(patientId, value, new PulseRange(min, max),
                timestamp);
        return abnormalPulseData;
    }

    private String getMessageBoxClass() {
        return env.getOrDefault("MESSAGE_BOX_CLASS", DEFAULT_MESSAGE_BOX_CLASS);
    }

    private String getMessageBox() {
        return env.getOrDefault("MESSAGE_BOX_NAME", DEFAULT_MESSAGE_BOX);
    }

    private String getEmailProviderClientClass() {
        return env.getOrDefault("RANGE_PROVIDER_CLASS", DEFAULT_EMAIL_PROVIDER_CLASS);
    }

    private EmailProviderClient getEmailProviderClient(String className) {
        EmailProviderClient res = new EmailProviderClientMapImpl();
        try {
            res = (EmailProviderClient) Class.forName(className).getConstructor().newInstance();
            logger.log("config", "created object of class %s".formatted(className));
        } catch (Exception e) {
            logger.log("warning", "class %s has not found, created object of class by default (%s)"
                    .formatted(className, DEFAULT_EMAIL_PROVIDER_CLASS));
        }
        return res;
    }
}
