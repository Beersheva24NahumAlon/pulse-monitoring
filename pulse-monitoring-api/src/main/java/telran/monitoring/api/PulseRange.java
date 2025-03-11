package telran.monitoring.api;

import org.json.JSONObject;

public record PulseRange(int min, int max) {

        public static PulseRange of(String jsonStr) {
        JSONObject jsonObj = new JSONObject(jsonStr);
        int min = jsonObj.getInt("min");
        int max = jsonObj.getInt("max");
        return new PulseRange(min, max);
    }

    @Override
    public String toString() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("min", min);
        jsonObj.put("max", max);
        return jsonObj.toString();
    }
}
