package telran.monitoring;

import java.util.HashMap;

import telran.monitoring.api.PulseRange;

public class RangeProviderClientMapImpl implements RangeProviderClient {
    HashMap<Long, PulseRange> map = new HashMap<>(){{
        put(101l, new PulseRange(40, 190));
    }};

    @Override
    public PulseRange getRange(long patientId) {
        return map.get(patientId);
    }

}
