package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProfileController {

    @FXML private Label nameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField usernameField;
    @FXML private TextField erpIdField;
    @FXML private TextField deptField;
    @FXML private TextField yearField;
    @FXML private ImageView profileImage; // The image view

    private User user;

    public void setUser(User user) {
        this.user = user;
        nameLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole().toUpperCase());
        usernameField.setText(user.getUsername());
        erpIdField.setText(user.getErpId());
        deptField.setText(user.getDepartment() != null ? user.getDepartment() : "N/A");
        yearField.setText(String.valueOf(user.getYear()));

        // Load existing photo if available
        loadPhoto(user.getPhotoPath());
    }

    private void loadPhoto(String path) {
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                Image img = new Image(file.toURI().toString());
                profileImage.setImage(img);

                // Make it circular (Optional visual flair)
                Circle clip = new Circle(50, 50, 50);
                profileImage.setClip(clip);
            }
        }
    }

    @FXML
    private void handleUploadPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Photo");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(nameLabel.getScene().getWindow());

        if (file != null) {
            savePhotoPath(file.getAbsolutePath());
            loadPhoto(file.getAbsolutePath()); // Update UI immediately
        }
    }

    private void savePhotoPath(String path) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "UPDATE users SET photo_path = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, path);
            pstmt.setInt(2, user.getId());
            pstmt.executeUpdate();

            // Update local user object too
            user.setPhotoPath(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) nameLabel.getScene().getWindow()).close();
    }
}
