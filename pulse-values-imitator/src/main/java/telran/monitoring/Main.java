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
    static HashMap<Long, Integer> lastPulseValues = new HashMap<>();
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
        String jsonStr = sensorData.toString();
        try {
            udpSend(jsonStr);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    private static void udpSend(String jsonStr) throws Exception {
        logger.log("finest", "data to be send is: %s".formatted(jsonStr));
        byte[] bufferSend = jsonStr.getBytes();
        DatagramPacket packet = new DatagramPacket(bufferSend, bufferSend.length, InetAddress.getByName(DEFAULT_HOST),
                DEFAULT_PORT);
        socket.send(packet);
        socket.receive(packet);
        if (!jsonStr.equals(new String(packet.getData()))) {
            throw new Exception("received packet doesn't equal the send one");
        }
    }

    private static SensorData getRandomSensorData() {
        long patientId = random.nextInt(1, DEFAULT_N_PATIENTS + 1);
        int value = getPulseValue(patientId);
        long timestamp = System.currentTimeMillis();
        return new SensorData(patientId, value, timestamp);
    }

    private static int getPulseValue(long patientId) {
        Integer last = lastPulseValues.get(patientId);
        last = last == null ? NORMAL_PULSE : last;
        float chance = random.nextFloat(-1, 1);
        int inc = random.nextInt(MIN_NORMAL_OFFSET, MAX_NORMAL_OFFSET + 1);
        if (chance > 0) {
            if (chance < PROP_INCREASE) {
                inc = random.nextInt(MAX_PULSE_VALUE - last);
            }
        } else {
            chance = -chance;
            if (chance < PROP_DECREASE) {
                inc = -random.nextInt(last - MIN_PULSE_VALUE);
            }
        }
        lastPulseValues.put(patientId, last + inc);
        return last + inc;
    }

}