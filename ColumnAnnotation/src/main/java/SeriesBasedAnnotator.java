import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SeriesBasedAnnotator implements Annotator {
    public String getAnnotation(Map<String, List<String>> itemsToClasses) {
        var seriesList = new ArrayList<ClassSeries>();
        for(var pair: itemsToClasses.entrySet()) {
            var classes = pair.getValue();
            for(int i=classes.size()-1; i >=0 ; i--) {
                var newSeries = classes.subList(i, classes.size());
                if(!seriesExists(seriesList, newSeries)) {
                    seriesList.add(new ClassSeries(newSeries));
                }
            }
        }
        if (seriesList.isEmpty()) {
            return "";
        }


        Integer allSeriesCounter = 0;
        for(var series: seriesList) {
            allSeriesCounter += series.count;
        }

        seriesList.sort(Comparator.comparing((ClassSeries o) -> o.count).reversed());

        ClassSeries finalSeries = seriesList.get(0);
        Integer currentSeriesCounter = finalSeries.count;
        for(int i = 1; i < seriesList.size(); i++) {
            var nextSeries = seriesList.get(i);
            if(nextSeries.series.containsAll(finalSeries.series) && nextSeries.count >= (allSeriesCounter-currentSeriesCounter)/3) {
                finalSeries = nextSeries;
                currentSeriesCounter += finalSeries.count;
            }
        }

        if (finalSeries.series.isEmpty()) {
            return "";
        }
        return finalSeries.series.get(0);
    }

    public boolean compareList(List<String> ls1, List<String> ls2){
        return ls1.toString().contentEquals(ls2.toString());
    }

    public boolean seriesExists(final List<ClassSeries> list, final List<String> series){
        var seriesOptional = list.stream().filter(o -> compareList(o.series, series)).findFirst();
        if (seriesOptional.isPresent()) {
            var seriesObj = seriesOptional.get();
            seriesObj.increaseCounter();
            return true;
        }
        return false;
    }
}
