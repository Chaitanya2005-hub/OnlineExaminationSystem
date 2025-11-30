package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ExamListController {

    @FXML private VBox examContainer;
    private User user;
    private Timeline approvalTimer;

    public void setUser(User user) {
        this.user = user;
        loadActiveExams();
    }

    private void loadActiveExams() {
        examContainer.getChildren().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM exams WHERE status = 'scheduled'";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                int examId = rs.getInt("id");
                String title = rs.getString("title");
                int duration = rs.getInt("duration_minutes");

                Button btn = new Button();
                btn.setMaxWidth(Double.MAX_VALUE);

                String status = getRequestStatus(examId);

                if (status.equals("Approved")) {
                    btn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-cursor: hand;");
                    btn.setText(title + " (" + duration + " mins)  [START NOW]");
                    btn.setOnAction(e -> startExam(examId, title, duration));
                } else if (status.equals("Requested")) {
                    btn.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-padding: 15;");
                    btn.setText(title + "  [WAITING FOR ADMIN APPROVAL...]");
                    btn.setDisable(true);
                    // Start polling to see if admin approves
                    if (approvalTimer == null) startPollingForApproval();
                } else {
                    btn.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-padding: 15; -fx-cursor: hand;");
                    btn.setText(title + " (" + duration + " mins)  [REQUEST ACCESS]");
                    btn.setOnAction(e -> requestAccess(examId));
                }

                examContainer.getChildren().add(btn);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String getRequestStatus(int examId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT status FROM exam_requests WHERE student_id = ? AND exam_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, examId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (Exception e) { e.printStackTrace(); }
        return "None";
    }

    private void requestAccess(int examId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO exam_requests (student_id, exam_id, status) VALUES (?, ?, 'Requested')";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, examId);
            pstmt.executeUpdate();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Request Sent");
            alert.setContentText("Request sent to Admin. Please wait for approval.");
            alert.showAndWait();

            loadActiveExams(); // Refresh UI immediately
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startPollingForApproval() {
        approvalTimer = new Timeline(new KeyFrame(Duration.seconds(3), e -> loadActiveExams()));
        approvalTimer.setCycleCount(Timeline.INDEFINITE);
        approvalTimer.play();
    }

    private void startExam(int examId, String title, int duration) {
        if (approvalTimer != null) approvalTimer.stop(); // Stop polling
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam_interface.fxml"));
            Parent root = loader.load();
            ExamInterfaceController controller = loader.getController();

            Stage examStage = new Stage();
            examStage.setTitle("EXAM: " + title);
            examStage.setScene(new Scene(root));
            examStage.setFullScreen(true);

            controller.setupExam(this.user, examId, title, duration, examStage);

            ((Stage) examContainer.getScene().getWindow()).close();
            examStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
