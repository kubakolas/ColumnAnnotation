package columnannotation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    private final String DELIMITER = ",";
    private String path = "first";

    @FXML
    Label accuracyLabel;
    @FXML
    Button countButton;
    @FXML
    ListView<String> listView;
    @FXML
    TableView<Record> ontologyTable;
    @FXML
    Label annotationLabel;
    @FXML
    Button refreshButton;

    private TableUtil tableUtil;

    private final TableView<Record> tableView = new TableView<>();
    private final ObservableList<Record> annotationsDataList = FXCollections.observableArrayList();
    private final ObservableList<Record> tableDataList = FXCollections.observableArrayList();
    private ObservableList<String> list = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        init();
        setupListViewOnClick();
        tableUtil = new TableUtil(ontologyTable);
    }

    private void init() {
        list.clear();
        annotationsDataList.clear();
        tableDataList.clear();
        tableView.getItems().clear();
        readCSV();
        listView.setItems(list);
        getAccuracy();
    }

    private void setupListViewOnClick() {
        listView.setOnMouseClicked(event -> {
            tableUtil.clearTable();
            String[] clickedItem = listView.getSelectionModel().getSelectedItem().split(DELIMITER, -1);
            String annotation = "";
            for (Record record : annotationsDataList) {
                String f1 = record.getF1();
                String f2 = record.getF2();
                if (f1.equals(clickedItem[0]) && f2.equals(clickedItem[1])) {
                    annotation = record.getF3();
                    break;
                }
            }
            annotationLabel.setText("Annotation: " + annotation);
            setupTableView(clickedItem[0], clickedItem[1]);
        });
    }

    private void setupTableView(String annotation, String column) {
        String CsvFile = path + "\\data\\" + annotation + ".csv";
        BufferedReader br;
        tableDataList.clear();
        tableView.getItems().clear();
        try {
            br = new BufferedReader(new FileReader(CsvFile));
            String line;
            while ((line = br.readLine()) != null) {
                createRecord(line);
            }
        } catch (IOException ignored) {
        }
        tableUtil.clearTable();
        tableUtil.addColumns(tableDataList, column);
    }

    private void createRecord(String line) {
        String[] fields = line.split(DELIMITER, -1);
        String f1 = "", f2 = "", f3 = "", f4 = "", f5 = "", f6 = "", f7 = "", f8 = "";
        for (int i = 0; i < fields.length; i++) {
            if (i == 0) {
                f1 = fields[i];
            } else if (i == 1) {
                f2 = fields[i];
            } else if (i == 2) {
                f3 = fields[i];
            } else if (i == 3) {
                f4 = fields[i];
            } else if (i == 4) {
                f5 = fields[i];
            } else if (i == 5) {
                f6 = fields[i];
            } else if (i == 6) {
                f7 = fields[i];
            } else if (i == 7) {
                f8 = fields[i];
            }
        }
        Record record = new Record(f1, f2, f3, f4, f5, f6, f7, f8);
        tableDataList.add(record);
    }

    private void readCSV() {
        String CsvFile = path + "\\annotations.csv";
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(CsvFile));
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(DELIMITER, -1);
                String f1, f2, f3, f4 = "", f5 = "", f6 = "", f7 = "", f8 = "";
                if (fields[0] != null) f1 = fields[0];
                else f1 = "";
                if (fields[1] != null) f2 = fields[1];
                else f2 = "";
                if (fields[2] != null) f3 = fields[2];
                else f3 = "";
                Record record = new Record(f1, f2, f3, f4, f5, f6, f7, f8);
                annotationsDataList.add(record);
                list.add(f1 + "," + f2);
            }
        } catch (IOException ignored) {
        }
    }

    private void getAccuracy() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(path + "\\accuracy.txt").toAbsolutePath());
            String accuracy = new String(bytes);
            accuracyLabel.setText("accuracy: " + accuracy);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void countAgain() throws IOException {
        Runtime.getRuntime().exec(new String[]{"cmd", "/K", "Start", "first\\ColumnAnnotation.exe"});
    }

    public void refreshTable() {
        init();
    }
}
