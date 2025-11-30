package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField; // Fixed Import

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;

public class AttendanceController {
    @FXML private Label lblTotal, lblPresent, lblAbsent;
    @FXML private PieChart attendanceChart;
    @FXML private TextField codeField; // Fixed Definition

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadAttendance();
    }

    private void loadAttendance() {
        int present = 0, absent = 0;
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT status, COUNT(*) as count FROM attendance WHERE student_id = ? GROUP BY status";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                if (rs.getString("status").equals("Present")) present = rs.getInt("count");
                else absent = rs.getInt("count");
            }

            lblPresent.setText(String.valueOf(present));
            lblAbsent.setText(String.valueOf(absent));
            lblTotal.setText(String.valueOf(present + absent));

            attendanceChart.getData().clear();
            attendanceChart.getData().add(new PieChart.Data("Present", present));
            attendanceChart.getData().add(new PieChart.Data("Absent", absent));

        } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML
    private void handleMarkSelfAttendance() {
        String inputCode = codeField.getText();

        if (inputCode == null || inputCode.isEmpty()) {
            showAlert("Error", "Please enter the code.");
            return;
        }

        // 1. Verify Code from Database
        boolean isCodeValid = false;
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT code FROM live_codes WHERE id = 1";
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                String activeCode = rs.getString("code");
                if (activeCode.equals(inputCode)) {
                    isCodeValid = true;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 2. Mark Attendance if Valid
        if (isCodeValid) {
            saveAttendance("Present");
            showAlert("Success", "✅ Attendance Marked! Code Matched.");
            codeField.clear();
            loadAttendance();
        } else {
            showAlert("Failed", "❌ Invalid or Expired Code! Try again.");
        }
    }




    // Fixed: Helper Method to Save to DB
    private void saveAttendance(String status) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO attendance (student_id, date, status, marked_by) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            pstmt.setString(3, status);
            pstmt.setInt(4, user.getId()); // Self-marked
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage());
        }
    }

    // Fixed: Helper Method for Alerts
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
