package telran.monitoring;

import telran.monitoring.api.LatestValueSaver;
import telran.monitoring.logging.Logger;

public abstract class AbstractLatestSaverLogger implements LatestValueSaver {
    protected Logger logger;

    public AbstractLatestSaverLogger(Logger logger) {
        this.logger = logger;
    }
}
