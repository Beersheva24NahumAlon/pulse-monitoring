package telran.monitoring;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class LatestValueSaverS3 extends AbstractLatestSaverLogger {

    String bucketName = getBucketName();

    S3Client amazonS3Client = S3Client.builder().build();

    protected LatestValueSaverS3(Logger logger) {
        super(logger);
    }

    private String getBucketName() {
        String res = System.getenv("BUCKET_NAME");
        if (res == null) {
            throw new IllegalStateException("env BUCKET_NAME should be defined");
        }
        return res;
    }

    @Override
    public void addValue(SensorData sensorData) {
        long patientId = sensorData.patientId();
        List<SensorData> list = getAllValues(patientId);
        list.add(sensorData);
        putListOfSensorDataToFile(patientId, list);
    }

    private void putListOfSensorDataToFile(long patientId, List<SensorData> list) {
        String fileName = getFileName(patientId);
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            String content = String.join("\n", list.stream().map(d -> d.toString()).toList());
            RequestBody requestBody = RequestBody.fromString(content);
            amazonS3Client.putObject(putObjectRequest, requestBody);
            logger.log("finest", "Put %d lines to %s".formatted(list.size(), fileName));
        } catch (Exception e) {
            logger.log("error", e.toString());
        }
    }

    @Override
    public List<SensorData> getAllValues(long patientId) {
        String fileName = getFileName(patientId);
        return getListSensorDataFromFile(fileName);
    }

    private String getFileName(long patientId) {
        return "latest-values-%d.json".formatted(patientId);
    }

    private List<SensorData> getListSensorDataFromFile(String fileName) {
        List<SensorData> res = new ArrayList<>();
        try {
            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .key(fileName)
                    .bucket(bucketName)
                    .build();
            ResponseBytes<GetObjectResponse> objectBytes = amazonS3Client.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();
            String objectContent = new String(data, StandardCharsets.UTF_8);
            String[] array = objectContent.split("\n");
            logger.log("finest", "Recieved %d lines from %s".formatted(array.length, fileName));
            res = Arrays.stream(array).map(s -> SensorData.of(s)).toList();
        } catch (NoSuchKeyException e) {
            logger.log("warning", e.toString());
        } catch (Exception e) {
            logger.log("error", e.toString());
            throw new RuntimeException();
        }
        return res;
    }

    @Override
    public SensorData getLastValue(long patientId) {
        SensorData res = null;
        List<SensorData> list = getAllValues(patientId);
        if (!list.isEmpty()) {
            res = list.getLast();
        }
        return res;
    }

    @Override
    public void clearValues(long patientId) {
        putListOfSensorDataToFile(patientId, new ArrayList<>());
    }

    @Override
    public void clearAndAddValue(long patientId, SensorData sensorData) {
        clearValues(patientId);
        addValue(sensorData);
    }

}
