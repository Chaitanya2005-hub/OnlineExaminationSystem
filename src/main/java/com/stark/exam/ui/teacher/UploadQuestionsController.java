package com.stark.exam.ui.teacher;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UploadQuestionsController {

    @FXML private ComboBox<String> examSelector;
    @FXML private TextArea questionText;
    @FXML private TextField optA, optB, optC, optD;
    @FXML private ComboBox<String> correctAns;
    @FXML private Label statusLabel;

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadExams();
    }

    @FXML
    public void initialize() {
        System.out.println("DEBUG: Initializing UploadQuestionsController...");
        correctAns.setItems(FXCollections.observableArrayList("A", "B", "C", "D"));
    }

    private void loadExams() {
        examSelector.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("DEBUG: Loading exams from DB...");
            String query = "SELECT id, title FROM exams WHERE status = 'scheduled'";
            ResultSet rs = conn.createStatement().executeQuery(query);

            boolean found = false;
            while (rs.next()) {
                String item = rs.getInt("id") + ": " + rs.getString("title");
                examSelector.getItems().add(item);
                System.out.println("DEBUG: Found exam -> " + item);
                found = true;
            }

            if (!found) {
                System.out.println("DEBUG: No scheduled exams found.");
                statusLabel.setText("No scheduled exams found (Create one in Admin first).");
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("DB Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpload() {
        System.out.println("DEBUG: Upload Button Clicked!");

        // 1. Validation
        if (examSelector.getValue() == null) {
            statusLabel.setText("❌ Select an exam first.");
            System.out.println("DEBUG: Fail - No exam selected.");
            return;
        }
        if (questionText.getText().trim().isEmpty()) {
            statusLabel.setText("❌ Question text is empty.");
            System.out.println("DEBUG: Fail - Empty question.");
            return;
        }
        if (correctAns.getValue() == null) {
            statusLabel.setText("❌ Select correct answer.");
            System.out.println("DEBUG: Fail - No correct answer.");
            return;
        }

        try {
            // 2. Parse ID
            String selected = examSelector.getValue();
            int examId = Integer.parseInt(selected.split(":")[0].trim());
            System.out.println("DEBUG: Uploading to Exam ID: " + examId);

            // 3. Insert
            try (Connection conn = DBConnection.getConnection()) {
                String query = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, examId);
                pstmt.setString(2, questionText.getText().trim());
                pstmt.setString(3, optA.getText().trim());
                pstmt.setString(4, optB.getText().trim());
                pstmt.setString(5, optC.getText().trim());
                pstmt.setString(6, optD.getText().trim());
                pstmt.setString(7, correctAns.getValue());

                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    System.out.println("DEBUG: Success! Rows affected: " + rows);
                    statusLabel.setText("✅ Question Saved!");
                    statusLabel.setStyle("-fx-text-fill: green;");
                    clearFields();
                } else {
                    System.out.println("DEBUG: Fail - SQL executed but no rows added.");
                    statusLabel.setText("❌ DB Error: Save failed.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void clearFields() {
        questionText.clear();
        optA.clear(); optB.clear(); optC.clear(); optD.clear();
        correctAns.getSelectionModel().clearSelection();
    }
}
