package com.stark.exam.ui.author;

import com.stark.exam.db.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.sql.Connection;
import java.sql.ResultSet;

public class SystemReportsController {

    @FXML private Label lblTotalStudents, lblTotalTeachers, lblTotalExams;
    @FXML private PieChart passFailChart;
    @FXML private BarChart<String, Number> sectionChart; // <--- NEW CHART

    @FXML
    public void initialize() {
        loadStats();
        loadPassFailRatio();
        loadSectionPerformance(); // <--- NEW METHOD
    }

    private void loadStats() {
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users WHERE role='student'");
            if (rs1.next()) lblTotalStudents.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM users WHERE role='teacher'");
            if (rs2.next()) lblTotalTeachers.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM exams");
            if (rs3.next()) lblTotalExams.setText(String.valueOf(rs3.getInt(1)));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPassFailRatio() {
        int passed = 0;
        int failed = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT score, total_marks FROM results";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                int score = rs.getInt("score");
                int total = rs.getInt("total_marks");
                // Avoid divide by zero
                if (total == 0) continue;
                double percentage = (double) score / total * 100;

                if (percentage >= 40) passed++;
                else failed++;
            }
            passFailChart.getData().clear();
            passFailChart.getData().add(new PieChart.Data("Passed", passed));
            passFailChart.getData().add(new PieChart.Data("Failed", failed));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- NEW: Calculate Average Score per Section ---
    private void loadSectionPerformance() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg Score %");

        try (Connection conn = DBConnection.getConnection()) {
            // SQL to get Avg Percentage per Section
            String query = "SELECT u.section, AVG((r.score / r.total_marks) * 100) as avg_pct " +
                    "FROM results r " +
                    "JOIN users u ON r.student_id = u.id " +
                    "GROUP BY u.section " +
                    "ORDER BY u.section";

            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                String sec = rs.getString("section");
                double avg = rs.getDouble("avg_pct");
                // Handle null sections if any
                if (sec == null) sec = "Unknown";

                series.getData().add(new XYChart.Data<>("Section " + sec, avg));
            }
            sectionChart.getData().clear();
            sectionChart.getData().add(series);

        } catch (Exception e) { e.printStackTrace(); }
    }
}
