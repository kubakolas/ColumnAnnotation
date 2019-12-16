import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.commons.text.WordUtils;
import org.apache.jena.query.*;
import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String args[]) throws Exception {
        String path = "task_data.csv";
        List<String> tableNames = new ArrayList<>();
        List<String> columnIds = new ArrayList<>();
        try {
            FileReader filereader = new FileReader(path);
            CSVReader csvReader = new CSVReader(filereader);
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                tableNames.add(nextRecord[0]);
                columnIds.add(nextRecord[1]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        var annotationsNumber = tableNames.size();
        List<String> annotations = new ArrayList<>();
        for (int i = 0; i < annotationsNumber; i++) {
            System.out.print("Annotating " + (i+1) + " column...");
            annotations.add(annotateColumn(tableNames.get(i), columnIds.get(i)));
            System.out.println(" DONE");
        }

        // save annotations to CSV
        System.out.println("Saving results to csv...");
        try (
                Writer writer = Files.newBufferedWriter(Paths.get("annotations.csv"));
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            for (int i = 0; i < annotationsNumber; i++) {
                csvWriter.writeNext(new String[]{tableNames.get(i), columnIds.get(i), annotations.get(i)});
            }
        }
        var evaluator = new AnnotationsEvaluator();
        System.out.printf("%.2f", evaluator.evaluate());
    }

    static String annotateColumn(String tableName, String columnId) {
        var columnItems = getColumnItems(tableName, columnId);
        var preprocessedItems = preprocessItems(columnItems);
        Map<String, List<String>> itemsToClasses = new HashMap();
        for(String item: preprocessedItems) {
            itemsToClasses.put(item, getResourceClasses(item));
        }


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

    public static boolean compareList(List<String> ls1, List<String> ls2){
        return ls1.toString().contentEquals(ls2.toString());
    }

    public static boolean seriesExists(final List<ClassSeries> list, final List<String> series){
        var seriesOptional = list.stream().filter(o -> compareList(o.series, series)).findFirst();
        if (seriesOptional.isPresent()) {
            var seriesObj = seriesOptional.get();
            seriesObj.increaseCounter();
            return true;
        }
        return false;
    }

    static List<String> getResourceClasses(String resource) {
        ParameterizedSparqlString qs = new ParameterizedSparqlString(""
                + "PREFIX dbr:     <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
                + "\n"
                + "SELECT DISTINCT ?class ?superclass WHERE {\n"
                + "dbr:" + resource + " a ?class .\n"
                + "?class rdfs:subClassOf* ?superclass .\n"
                + "FILTER NOT EXISTS {\n"
                + "?subclass ^a  dbr:" + resource + ";\n"
                + "rdfs:subClassOf ?class .\n"
                + "FILTER ( ?subclass != ?class )\n"
                + "}.\n"
                + "FILTER(regex(?class, \"dbpedia.org/ontology\", \"i\")) .\n"
                + "FILTER(regex(?superclass, \"dbpedia.org/ontology\", \"i\")) .\n"
                + "FILTER ( ?superclass != ?class )\n"
                + "}\n");

        QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
        ResultSet results = exec.execSelect();
        var classes = new ArrayList<String>();
        Integer counter = 0;
        while (results.hasNext()) {
            var result = results.next();
            if (counter == 0) {
                try {
                    classes.add(result.get("class").toString());
                } catch(Exception e ) { }
                counter++;
            }
            try {
                var superclass = result.get("superclass").toString();
                if(!classes.contains(superclass)) {
                    classes.add(superclass);
                }
            } catch(Exception e) {}
        }
        return classes;
    }

    static List<String> preprocessItems(List<String> items) {
        List<String> preprocessedItems = new ArrayList();

        for (String item: items) {
			String newItem = WordUtils.capitalize(item);
            newItem = newItem.replaceAll(" ","_");
            newItem = newItem.replaceAll("__","_");
            newItem = newItem.replaceAll("[^A-Za-z0-9_]","");

            preprocessedItems.add(newItem);
        }
        return preprocessedItems;
    }

    static List<String> getColumnItems(String tableName, String columnId) {
        String path = "data/" + tableName + ".csv";
        List<String> columnItems = new ArrayList<>();
        try {
            FileReader filereader = new FileReader(path);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                columnItems.add(nextRecord[Integer.parseInt(columnId)]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return columnItems;
    }

    static String listToString(List<String> list) {
        String output = "";
        for (String item : list) {
            output += item;
            if (item != list.get(list.size() - 1)) {
                output += " ";
            }
        }
        return output;
    }

    static Map<String, Long> calculateClassesCount(Map<String,List<String>> temp) {
        List<String> classList = new ArrayList<>();

        for (List<String> callasses: temp.values()) {
            classList.addAll(callasses);
        }

        Map<String, Long> result =
                classList.stream().collect(
                        Collectors.groupingBy(
                                Function.identity(), Collectors.counting()
                        )
                );

        Map.Entry<String, Long> maxEntry = null;
        for (Map.Entry<String, Long> entry : result.entrySet())
        {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
            {
                maxEntry = entry;
            }
        }
        System.out.println(maxEntry);

        return result;
    }
}
