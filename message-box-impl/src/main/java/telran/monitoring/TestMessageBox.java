package telran.monitoring;

import telran.monitoring.logging.Logger;
import telran.monitoring.logging.LoggerStandard;
import telran.monitoring.messagebox.MessageBox;

public class TestMessageBox<T> implements MessageBox<T> {
    Logger logger;

    public TestMessageBox(String loggerName) {
       logger = new LoggerStandard(loggerName); 
    }

    @Override
    public void put(T object) {
        logger.log("info", "data for test saving: %s".formatted(object.toString()));
    }

}
