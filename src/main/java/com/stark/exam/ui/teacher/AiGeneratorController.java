package com.stark.exam.ui.teacher;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import com.stark.exam.util.GeminiService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

public class AiGeneratorController {

    @FXML private ComboBox<String> examSelector;
    @FXML private ComboBox<String> subjectBox;
    @FXML private ComboBox<String> difficultyBox;
    @FXML private TextField countField;
    @FXML private Label statusLabel;
    @FXML private Button saveButton;

    // Table Components
    @FXML private TableView<GeminiService.GeneratedQuestion> previewTable;
    @FXML private TableColumn<GeminiService.GeneratedQuestion, String> colQ, colA, colB, colC, colD, colAns;

    private User user;
    private ObservableList<GeminiService.GeneratedQuestion> generatedList = FXCollections.observableArrayList();

    public void setUser(User user) {
        this.user = user;
        loadExams();
    }

    @FXML
    public void initialize() {
        // Setup Dropdowns
        subjectBox.getItems().addAll(
                "Engineering Economics", "Data Structures & Algorithms",
                "Customer Experience Design", "AWS Cloud Practitioner",
                "Machine Learning (Python)", "Software Engineering",
                "Java Programming", "Probability & Statistics",
                "Job Readiness", "Industrial IoT", "Climate Change"
        );
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");

        // Setup Table Columns
        colQ.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().question_text));
        colA.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().option_a));
        colB.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().option_b));
        colC.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().option_c));
        colD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().option_d));
        colAns.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().correct_answer));

        previewTable.setItems(generatedList);
    }

    private void loadExams() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, title FROM exams WHERE status = 'scheduled'");
            while (rs.next()) {
                examSelector.getItems().add(rs.getInt("id") + ": " + rs.getString("title"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleGenerate() {
        if (subjectBox.getValue() == null || countField.getText().isEmpty()) {
            statusLabel.setText("Please select Subject and Count.");
            return;
        }

        statusLabel.setText("⏳ Asking AI to generate questions... Please wait.");
        saveButton.setDisable(true);
        generatedList.clear();

        new Thread(() -> {
            try {
                String subject = subjectBox.getValue();
                String difficulty = difficultyBox.getValue();
                int count = Integer.parseInt(countField.getText());

                // 1. Fetch from AI
                List<GeminiService.GeneratedQuestion> aiQuestions =
                        GeminiService.generateQuestions(subject, difficulty, count);

                Platform.runLater(() -> {
                    if (aiQuestions.isEmpty()) {
                        statusLabel.setText("❌ AI Request Failed. Check Key/Internet.");
                    } else {
                        // 2. Show in Table
                        generatedList.setAll(aiQuestions);
                        statusLabel.setText("✅ Generated! Review the questions below and click 'Upload'.");
                        saveButton.setDisable(false); // Enable Save button
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void handleSaveToDB() {
        if (examSelector.getValue() == null) {
            statusLabel.setText("⚠️ Please select a TARGET EXAM before uploading.");
            return;
        }
        if (generatedList.isEmpty()) return;

        try (Connection conn = DBConnection.getConnection()) {
            int examId = Integer.parseInt(examSelector.getValue().split(":")[0].trim());
            String sql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES (?,?,?,?,?,?,?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);

            for (GeminiService.GeneratedQuestion q : generatedList) {
                pstmt.setInt(1, examId);
                pstmt.setString(2, q.question_text);
                pstmt.setString(3, q.option_a);
                pstmt.setString(4, q.option_b);
                pstmt.setString(5, q.option_c);
                pstmt.setString(6, q.option_d);
                pstmt.setString(7, q.correct_answer);
                pstmt.addBatch();
            }
            pstmt.executeBatch();

            statusLabel.setText("🎉 Successfully Uploaded " + generatedList.size() + " Questions to Exam ID: " + examId);
            saveButton.setDisable(true);
            generatedList.clear(); // Clear table after save

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Database Error: " + e.getMessage());
        }
    }
}
