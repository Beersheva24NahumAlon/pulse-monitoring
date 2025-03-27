package telran.monitoring;

import java.util.Map;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.model.*;

import telran.monitoring.logging.Logger;

public class MailSenderSes implements MailSender {

    private static final String DEFAULT_SENDER_ADDRESS = "nahumalon2301@gmail.com";
    private static final String DEFAULT_REGION = "us-east-1";

    Map<String, String> env = System.getenv();
    Logger logger = loggers[0];
    String senderAddress = getSenderAddress();
    String region = getRegion();
    SesClient sesClient;

    public MailSenderSes() {
        configLog();
        sesClient = SesClient.builder().region(Region.of(region)).build();
    }

    private void configLog() {
        logger.log("config", "sender address: %s".formatted(senderAddress));
        logger.log("config", "region: %s".formatted(region));
    }

    private String getRegion() {
        return env.getOrDefault("REGION", DEFAULT_REGION);
    }

    private String getSenderAddress() {
        return env.getOrDefault("SENDER_ADDRESS", DEFAULT_SENDER_ADDRESS);
    }

    @Override
    public void sendMail(String subject, String recipientAddress, String text) {
        Destination destination = Destination.builder().toAddresses(recipientAddress).build();
        Content content = Content.builder().data(text).build();
        Content subjectContent = Content.builder().data(subject).build();
        Body body = Body.builder().html(content).build();
        Message message = Message.builder().subject(subjectContent).body(body).build();
        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(senderAddress)
                .build();
        try {
            logger.log("finest", "email request %s".formatted(emailRequest));
            SendEmailResponse emailResponse = sesClient.sendEmail(emailRequest);
            logger.log("finest", "email response: %s".formatted(emailResponse));
        } catch (SesException e) {
            logger.log("error", "error: " + e.toString());
            throw new RuntimeException(e);
        }
    }

}
