package telran.monitoring;

import java.util.HashMap;
import java.util.NoSuchElementException;

import telran.monitoring.api.PulseRange;

public class RangeProviderClientMapImpl implements RangeProviderClient {
    HashMap<Long, PulseRange> map = new HashMap<>(){{
        put(101l, new PulseRange(40, 190));
    }};

    @Override
    public PulseRange getRange(long patientId) {
        PulseRange res = map.get(patientId);
        if (res == null) {
            throw new NoSuchElementException("patient with id %d has not found in database".formatted(patientId));
        }
        return res;
    }

}
