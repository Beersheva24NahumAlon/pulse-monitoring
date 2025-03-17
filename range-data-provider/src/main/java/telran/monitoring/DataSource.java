package telran.monitoring;

import telran.monitoring.api.PulseRange;

public interface DataSource {
    public PulseRange getRange(long patientId);
}
