package com.stark.exam.ui.login;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter username and password");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user);
            pstmt.setString(2, pass);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                // 1. Create User Object
                User currentUser = new User();
                currentUser.setId(rs.getInt("id"));
                currentUser.setUsername(rs.getString("username"));
                currentUser.setFullName(rs.getString("full_name"));
                currentUser.setRole(rs.getString("role"));
                currentUser.setErpId(rs.getString("erp_id"));
                currentUser.setYear(rs.getInt("year"));
                currentUser.setDepartment(rs.getString("department"));

                // --- NEW: Load Photo Path ---
                currentUser.setPhotoPath(rs.getString("photo_path"));

                // 2. Route to Dashboard
                openDashboard(currentUser);
            } else {
                errorLabel.setText("Invalid Username or Password");
            }

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Database Connection Error");
        }
    }

    private void openDashboard(User user) {
        try {
            String fxml = "";
            String title = "";

            switch (user.getRole()) {
                case "student":
                    fxml = "/fxml/student_dashboard.fxml";
                    title = "Student Portal - " + user.getFullName();
                    break;
                case "teacher":
                    fxml = "/fxml/teacher_dashboard.fxml";
                    title = "Faculty Portal - " + user.getFullName();
                    break;
                case "author":
                    fxml = "/fxml/author_dashboard.fxml";
                    title = "Admin Control Center";
                    break;
                default:
                    errorLabel.setText("Unknown Role");
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            // Pass User Data to Controller (Reflection used to handle different classes)
            Object controller = loader.getController();
            try {
                controller.getClass().getMethod("setUser", User.class).invoke(controller, user);
            } catch (Exception e) { e.printStackTrace(); }

            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Error loading dashboard: " + e.getMessage());
        }
    }
}
