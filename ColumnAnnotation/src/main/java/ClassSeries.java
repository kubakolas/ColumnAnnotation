import java.util.List;

public class ClassSeries {
    List<String> series;
    Integer count;

    ClassSeries(List<String> series) {
        this.series = series;
        this.count = 1;
    }

    public void increaseCounter() {
        count++;
    }
}