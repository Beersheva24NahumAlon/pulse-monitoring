package telran.monitoring.api;

import org.json.JSONObject;

public record ReducePulseData(long patientId, int avgValue, long timestamp) {
    
    public static ReducePulseData of(String jsonStr) {
        JSONObject jsonObj = new JSONObject(jsonStr);
        long patientId = jsonObj.getLong("patientId");
        int avgValue = jsonObj.getInt("avgValue");
        long timestamp = jsonObj.getLong("timestamp");
        return new ReducePulseData(patientId, avgValue, timestamp);
    }

    @Override
    public String toString() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("patientId", patientId);
        jsonObj.put("oldValue", avgValue);
        jsonObj.put("timestamp", timestamp);
        return jsonObj.toString();
    }
}
