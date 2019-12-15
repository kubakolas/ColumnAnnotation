import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.jena.query.*;
import java.io.FileReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
        List<List<String>> annotations = new ArrayList<>();
        for (int i = 0; i < tableNames.size(); i++) {
            annotations.add(annotateColumn(tableNames.get(i), columnIds.get(i)));
        }

        // save annotations to CSV

        try (
                Writer writer = Files.newBufferedWriter(Paths.get("annotations.csv"));
                CSVWriter csvWriter = new CSVWriter(writer,
                        CSVWriter.DEFAULT_SEPARATOR,
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.DEFAULT_LINE_END);
        ) {
            for (int i = 0; i < tableNames.size(); i++) {
                csvWriter.writeNext(new String[]{tableNames.get(i), columnIds.get(i), listToString(annotations.get(i))});
            }
        }
    }

    static List<String> annotateColumn(String tableName, String columnId) {
        List<String> columnItems = getColumnItems(tableName, columnId);
        List<String> preprocessedItems = preprocessItems(columnItems);
        
        ParameterizedSparqlString qs = new ParameterizedSparqlString(""
                + "PREFIX dbr:     <http://dbpedia.org/resource/>\n"
                + "PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#>"
                + "\n"
                + "SELECT DISTINCT ?class ?superclass WHERE {\n"
                + "dbr:Casablanca a ?class .\n"
                + "?class rdfs:subClassOf* ?superclass .\n"
                + "FILTER NOT EXISTS {\n"
                + "?subclass ^a  dbr:Casablanca ;\n"
                + "rdfs:subClassOf ?class .\n"
                + "FILTER ( ?subclass != ?class )\n"
                + "}.\n"
                + "FILTER(regex(?class, \"dbpedia.org/ontology\", \"i\")) .\n"
                + "FILTER(regex(?superclass, \"dbpedia.org/ontology\", \"i\")) .\n"
                + "FILTER ( ?superclass != ?class )\n"
                + "}\n");

        QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
        ResultSet results = exec.execSelect();
        while (results.hasNext()) {
            System.out.println(results.next().get("superclass").toString());
        }
        ResultSetFormatter.out(results);

        List<String> columnAnnotations = new ArrayList<>();
        return columnAnnotations;
    }

    static List<String> preprocessItems(List<String> items) {
        List<String> preprocessedItems = new ArrayList();

        for (String item: items) {
            String newItem = item.replaceAll(" ","_");
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
}
