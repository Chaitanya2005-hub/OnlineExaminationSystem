package com.stark.exam.ui.author;

import com.stark.exam.db.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ScheduleExamController {
    @FXML private TextField examTitle, examTime, examDuration;
    @FXML private DatePicker examDate;
    @FXML private Label statusLabel;

    @FXML
    private void handleCreate() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO exams (title, exam_date, start_time, duration_minutes, status) VALUES (?, ?, ?, ?, 'scheduled')";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, examTitle.getText());
            pstmt.setDate(2, java.sql.Date.valueOf(examDate.getValue()));
            pstmt.setTime(3, java.sql.Time.valueOf(examTime.getText()));
            pstmt.setInt(4, Integer.parseInt(examDuration.getText()));
            pstmt.executeUpdate();
            statusLabel.setText("✅ Exam Scheduled!");
        } catch (Exception e) { statusLabel.setText("❌ Error: " + e.getMessage()); }
    }
}
