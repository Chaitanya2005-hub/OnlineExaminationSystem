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

public class ManageSubjectsController {

    @FXML private TextField nameField, codeField, deptField;
    @FXML private TableView<Subject> subjectTable;
    @FXML private TableColumn<Subject, String> colName;
    @FXML private TableColumn<Subject, String> colCode;
    @FXML private TableColumn<Subject, String> colDept;
    @FXML private Label statusLabel;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().name));
        colCode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().code));
        colDept.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().dept));
        loadSubjects();
    }

    @FXML
    private void handleAdd() {
        if (nameField.getText().isEmpty() || codeField.getText().isEmpty()) {
            statusLabel.setText("Please fill all fields.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO subjects (name, code, department) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, nameField.getText());
            pstmt.setString(2, codeField.getText());
            pstmt.setString(3, deptField.getText());
            pstmt.executeUpdate();

            statusLabel.setText("✅ Subject Added!");
            nameField.clear(); codeField.clear(); deptField.clear();
            loadSubjects(); // Refresh table
        } catch (Exception e) { statusLabel.setText("Error: " + e.getMessage()); }
    }

    private void loadSubjects() {
        ObservableList<Subject> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM subjects");
            while (rs.next()) {
                list.add(new Subject(rs.getString("name"), rs.getString("code"), rs.getString("department")));
            }
            subjectTable.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class Subject {
        String name, code, dept;
        public Subject(String n, String c, String d) { this.name = n; this.code = c; this.dept = d; }
    }
}
