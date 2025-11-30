package com.stark.exam.ui.teacher;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentProgressController {

    @FXML private TextField searchField;
    @FXML private BarChart<String, Number> performanceChart;
    @FXML private Label statusLabel;

    private User teacher; // Current logged-in teacher

    public void setUser(User user) {
        this.teacher = user;
    }

    @FXML
    private void handleSearch() {
        String erpId = searchField.getText();
        if (erpId.isEmpty()) {
            statusLabel.setText("Please enter a Student ERP ID / Roll No.");
            return;
        }

        performanceChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Scores for " + erpId);

        boolean found = false;

        try (Connection conn = DBConnection.getConnection()) {
            // Join results, exams, and users to get scores for a specific student
            String query = "SELECT e.title, r.score FROM results r " +
                    "JOIN exams e ON r.exam_id = e.id " +
                    "JOIN users u ON r.student_id = u.id " +
                    "WHERE u.erp_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, erpId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                found = true;
                series.getData().add(new XYChart.Data<>(rs.getString("title"), rs.getInt("score")));
            }

            if (found) {
                performanceChart.getData().add(series);
                statusLabel.setText("Data loaded for: " + erpId);
            } else {
                statusLabel.setText("No results found for student ID: " + erpId);
            }

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Database Error");
        }
    }
}
