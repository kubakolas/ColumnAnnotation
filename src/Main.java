import org.apache.jena.query.*;

public class Main {
    public static void main(String args[]) {
        ParameterizedSparqlString qs = new ParameterizedSparqlString(""
                + "prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX dbo:     <http://dbpedia.org/ontology/>"
                + "\n"
                + "select distinct ?resource ?abstract where {\n"
                + "  ?resource rdfs:label 'Ibuprofen'@en.\n"
                + "  ?resource dbo:abstract ?abstract.\n"
                + "  FILTER (lang(?abstract) = 'en')}");
        QueryExecution exec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", qs.asQuery());
        ResultSet results = exec.execSelect();
        while (results.hasNext()) {
            System.out.println(results.next().get("abstract").toString());
        }
        ResultSetFormatter.out(results);

//        Model model = ModelFactory.createDefaultModel();
//        String inputFileName = "/Users/jakubkolasinski/Desktop/instance_types_en.nt";
//        InputStream in = FileManager.get().open( inputFileName );
//        if (in == null) {
//            throw new IllegalArgumentException(
//                    "File: " + inputFileName + " not found");
//        }
//        model.read(in, null, "N-TRIPLES");
    }
}
