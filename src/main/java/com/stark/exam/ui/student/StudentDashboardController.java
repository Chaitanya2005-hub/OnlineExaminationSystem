package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
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
import java.sql.Connection;
import java.sql.ResultSet;

public class StudentDashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userErpLabel;
    @FXML private FlowPane menuGrid;
    @FXML private TextField searchField;

    private User user;

    public void setUser(User user) {
        this.user = user;
        userNameLabel.setText(user.getFullName().toUpperCase());
        userErpLabel.setText(user.getErpId());
        loadMenuOptions();
    }

    private void loadMenuOptions() {
        menuGrid.getChildren().clear();

        // Student Modules
        addMenuItem("Online Exam", "💻", this::openExamList);
        addMenuItem("Timetable", "🕒", () -> openWindow("/fxml/timetable.fxml", "My Timetable"));
        addMenuItem("Attendance", "📅", () -> openWindow("/fxml/attendance.fxml", "My Attendance"));
        addMenuItem("Results", "📊", () -> openWindow("/fxml/performance.fxml", "My Results"));
        addMenuItem("Admit Card", "🎫", () -> openWindow("/fxml/admit_card.fxml", "Admit Card"));
        addMenuItem("Assignment", "📝", () -> openWindow("/fxml/student_assignment.fxml", "Assignments"));
        addMenuItem("Grievance", "⚖️", () -> openWindow("/fxml/grievance.fxml", "Grievance Portal"));
        addMenuItem("Fees", "💵", this::openFeePortal);
        addMenuItem("Logout", "🚪", this::logout);
    }
    private void openFeePortal() {
        openWindow("/fxml/fee_details.fxml", "My Fee Status");
    }


    // --- NEW NOTICE BOARD FEATURE ---
    @FXML
    private void handleShowNotices() {
        StringBuilder content = new StringBuilder();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT title, message, date FROM notices ORDER BY date DESC LIMIT 5";
            ResultSet rs = conn.createStatement().executeQuery(query);

            if (!rs.isBeforeFirst()) content.append("No new announcements.");

            while (rs.next()) {
                content.append("📢 ").append(rs.getString("title").toUpperCase())
                        .append("\n   ").append(rs.getString("message"))
                        .append("\n   [").append(rs.getString("date")).append("]\n\n");
            }
        } catch (Exception e) { e.printStackTrace(); content.append("Error loading notices."); }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notice Board");
        alert.setHeaderText("Latest Announcements");
        TextArea area = new TextArea(content.toString());
        area.setEditable(false); area.setWrapText(true); area.setPrefSize(400, 300);
        alert.getDialogPane().setContent(area);
        alert.showAndWait();
    }

    @FXML
    private void handleViewProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/profile.fxml"));
            Parent root = loader.load();
            ProfileController controller = loader.getController();
            controller.setUser(this.user);
            Stage stage = new Stage();
            stage.setTitle("My Profile");
            stage.setScene(new Scene(root));
            stage.show();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof StudentAssignmentController) ((StudentAssignmentController) controller).setUser(this.user);
            else if (controller instanceof GrievanceController) ((GrievanceController) controller).setUser(this.user);
            else if (controller instanceof AttendanceController) ((AttendanceController) controller).setUser(this.user);
            else if (controller instanceof PerformanceController) ((PerformanceController) controller).setUser(this.user);
            else if (controller instanceof AdmitCardController) ((AdmitCardController) controller).setUser(this.user);
            else if (controller instanceof FeeController) {
                ((FeeController) controller).setUser(this.user);
            }
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void openExamList() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/exam_list.fxml"));
            Parent root = loader.load();
            ExamListController controller = loader.getController();
            controller.setUser(this.user);
            Stage stage = new Stage();
            stage.setTitle("Select Exam");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void logout() {
        try {
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
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
}
