package com.stark.exam.ui.teacher;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManageQuestionsController {

    @FXML private ComboBox<String> examSelector;
    @FXML private TableView<QuestionRow> questionTable;
    @FXML private TableColumn<QuestionRow, Number> colId;
    @FXML private TableColumn<QuestionRow, String> colText;
    @FXML private TableColumn<QuestionRow, String> colAns;

    // Edit Fields
    @FXML private TextArea qTextField;
    @FXML private TextField optA, optB, optC, optD;
    @FXML private ComboBox<String> correctBox;
    @FXML private Label statusLabel;

    private User user;
    private int selectedQuestionId = -1;

    public void setUser(User user) {
        this.user = user;
        loadExams();
    }

    @FXML
    public void initialize() {
        correctBox.getItems().addAll("A", "B", "C", "D");

        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().id));
        colText.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().text));
        colAns.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().ans));

        // Handle Table Selection
        questionTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateFields(newVal);
        });
    }

    private void loadExams() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT id, title FROM exams");
            while (rs.next()) {
                examSelector.getItems().add(rs.getInt("id") + ": " + rs.getString("title"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLoadQuestions() {
        if (examSelector.getValue() == null) return;
        int examId = Integer.parseInt(examSelector.getValue().split(":")[0].trim());

        ObservableList<QuestionRow> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM questions WHERE exam_id = ?";
            PreparedStatement p = conn.prepareStatement(query);
            p.setInt(1, examId);
            ResultSet rs = p.executeQuery();

            while (rs.next()) {
                list.add(new QuestionRow(
                        rs.getInt("id"),
                        rs.getString("question_text"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        rs.getString("correct_answer")
                ));
            }
            questionTable.setItems(list);
            statusLabel.setText("Loaded " + list.size() + " questions.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void populateFields(QuestionRow q) {
        selectedQuestionId = q.id;
        qTextField.setText(q.text);
        optA.setText(q.a);
        optB.setText(q.b);
        optC.setText(q.c);
        optD.setText(q.d);
        correctBox.setValue(q.ans);
    }

    @FXML
    private void handleUpdate() {
        if (selectedQuestionId == -1) {
            statusLabel.setText("Select a question to update.");
            return;
        }
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_answer=? WHERE id=?";
            PreparedStatement p = conn.prepareStatement(sql);
            p.setString(1, qTextField.getText());
            p.setString(2, optA.getText());
            p.setString(3, optB.getText());
            p.setString(4, optC.getText());
            p.setString(5, optD.getText());
            p.setString(6, correctBox.getValue());
            p.setInt(7, selectedQuestionId);

            p.executeUpdate();
            statusLabel.setText("✅ Question Updated Successfully!");
            handleLoadQuestions(); // Refresh table
        } catch (Exception e) { statusLabel.setText("Error: " + e.getMessage()); }
    }

    @FXML
    private void handleDelete() {
        if (selectedQuestionId == -1) return;
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement p = conn.prepareStatement("DELETE FROM questions WHERE id=?");
            p.setInt(1, selectedQuestionId);
            p.executeUpdate();
            statusLabel.setText("🗑️ Question Deleted.");
            handleLoadQuestions();
            clearFields();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void clearFields() {
        qTextField.clear(); optA.clear(); optB.clear(); optC.clear(); optD.clear(); selectedQuestionId = -1;
    }

    public static class QuestionRow {
        int id; String text, a, b, c, d, ans;
        public QuestionRow(int id, String t, String a, String b, String c, String d, String ans) {
            this.id = id; this.text = t; this.a = a; this.b = b; this.c = c; this.d = d; this.ans = ans;
        }
    }
}
