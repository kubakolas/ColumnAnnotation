import java.util.List;
import java.util.Map;

public interface Annotator {
    String getAnnotation(Map<String, List<String>> itemWithClasses);
}
