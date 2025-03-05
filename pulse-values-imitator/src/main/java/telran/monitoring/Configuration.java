package telran.monitoring;

public interface Configuration {

    int MIN_PULSE_VALUE = 40;
    int MAX_PULSE_VALUE = 240;
    int TIMEOUT_RESPONSE = 2000;
    int TIMEOUT_SEND = 500;
    String DEFAULT_HOST = "localhost";
    int DEFAULT_PORT = 5000;
    int DEFAULT_N_PATIENTS = 5;
    int DEFAULT_N_PACKETS = 10;
    int JUMP_PROB = 10;
    int MIN_JUMP_PERCENT = 10;
    int MAX_JUMP_PERCENT = 100;
    int JUMP_POSITIVE_PROB = 70;
    long PATIENT_ID_FOR_INFO_LOGGING = 3;

}
