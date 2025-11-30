package com.stark.exam.ui.author;

import com.stark.exam.model.User;
import com.stark.exam.ui.teacher.*; // Import Teacher Controllers to reuse them
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthorDashboardController {

    @FXML private Label adminNameLabel;
    @FXML private FlowPane menuGrid;

    private User user;

    public void setUser(User user) {
        this.user = user;
        adminNameLabel.setText(user.getFullName().toUpperCase());
        loadMenuOptions();
    }

    private void loadMenuOptions() {
        menuGrid.getChildren().clear();

        // --- ADMIN SPECIFIC MODULES ---
        addMenuItem("Schedule Exam", "📅", () -> openWindow("/fxml/schedule_exam.fxml", "Exam Scheduler"));
        addMenuItem("Post Notice", "📢", () -> openWindow("/fxml/post_notice.fxml", "Announcements"));
        addMenuItem("Manage Users", "👥", () -> openWindow("/fxml/manage_users.fxml", "User Management"));
        addMenuItem("Admit Cards", "🎫", () -> openWindow("/fxml/manage_admit_cards.fxml", "Admit Cards"));
        addMenuItem("Fee Manager", "💰", () -> openWindow("/fxml/admin_fees.fxml", "Fee Approvals"));

        // --- QUESTION MANAGEMENT ---
        addMenuItem("Upload Questions", "📝", () -> openWindow("/fxml/upload_questions.fxml", "Upload Questions"));
        addMenuItem("Manage Questions", "✏️", () -> openWindow("/fxml/manage_questions.fxml", "Edit/Delete Questions"));

        // --- MONITORING & REPORTS ---
        addMenuItem("Live Monitor", "🔴", () -> openWindow("/fxml/live_exam_monitor.fxml", "Live Exam Monitor"));
        addMenuItem("Check Attendance", "📋", () -> openWindow("/fxml/admin_attendance.fxml", "Attendance Report"));
        addMenuItem("View Results", "📊", () -> openWindow("/fxml/exam_results.fxml", "Exam Results"));
        addMenuItem("Assignments", "📂", () -> openWindow("/fxml/teacher_assignment.fxml", "Assignment Manager"));

        // --- SYSTEM ---
        addMenuItem("Logout", "🚪", this::logout);
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Pass User Data to Controllers (Linking logic)
            Object controller = loader.getController();

            // Admin Controllers
            if (controller instanceof PostNoticeController) {
                ((PostNoticeController) controller).setUser(this.user);
            }
            // Teacher Controllers (Reused by Admin)
            else if (controller instanceof ManageQuestionsController) {
                ((ManageQuestionsController) controller).setUser(this.user);
            }
            else if (controller instanceof UploadQuestionsController) {
                ((UploadQuestionsController) controller).setUser(this.user);
            }
            else if (controller instanceof ExamResultsController) {
                ((ExamResultsController) controller).setUser(this.user);
            }
            else if (controller instanceof AssignmentController) {
                ((AssignmentController) controller).setUser(this.user);
            }
            else if (controller instanceof MarkAttendanceController) {
                ((MarkAttendanceController) controller).setUser(this.user);
            }
            // Controllers that don't require User object
            else if (controller instanceof LiveExamController) { /* Logic handled internally */ }
            else if (controller instanceof AdminAttendanceController) { /* Logic handled internally */ }

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Blocks main window until closed
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load module: " + fxmlPath + "\nCheck console for details.");
        }
    }

    private void logout() {
        try {
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addMenuItem(String title, String icon, Runnable action) {
        Button btn = new Button();
        btn.setPrefSize(100, 100);
        // Purple/Indigo Theme for Admin buttons
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand; -fx-border-color: #ede7f6; -fx-border-radius: 15;");

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);
        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 30px;");

        Label textLbl = new Label(title);
        textLbl.setWrapText(true);
        textLbl.setFont(Font.font("System", 11));
        textLbl.setAlignment(Pos.CENTER);

        content.getChildren().addAll(iconLbl, textLbl);

        btn.setGraphic(content);
        btn.setOnAction(e -> action.run());
        menuGrid.getChildren().add(btn);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
