package telran.monitoring;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;

import org.apache.log4j.BasicConfigurator;

import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import telran.monitoring.messagebox.MessageBox;

import static telran.monitoring.Configuration.*;

public class Main {
    private static final String PULSE_VALUES_MESSAGE_BOX = "pulse-values";
    private static final String PULSE_VALUES_MESSAGE_BOX_CLASS = "telran.monitoring.SensorDataMessageBox";

    static Logger logger = new LoggerStandard("receiver");
    static Map<String, String> env = System.getenv();

    public static void main(String[] args) {
        BasicConfigurator.configure();
        try (DatagramSocket socket = new DatagramSocket(PORT)) {
            byte[] buffer = new byte[MAX_SIZE];
            @SuppressWarnings("unchecked")
            MessageBox<SensorData> messageBox = MessageBoxFactory.getMessageBox(getMessageBoxClass(), getMessageBox());
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String jsonStr = new String(packet.getData());
                logPulseValue(jsonStr);
                messageBox.put(SensorData.of(jsonStr));
                socket.send(packet);
            }
        } catch (Exception e) {
            logger.log("error", e.toString());
        }

    }

    private static String getMessageBoxClass() {
        return env.getOrDefault("MESSAGE_BOX_CLASS", PULSE_VALUES_MESSAGE_BOX_CLASS);
    }

    private static String getMessageBox() {
        return env.getOrDefault("MESSAGE_BOX_NAME", PULSE_VALUES_MESSAGE_BOX);
    }

    private static void logPulseValue(String jsonStr) {
        logger.log("finest", jsonStr);
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