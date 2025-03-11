package telran.monitoring.api;

import org.json.JSONObject;

public record AbnormalPulseData(long patientId, int value, PulseRange range, long timestamp) {

    public static AbnormalPulseData of(String jsonStr) {
        JSONObject jsonObj = new JSONObject(jsonStr);
        long patientId = jsonObj.getLong("patientId");
        int value = jsonObj.getInt("value");
        PulseRange range = PulseRange.of(jsonObj.getString("range"));
        long timestamp = jsonObj.getLong("timestamp");
        return new AbnormalPulseData(patientId, value, range, timestamp);
    }

    @Override
    public String toString() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("patientId", patientId);
        jsonObj.put("value", value);
        jsonObj.put("range", range.toString());
        jsonObj.put("timestamp", timestamp);
        return jsonObj.toString();
    }
}
