package telran.monitoring;
import org.bson.Document;

public interface DataSource {

    void put(Document doc);
}
