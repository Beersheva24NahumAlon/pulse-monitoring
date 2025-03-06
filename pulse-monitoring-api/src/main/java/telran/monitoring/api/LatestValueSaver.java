package telran.monitoring.api;

import java.util.List;

public interface LatestValueSaver {
    void addValue(SensorData sensorData);

    List<SensorData> getAllValues(long patientId);

    SensorData getLastValue(long patientId);

    void clearValues(long patientId);

    void clearAndAddValue(long patientId, SensorData sensorData);
}   
