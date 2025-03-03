package telran.monitoring;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;

public class Main {
    private static final int PORT = 5000;
    private static final int MAX_SIZE = 1500;
    Logger logger = new LoggerStandard("receiver");

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(PORT);
        byte[] buffer = new byte[MAX_SIZE];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            System.out.println(new String(buffer));
            socket.send(packet);
        }
    }
}