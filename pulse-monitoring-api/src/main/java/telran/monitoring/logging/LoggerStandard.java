package telran.monitoring.logging;

import java.util.HashMap;
import java.util.logging.*;

public class LoggerStandard implements Logger {
    private java.util.logging.Logger logger;
    static private String defaultValue = Logger.defaultValue;
    static private HashMap<String, String> levelsMap = new HashMap<>(){{
        put("debug", "config");
        put("trace", "fine");
        put("error", "severe");
    }};
    
    static void setDefaultLevel(String level) {
       LoggerStandard.defaultValue = level; 
    }

    public LoggerStandard(String loggerName) {
        LogManager.getLogManager().reset();
        logger = java.util.logging.Logger.getLogger(loggerName);
        String level = System.getenv("LOGGER_LEVEL");
        level = level == null ? defaultValue : level;
        String javaLevel = getLevel(level);
        Level loggerLevel = Level.parse(javaLevel.toUpperCase());
        logger.setLevel(loggerLevel);
        Handler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(loggerLevel);
        logger.addHandler(consoleHandler);
    }

    @Override
    public void log(String level, String message) {
        String javaLevel = getLevel(level);
        logger.log(Level.parse(javaLevel.toUpperCase()), message);
    }

    private String getLevel(String level) {
        String javaLevel = levelsMap.get(level);
        return javaLevel == null ? level : javaLevel;
    }

}
