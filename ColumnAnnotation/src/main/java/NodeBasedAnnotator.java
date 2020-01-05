import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.*;
import static java.util.stream.Collectors.groupingBy;

public class NodeBasedAnnotator implements Annotator {
    public String getAnnotation(Map<String, List<String>> itemWithClasses) {
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

        if(maxOntology != null) { return maxOntology.getKey(); }
        else { return null; }
    }
}
