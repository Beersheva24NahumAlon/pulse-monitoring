package telran.monitoring;

import telran.monitoring.api.PulseRange;

public interface RangeProviderClient {
    PulseRange getRange(long patientId);
}
