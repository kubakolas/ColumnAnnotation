import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class AnnotationsEvaluator {

    /** Metoda sprawdzająca poprawność wyników algorytmu eksperckiego z poprawnymi przyporządkowaniami.
     * @return procentowy wynik poprawnych adnotacji algorytmu eksperckiego
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


    /** Metoda czytająca z pliku poprawne przyporządkowanie klasy DBpedii dla zadanej tabeli.
     * @param tableName nazwa tabeli
     * @return wartosci komórek z poprawną klasą dla adnotowanej kolumny
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
