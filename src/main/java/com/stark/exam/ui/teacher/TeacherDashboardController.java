package com.stark.exam.ui.teacher;

import com.stark.exam.model.User;
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

public class TeacherDashboardController {

    @FXML private Label teacherNameLabel;
    @FXML private Label teacherIdLabel;
    @FXML private FlowPane menuGrid;

    private User user;

    public void setUser(User user) {
        this.user = user;
        teacherNameLabel.setText(user.getFullName().toUpperCase());
        teacherIdLabel.setText(user.getErpId());
        loadMenuOptions();
    }

    private void loadMenuOptions() {
        menuGrid.getChildren().clear();
        addMenuItem("Answer Sheets", "📄", () -> openWindow("/fxml/answer_sheet.fxml", "Student Answer Sheets"));
        addMenuItem("Live Monitor", "🔴", () -> openWindow("/fxml/live_exam_monitor.fxml", "Live Exam Monitor"));
        addMenuItem("View Results", "📊", () -> openWindow("/fxml/exam_results.fxml", "Exam Results"));
        addMenuItem("Assignments", "📂", () -> openWindow("/fxml/teacher_assignment.fxml", "Manage Assignments"));
        addMenuItem("Mark Attendance", "✅", () -> openWindow("/fxml/mark_attendance.fxml", "Mark Attendance"));
        addMenuItem("Upload Questions", "📝", () -> openWindow("/fxml/upload_questions.fxml", "Upload Questions"));
        addMenuItem("Manage Questions", "✏️", () -> openWindow("/fxml/manage_questions.fxml", "Edit/Delete Questions"));
        addMenuItem("Logout", "🚪", this::logout);
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            // NEW: Handle ManageQuestionsController
            if (controller instanceof ManageQuestionsController) {
                ((ManageQuestionsController) controller).setUser(this.user);
            }
            else if (controller instanceof UploadQuestionsController) ((UploadQuestionsController) controller).setUser(this.user);
            else if (controller instanceof AssignmentController) ((AssignmentController) controller).setUser(this.user);
            else if (controller instanceof ExamResultsController) ((ExamResultsController) controller).setUser(this.user);
            else if (controller instanceof MarkAttendanceController) ((MarkAttendanceController) controller).setUser(this.user);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load module: " + fxmlPath);
        }
    }

    private void logout() {
        try {
            Stage stage = (Stage) teacherNameLabel.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(loader.load()));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void addMenuItem(String title, String icon, Runnable action) {
        Button btn = new Button();
        btn.setPrefSize(100, 100);
        btn.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2); -fx-cursor: hand;");
        VBox content = new VBox(5); content.setAlignment(Pos.CENTER);
        Label iconLbl = new Label(icon); iconLbl.setStyle("-fx-font-size: 30px;");
        Label textLbl = new Label(title); textLbl.setWrapText(true); textLbl.setFont(Font.font("System", 12));
        content.getChildren().addAll(iconLbl, textLbl);
        btn.setGraphic(content); btn.setOnAction(e -> action.run());
        menuGrid.getChildren().add(btn);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setContentText(content); alert.showAndWait();
    }
}
