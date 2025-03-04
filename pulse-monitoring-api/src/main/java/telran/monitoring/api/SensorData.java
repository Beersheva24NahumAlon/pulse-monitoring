package telran.monitoring.api;

import org.json.JSONObject;

public record SensorData(long patientId, int value, long timestamp) {
    
    public static SensorData of(String jsonStr) {
        JSONObject jsonObject = new JSONObject(jsonStr);
        long patientId = jsonObject.getLong("patientId");
        int value = jsonObject.getInt("value");
        long timestamp = jsonObject.getLong("timestamp");
        return new SensorData(patientId, value, timestamp);
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("patientId", patientId);
        jsonObject.put("value", value);
        jsonObject.put("timestamp", timestamp);
        return jsonObject.toString();
    }
}
