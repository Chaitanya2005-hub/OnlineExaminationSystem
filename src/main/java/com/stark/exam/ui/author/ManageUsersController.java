package com.stark.exam.ui.author;

import com.stark.exam.db.DBConnection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ManageUsersController {

    @FXML private TextField usernameField, passwordField, nameField, erpField, deptField, yearField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("student", "teacher", "author"));

        // Disable Year field if role is not student
        roleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            yearField.setDisable(!"student".equals(newVal));
        });
    }

    @FXML
    private void handleAddUser() {
        String role = roleCombo.getValue();
        if (role == null || usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            statusLabel.setText("Please fill all required fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO users (username, password, full_name, role, erp_id, department, year) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, usernameField.getText());
            pstmt.setString(2, passwordField.getText());
            pstmt.setString(3, nameField.getText());
            pstmt.setString(4, role);
            pstmt.setString(5, erpField.getText());
            pstmt.setString(6, deptField.getText());

            if ("student".equals(role) && !yearField.getText().isEmpty()) {
                pstmt.setInt(7, Integer.parseInt(yearField.getText()));
            } else {
                pstmt.setObject(7, null);
            }

            pstmt.executeUpdate();
            statusLabel.setText("✅ User Added Successfully!");
            clearFields();

            // If student, create a blocked admit card entry automatically
            if ("student".equals(role)) createAdmitCardEntry(erpField.getText());

        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void createAdmitCardEntry(String erpId) {
        try (Connection conn = DBConnection.getConnection()) {
            // Get ID of newly added user
            String getId = "SELECT id FROM users WHERE erp_id = ?";
            PreparedStatement getStmt = conn.prepareStatement(getId);
            getStmt.setString(1, erpId);
            var rs = getStmt.executeQuery();
            if (rs.next()) {
                String insertCard = "INSERT INTO admit_cards (student_id, status) VALUES (?, 'Blocked')";
                PreparedStatement cardStmt = conn.prepareStatement(insertCard);
                cardStmt.setInt(1, rs.getInt("id"));
                cardStmt.executeUpdate();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearFields() {
        usernameField.clear(); passwordField.clear(); nameField.clear();
        erpField.clear(); deptField.clear(); yearField.clear();
    }
}
