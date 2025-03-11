package telran.monitoring;

import java.lang.reflect.Constructor;

@SuppressWarnings("unchecked")
public class RangeProviderFactory {
    
    public static RangeProviderClient getRangeProviderClient(String className) throws Exception{
        Class<RangeProviderClient> clazz = (Class<RangeProviderClient>) Class.forName(className);
        Constructor<RangeProviderClient> constructor = clazz.getConstructor();
        RangeProviderClient res = constructor.newInstance();
        return res;
    }
}
