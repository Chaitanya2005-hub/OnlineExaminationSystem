package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class PerformanceController {
    @FXML private LineChart<String, Number> cgpaChart;
    private User user;

    public void setUser(User user) {
        this.user = user;
        loadChart();
    }

    private void loadChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Exam Scores");

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT e.title, r.score FROM results r JOIN exams e ON r.exam_id = e.id WHERE r.student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                series.getData().add(new XYChart.Data<>(rs.getString("title"), rs.getInt("score")));
            }
        } catch (Exception e) { e.printStackTrace(); }

        cgpaChart.getData().add(series);
    }
}
