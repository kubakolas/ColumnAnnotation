import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import org.apache.commons.text.WordUtils;
import org.apache.jena.base.Sys;
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
import java.util.stream.Stream;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.*;

public class Main {
    static int a = 1;
    public static void main(String args[]) throws Exception {

        //var evaluator = new evaluate();
        //System.out.printf("%.2f", evaluator.evaluate());

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
        List<String> annotations = new ArrayList<>();
        for (int i = 0; i < tableNames.size(); i++) {
            var anotation = annotateColumn(tableNames.get(i), columnIds.get(i));
            if(anotation != null)
            {annotations.add(anotation);}
            else{annotations.add("-1");}
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
                csvWriter.writeNext(new String[]{tableNames.get(i), columnIds.get(i), annotations.get(i)});
            }
        }
    }

    static String annotateColumn(String tableName, String columnId) {
        var columnItems = getColumnItems(tableName, columnId);
        var preprocessedItems = preprocessItems(columnItems);
        Map<String, List<String>> itemsToClasses = new HashMap();
        for(String item: preprocessedItems) {
            itemsToClasses.put(item, getResourceClasses(item));
        }

        return algorithm(itemsToClasses);
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
                classes.add(result.get("superclass").toString());
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

    static String algorithm(Map<String,List<String>> itemWithClasses){
        LinkedHashSet<String> ontologyDict = new LinkedHashSet();

        for (Map.Entry<String,List<String>> dict : itemWithClasses.entrySet()) {
            for (String ontology : dict.getValue())
            {
                ontologyDict.add(ontology);
            }
        }

        var nodeList = new ArrayList<Node>();

        var ontologyDictArray = new ArrayList<>(ontologyDict);

        for (Map.Entry<String,List<String>> dict : itemWithClasses.entrySet()) {
            var ontologyList = dict.getValue();
            var size = ontologyList.size();

            for (int i = 0; i < size; i++)
            {
                Node node = new Node();
                node.id = ontologyDictArray.indexOf(ontologyList.get(i));
                if(i+1 < ontologyList.size())
                {node.idNode = ontologyDictArray.indexOf(ontologyList.get(i+1));}
                else {node.idNode = -1;}

                node.ontology = ontologyList.get(i);

                nodeList.add(node);
            }
            }

        Map<Integer, List<String>> nodesBySubClass = nodeList.stream()
                .collect(groupingBy(p -> p.idNode, mapping((Node n) -> n.ontology , toList())));

        if(nodesBySubClass.containsKey((int)-1))
        {nodesBySubClass.remove((int)-1);}


        List<String> maxGroup = null;
        if(nodesBySubClass != null) {

            for (Map.Entry<Integer, List<String>> entry : nodesBySubClass.entrySet()) {
                if (maxGroup == null || entry.getValue().size() > maxGroup.size()) {
                    maxGroup = entry.getValue();
                }
            }

        }
        Map<String, Long> ontologyByMaxGroup = null;
        if(maxGroup != null) {
             ontologyByMaxGroup =
                    maxGroup.stream().collect(
                            groupingBy(
                                    Function.identity(), Collectors.counting()
                            )
                    );
        }

        Map.Entry<String, Long> maxOntology = null;
        if(ontologyByMaxGroup != null)
        {
            for (Map.Entry<String, Long> entry : ontologyByMaxGroup.entrySet())
            {
                if (maxOntology == null || entry.getValue().compareTo(maxOntology.getValue()) > 0)
                {
                    maxOntology = entry;
                }
            }
        }

        System.out.print(a++);
        if(maxOntology != null)
        {return maxOntology.getKey();}
        else{return null;}
    }

    static String temp(Map<String,List<String>> temp){
        List<String> classList = new ArrayList<>();

        for (List<String> callasses: temp.values()) {
            classList.addAll(callasses);
        }

        Map<String, Long> result =
                classList.stream().collect(
                        groupingBy(
                                Function.identity(), Collectors.counting()
                        )
                );

        Map.Entry<String, Long> maxGroup = null;
        for (Map.Entry<String, Long> entry : result.entrySet())
        {
            if (maxGroup == null || entry.getValue().compareTo(maxGroup.getValue()) > 0)
            {
                maxGroup = entry;
            }
        }
        return maxGroup.getKey();
    }
}

class Node{
    int id;
    int idNode;
    String ontology;
}
