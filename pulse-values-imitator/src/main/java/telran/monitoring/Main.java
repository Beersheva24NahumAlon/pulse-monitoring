package telran.monitoring;

import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import java.net.*;
import java.util.HashMap;
import java.util.Random;
import static telran.monitoring.Configuration.*;

public class Main {
    static Logger logger = new LoggerStandard("imitator");
    static DatagramSocket socket = null;
    static HashMap<Long, Integer> patientIdPulseValue = new HashMap<>();
    static Random random = new Random();

    public static void main(String[] args) throws Exception {
        socket = new DatagramSocket();
        socket.setSoTimeout(TIMEOUT_RESPONSE);
        for (int i = 1; i <= DEFAULT_N_PACKETS; i++) {
            send();
        }
    }

    static void send() {
        SensorData sensorData = getRandomSensorData();
        logger.log("finest", sensorData.toString());
        if (sensorData.patientId() == PATIENT_ID_FOR_INFO_LOGGING) {
            logger.log("info", String.format("Pulse value for patient %d is %d",
                    PATIENT_ID_FOR_INFO_LOGGING, sensorData.value()));
        }
        String jsonStr = sensorData.toString();
        try {
            udpSend(jsonStr);
            Thread.sleep(TIMEOUT_SEND);
        } catch (SocketTimeoutException e) {
        } catch (Exception e) {
            logger.log("error", e.getMessage());
        }
    }

    private static void udpSend(String jsonStr) throws Exception {
        logger.log("finest", "data to be send is: %s".formatted(jsonStr));
        byte[] bufferSend = jsonStr.getBytes();
        DatagramPacket packet = new DatagramPacket(bufferSend, bufferSend.length,
                InetAddress.getByName(DEFAULT_HOST), DEFAULT_PORT);
        socket.send(packet);
        socket.receive(packet);
        if (!jsonStr.equals(new String(packet.getData()))) {
            throw new Exception("received packet doesn't equal the send one");
        }
    }

    private static SensorData getRandomSensorData() {
        long patientId = random.nextInt(1, DEFAULT_N_PATIENTS + 1);
        int value = getRandomPulseValue(patientId);
        long timestamp = System.currentTimeMillis();
        return new SensorData(patientId, value, timestamp);
    }

    private static int getRandomPulseValue(long patientId) {
        int valueRes = patientIdPulseValue.computeIfAbsent(patientId,
                k -> getRandomNumber(MIN_PULSE_VALUE, MAX_PULSE_VALUE));
        if (chance(JUMP_PROB)) {
            valueRes = getValueWithJump(valueRes);
            patientIdPulseValue.put(patientId, valueRes);
        }
        return valueRes;
    }

    private static boolean chance(int prob) {
        return getRandomNumber(0, 99) < prob;
    }

    private static int getValueWithJump(int previousValue) {
        int jumpPercent = getRandomNumber(MIN_JUMP_PERCENT, MAX_JUMP_PERCENT);
        int jumpValue = previousValue * jumpPercent / 100;
        if (!chance(JUMP_POSITIVE_PROB)) {
            jumpValue = -jumpValue;
        }
        int res = previousValue + jumpValue;
        if (res < MIN_PULSE_VALUE) {
            res = MIN_PULSE_VALUE;
        } else if (res > MAX_PULSE_VALUE) {
            res = MAX_PULSE_VALUE;
        }
        return res;
    }

    static int getRandomNumber(int minValue, int maxValue) {
        return new Random().nextInt(minValue, maxValue + 1);
    }
}