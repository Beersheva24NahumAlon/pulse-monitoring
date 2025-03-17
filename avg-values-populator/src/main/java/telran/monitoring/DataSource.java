package telran.monitoring;

import telran.monitoring.api.ReducePulseData;

public interface DataSource {
    void put(ReducePulseData avgPulseData);
}
