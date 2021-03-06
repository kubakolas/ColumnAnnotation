import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AnnotationsEvaluator {

    /** A method that checks the compliance of expert algorithm results with the correct assignments.
     * @return percentage result of the correct annotations of the expert algorithm
     */
    public float evaluate() {
        var gt = getColumnItems("gt");
        var annotated = getColumnItems("annotations");
        Integer correctCtr = 0;
        for(int i=0; i<annotated.size(); i++) {
            if (gt.get(i).equals(annotated.get(i))) {
                correctCtr += 1;
            }
        }
        return (float) correctCtr/annotated.size();
    }


    /** A method that reads ground truth for every column to be annotated.
     * @param tableName name of file containing table with ground truth
     * @return ground truth annotations for columns
     */
    private List<String> getColumnItems(String tableName) {
        String path = tableName + ".csv";
        List<String> columnItems = new ArrayList<>();
        try {
            FileReader filereader = new FileReader(path);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(0).build();
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                columnItems.add(nextRecord[2]);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return columnItems;
    }
}
