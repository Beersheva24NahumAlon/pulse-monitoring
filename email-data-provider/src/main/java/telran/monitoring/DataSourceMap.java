package telran.monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import telran.monitoring.logging.Logger;

public class DataSourceMap implements DataSource{
    HashMap<Long, String> map = new HashMap<>(){{
        put(123l, "test@gmail.com");
    }};
    Logger logger;
    Map<String, String> env;

    public DataSourceMap(Logger logger, Map<String, String> env) {
        this.logger = logger;
        this.env = env;
    }

    @Override
    public String getEmail(long patientId) {
        String res = map.get(patientId);
        if (res == null) {
            throw new NoSuchElementException("patient with id %d has not found".formatted(patientId));
        }
        return res;
    }

}
