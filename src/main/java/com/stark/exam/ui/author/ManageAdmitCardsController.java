package com.stark.exam.ui.author;

import com.stark.exam.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageAdmitCardsController {

    @FXML private TableView<AdmitCardData> cardTable;
    @FXML private TableColumn<AdmitCardData, String> colName;
    @FXML private TableColumn<AdmitCardData, String> colRoll;
    @FXML private TableColumn<AdmitCardData, String> colStatus;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        colRoll.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().roll));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));
        loadData();
    }

    private void loadData() {
        ObservableList<AdmitCardData> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT u.full_name, u.erp_id, ac.status, ac.id " +
                    "FROM admit_cards ac JOIN users u ON ac.student_id = u.id";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                list.add(new AdmitCardData(rs.getInt("id"), rs.getString("full_name"), rs.getString("erp_id"), rs.getString("status")));
            }
            cardTable.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleRelease() { updateStatus("Released"); }

    @FXML
    private void handleBlock() { updateStatus("Blocked"); }

    private void updateStatus(String newStatus) {
        AdmitCardData selected = cardTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select a student first!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE admit_cards SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, selected.id);
            pstmt.executeUpdate();

            statusLabel.setText("Status updated to: " + newStatus);
            loadData(); // Refresh table
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class AdmitCardData {
        int id;
        String name, roll, status;
        public AdmitCardData(int id, String n, String r, String s) { this.id = id; this.name = n; this.roll = r; this.status = s; }
    }
}
