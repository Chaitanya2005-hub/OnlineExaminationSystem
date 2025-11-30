package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import com.stark.exam.util.PdfService; // Import our new service
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser; // Used to open "Save As" dialog

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdmitCardController {
    @FXML private Label lblName, lblRoll, lblStatus;
    @FXML private VBox cardLayout;
    private User user;

    public void setUser(User user) {
        this.user = user;
        checkStatus();
    }

    private void checkStatus() {
        try (Connection conn = DBConnection.getConnection()) {
            // Check if admin has released the card
            String query = "SELECT status FROM admit_cards WHERE student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next() && rs.getString("status").equals("Released")) {
                cardLayout.setVisible(true);
                lblStatus.setText(""); // Clear error msg
                lblName.setText("Name: " + user.getFullName());
                lblRoll.setText("Roll No: " + user.getErpId());
            } else {
                cardLayout.setVisible(false);
                lblStatus.setText("⚠️ Admit Card NOT Released by Admin yet.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePrint() {
        // 1. Open "Save As" Dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Admit Card PDF");
        fileChooser.setInitialFileName(user.getErpId() + "_AdmitCard.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(lblName.getScene().getWindow());

        if (file != null) {
            try {
                // 2. Call our PdfService to write the file
                PdfService.generateAdmitCard(user, file.getAbsolutePath());

                // 3. Show Success Message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Download Successful");
                alert.setHeaderText(null);
                alert.setContentText("Admit Card saved to:\n" + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setContentText("Failed to generate PDF: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
