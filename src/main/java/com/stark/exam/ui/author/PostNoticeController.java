package com.stark.exam.ui.author;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class PostNoticeController {

    @FXML private TextField titleField;
    @FXML private TextArea messageField;

    private User user; // The Admin posting the notice

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private void handlePost() {
        if (titleField.getText().isEmpty() || messageField.getText().isEmpty()) {
            showAlert("Error", "Please fill in both Title and Message.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "INSERT INTO notices (title, message, posted_by) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, titleField.getText());
            pstmt.setString(2, messageField.getText());
            // Use user.getId() if user is set, otherwise default to 1 (Admin)
            pstmt.setInt(3, (user != null) ? user.getId() : 1);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                showAlert("Success", "✅ Notice Posted Successfully!");
                titleField.clear();
                messageField.clear();
            } else {
                showAlert("Error", "Failed to post notice.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database Error", e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
