package telran.monitoring;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
            logger.log("debug", "received data: %s".formatted(new String(buffer)));
            socket.send(packet);
        }
    }
}