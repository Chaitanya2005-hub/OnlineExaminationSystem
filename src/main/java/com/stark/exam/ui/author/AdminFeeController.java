package com.stark.exam.ui.author;

import com.stark.exam.db.DBConnection;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.fxml.FXML;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class AdminFeeController {

    @FXML private TableView<FeeRecord> feeTable;
    @FXML private TableColumn<FeeRecord, String> colName;
    @FXML private TableColumn<FeeRecord, Number> colTotal; // NEW
    @FXML private TableColumn<FeeRecord, Number> colPaid;
    @FXML private TableColumn<FeeRecord, String> colStatus;
    @FXML private TableColumn<FeeRecord, Void> colActions; // NEW

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        colTotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().total));
        colPaid.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().paid));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));

        addActionsColumn();
        loadData();
    }

    private void loadData() {
        ObservableList<FeeRecord> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT u.id, u.full_name, f.total_fee, f.paid_fee, f.status FROM fees f JOIN users u ON f.student_id = u.id";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()) {
                data.add(new FeeRecord(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getDouble("total_fee"),
                        rs.getDouble("paid_fee"),
                        rs.getString("status")
                ));
            }
            feeTable.setItems(data);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addActionsColumn() {
        Callback<TableColumn<FeeRecord, Void>, TableCell<FeeRecord, Void>> cellFactory = param -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️ Edit");
            private final Button btnApprove = new Button("✅ Approve");
            private final HBox pane = new HBox(10, btnEdit, btnApprove);

            {
                btnEdit.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-cursor: hand;");
                btnApprove.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");

                btnEdit.setOnAction(event -> {
                    FeeRecord record = getTableView().getItems().get(getIndex());
                    showEditDialog(record);
                });

                btnApprove.setOnAction(event -> {
                    FeeRecord record = getTableView().getItems().get(getIndex());
                    approveFee(record.id);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FeeRecord record = getTableView().getItems().get(getIndex());
                    // Hide "Approve" button if already approved
                    btnApprove.setVisible(!record.status.equals("Approved"));
                    setGraphic(pane);
                }
            }
        };
        colActions.setCellFactory(cellFactory);
    }

    private void showEditDialog(FeeRecord record) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Fee Details");
        dialog.setHeaderText("Edit Fees for: " + record.name);

        // Set the button types
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Create labels and fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField totalField = new TextField(String.valueOf(record.total));
        TextField paidField = new TextField(String.valueOf(record.paid));

        grid.add(new Label("Total Fee:"), 0, 0);
        grid.add(totalField, 1, 0);
        grid.add(new Label("Paid Amount:"), 0, 1);
        grid.add(paidField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a pair when the login button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    double newTotal = Double.parseDouble(totalField.getText());
                    double newPaid = Double.parseDouble(paidField.getText());
                    updateFeeInDB(record.id, newTotal, newPaid);
                } catch (NumberFormatException e) {
                    showAlert("Invalid Input", "Please enter valid numbers.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateFeeInDB(int id, double total, double paid) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE fees SET total_fee = ?, paid_fee = ? WHERE student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, total);
            pstmt.setDouble(2, paid);
            pstmt.setInt(3, id);
            pstmt.executeUpdate();
            loadData(); // Refresh UI
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void approveFee(int studentId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.createStatement().executeUpdate("UPDATE fees SET status = 'Approved' WHERE student_id = " + studentId);
            loadData();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static class FeeRecord {
        int id; String name, status; double total, paid;
        public FeeRecord(int i, String n, double t, double p, String s) {
            this.id=i; this.name=n; this.total=t; this.paid=p; this.status=s;
        }
        // Getters needed for PropertyValueFactory
        public String getName() { return name; }
        public double getTotal() { return total; }
        public double getPaid() { return paid; }
        public String getStatus() { return status; }
    }
}
