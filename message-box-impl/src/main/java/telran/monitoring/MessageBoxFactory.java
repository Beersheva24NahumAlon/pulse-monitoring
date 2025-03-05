package telran.monitoring;

import java.lang.reflect.Constructor;
import telran.monitoring.messagebox.MessageBox;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MessageBoxFactory<T> {
    
    public static MessageBox getMessageBox(String className, String messageBox) throws Exception{
        Class<MessageBox> clazz = (Class<MessageBox>) Class.forName(className);
        Constructor<MessageBox> constructor = clazz.getConstructor(String.class);
        MessageBox res = constructor.newInstance(messageBox);
        return res;
    }
}
