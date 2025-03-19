package telran.monitoring;

import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

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
    private static final String FROM_EMAIL = "nahumalon2301@gmail.com";

    Logger logger = new LoggerStandard("abnormal-values-notifyer");
    Map<String, String> env = System.getenv();
    EmailProviderClient rangeProviderClient = getEmailProviderClient(getEmailProviderClientClass());
    SesClient client = SesClient.builder().build();

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
        long patientId = abnormalPulseData.patientId();
        int value = abnormalPulseData.value();
        long timestamp = abnormalPulseData.timestamp();
        String text = "Please pay attantion: patient id %d has abnormal pulse value: %d".formatted(patientId, value);
        // send email
        sendEmail(email, text);
        // save to dynamodb
        NotificationData notificationData = new NotificationData(patientId, email, text, timestamp);
        logger.log("fine", "notification data to save: %s".formatted(notificationData.toString()));
        saveNotificationData(notificationData);
    }

    private void sendEmail(String email, String text) {
        Destination destination = Destination.builder().toAddresses(email).build();
        Content content = Content.builder().data(text).build();
        Content subject = Content.builder().data(text).build();
        Body body = Body.builder().html(content).build();
        Message message = Message.builder().subject(subject).body(body).build();
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(FROM_EMAIL)
                .build();
        try {
            logger.log("fine", "Attempting to send an email to %s".formatted(email));
            client.sendEmail(emailRequest);
        } catch (SesException e) {
            logger.log("error", e.toString());
        }
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
