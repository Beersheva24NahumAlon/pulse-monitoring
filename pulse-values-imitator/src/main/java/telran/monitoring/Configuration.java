package telran.monitoring;

public interface Configuration {

    int TIMEOUT_SEND = 500;
    int TIMEOUT_RESPONSE = 1000;
    String DEFAULT_HOST = "localhost";
    int DEFAULT_PORT = 5000;
    int DEFAULT_N_PATIENTS = 1;
    int DEFAULT_N_PACKETS = 100;
    int MAX_PACKET_LENGTH = 1500;

    int MIN_PULSE_VALUE = 40;
    int MAX_PULSE_VALUE = 240;
    float PROP_INCREASE = 0.1f;
    float PROP_DECREASE = 0.07f;
    int NORMAL_PULSE = 70;
    int MIN_NORMAL_OFFSET = -3;
    int MAX_NORMAL_OFFSET = 3;

}
