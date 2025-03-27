package telran.monitoring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import telran.monitoring.api.SensorData;
import telran.monitoring.logging.Logger;

public class LatestValuesSeverMap extends AbstractLatestSaverLogger {
    public LatestValuesSeverMap(Logger logger) {
        super(logger);
    }

    HashMap<Long, List<SensorData>> map = new HashMap<>();

    @Override
    public void addValue(SensorData sensorData) {
        long patientId = sensorData.patientId();
        List<SensorData> list = getAllValues(patientId);
        list.add(sensorData);
        map.put(patientId, list);
    }

    @Override
    public List<SensorData> getAllValues(long patientId) {
        return map.getOrDefault(patientId, new ArrayList<>());
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
        map.put(patientId, new ArrayList<>());
    }

    @Override
    public void clearAndAddValue(long patientId, SensorData sensorData) {
        map.put(patientId, List.of(sensorData));
    }

}
