import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.commons.text.WordUtils;
import org.apache.jena.query.*;
import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            var anotation = annotateColumn(tableNames.get(i), columnIds.get(i));
            if(anotation != null) { annotations.add(anotation); }
            else { annotations.add("-1"); }
            System.out.println("DONE");
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

    // returns annotation for whole column
    static String annotateColumn(String tableName, String columnId) {
        var columnItems = getColumnItems(tableName, columnId);
        var preprocessedItems = preprocessItems(columnItems);
        Map<String, List<String>> itemsToClasses = new HashMap();
        for(String item: preprocessedItems) {
            itemsToClasses.put(item, getResourceClasses(item));
        }

        // here choose algotrithm
        Annotator annotator = new SeriesBasedAnnotator();
//        Annotator annotator = new NodeBasedAnnotator();

        return annotator.getAnnotation(itemsToClasses);
    }

    // returns all items from column
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

    // returns preprocessed items from column
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

    // returns all classes for single item from column
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
                + "dbr:" + resource + " a ?subclass .\n"
                + "?subclass rdfs:subClassOf ?class .\n"
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

//    static String listToString(List<String> list) {
//        String output = "";
//        for (String item : list) {
//            output += item;
//            if (item != list.get(list.size() - 1)) {
//                output += " ";
//            }
//        }
//        return output;
//    }
}
