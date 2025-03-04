package telran.monitoring;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import static telran.monitoring.Configuration.*;

public class Main {
    static Logger logger = new LoggerStandard("receiver");

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[MAX_SIZE];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String jsonStr = new String(packet.getData());
            logger.log("finest", jsonStr);
            logPulseValue(jsonStr);
            socket.send(packet);
        }
    }

    private static void logPulseValue(String jsonStr) {
        SensorData sensorData = SensorData.of(jsonStr);
        int value = sensorData.value();
        if (value >= WARNING_LOG_VALUE && value <= ERROR_LOG_VALUE) {
            logValue("warning", sensorData);
        } else if (value > ERROR_LOG_VALUE) {
            logValue("error", sensorData);
        }
    }

    private static void logValue(String level, SensorData sensorData) {
        logger.log(level, String.format("patient %d has pulse value greater than %d", sensorData.patientId(),
                level.equals("warning") ? WARNING_LOG_VALUE : ERROR_LOG_VALUE));
    }

}