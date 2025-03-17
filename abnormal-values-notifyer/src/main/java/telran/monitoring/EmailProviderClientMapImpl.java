package telran.monitoring;

import java.util.HashMap;
import java.util.NoSuchElementException;


public class EmailProviderClientMapImpl implements EmailProviderClient {
    HashMap<Long, String> map = new HashMap<>(){{
        put(1l, "test@gmail.com");
    }};

    @Override
    public String getEmail(long patientId) {
        String res = map.get(patientId);
        if (res == null) {
            throw new NoSuchElementException("patient with id %d has not found in database".formatted(patientId));
        }
        return res;
    }

}
