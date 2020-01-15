package columnannotation;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

class TableUtil {

    TableUtil(TableView<Record> table) {
        this.table = table;
        this.table.setSelectionModel(null);
    }

    private TableView<Record> table;
    private TableColumn<Record, String> columnF1, columnF2, columnF3, columnF4, columnF5, columnF6, columnF7, columnF8;

    void clearTable() {
        table.getColumns().clear();
    }

    void addColumns(ObservableList<Record> tableDataList, String column) {
        createColumns();
        Record first = tableDataList.get(0);
        tableDataList.remove(first);
        setTitles(first);
        setMaxWidth();
        addColumnsToTable();
        table.setItems(tableDataList);
        colorCurrentColumn(column);
    }

    private void colorCurrentColumn(String column) {
        String style = "-fx-background-color: cornsilk;";
        if ("0".equals(column)) {
            columnF1.setStyle(style);
        } else if ("1".equals(column)) {
            columnF2.setStyle(style);
        } else if ("2".equals(column)) {
            columnF3.setStyle(style);
        } else if ("3".equals(column)) {
            columnF4.setStyle(style);
        } else if ("4".equals(column)) {
            columnF5.setStyle(style);
        } else if ("5".equals(column)) {
            columnF6.setStyle(style);
        } else if ("6".equals(column)) {
            columnF7.setStyle(style);
        } else if ("7".equals(column)) {
            columnF8.setStyle(style);
        }
    }

    private void addColumnsToTable() {
        table.getColumns().add(columnF1);
        table.getColumns().add(columnF2);
        table.getColumns().add(columnF3);
        table.getColumns().add(columnF4);
        table.getColumns().add(columnF5);
        table.getColumns().add(columnF6);
        table.getColumns().add(columnF7);
        table.getColumns().add(columnF8);
    }

    private void setMaxWidth() {
        int maxWidth = 150;
        columnF1.setMaxWidth(maxWidth);
        columnF2.setMaxWidth(maxWidth);
        columnF3.setMaxWidth(maxWidth);
        columnF4.setMaxWidth(maxWidth);
        columnF5.setMaxWidth(maxWidth);
        columnF6.setMaxWidth(maxWidth);
        columnF7.setMaxWidth(maxWidth);
        columnF8.setMaxWidth(maxWidth);
    }

    private void setTitles(Record first) {
        columnF1.setText(first.getF1());
        columnF2.setText(first.getF2());
        columnF3.setText(first.getF3());
        columnF4.setText(first.getF4());
        columnF5.setText(first.getF5());
        columnF6.setText(first.getF6());
        columnF7.setText(first.getF7());
        columnF8.setText(first.getF8());
    }

    private void createColumns() {
        columnF1 = new TableColumn<>("f1");
        columnF1.setCellValueFactory(new PropertyValueFactory<>("f1"));
        columnF2 = new TableColumn<>("f2");
        columnF2.setCellValueFactory(new PropertyValueFactory<>("f2"));
        columnF3 = new TableColumn<>("f3");
        columnF3.setCellValueFactory(new PropertyValueFactory<>("f3"));
        columnF4 = new TableColumn<>("f4");
        columnF4.setCellValueFactory(new PropertyValueFactory<>("f4"));
        columnF5 = new TableColumn<>("f5");
        columnF5.setCellValueFactory(new PropertyValueFactory<>("f5"));
        columnF6 = new TableColumn<>("f6");
        columnF6.setCellValueFactory(new PropertyValueFactory<>("f6"));
        columnF7 = new TableColumn<>("f7");
        columnF7.setCellValueFactory(new PropertyValueFactory<>("f7"));
        columnF8 = new TableColumn<>("f8");
        columnF8.setCellValueFactory(new PropertyValueFactory<>("f8"));
    }
}
