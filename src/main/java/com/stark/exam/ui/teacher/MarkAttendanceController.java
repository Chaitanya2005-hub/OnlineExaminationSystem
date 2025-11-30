package com.stark.exam.ui.teacher;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import com.stark.exam.util.QrService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Random;

public class MarkAttendanceController {

    // --- FXML Fields (MUST MATCH FXML FILE) ---
    @FXML private DatePicker datePicker;
    @FXML private ListView<CheckBox> studentListView;
    @FXML private Label statusLabel;

    // QR Components
    @FXML private ImageView qrView;
    @FXML private Label codeLabel;
    @FXML private VBox qrContainer;

    private User user;
    private Timeline qrTimer;

    public void setUser(User user) {
        this.user = user;
        datePicker.setValue(LocalDate.now());
    }

    @FXML
    public void initialize() {
        // Optional: Auto-load students on start
        loadStudents();
    }

    @FXML
    private void loadStudents() {
        studentListView.getItems().clear();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT id, full_name, erp_id FROM users WHERE role = 'student'";
            ResultSet rs = conn.createStatement().executeQuery(query);

            while (rs.next()) {
                CheckBox cb = new CheckBox(rs.getString("full_name") + " (" + rs.getString("erp_id") + ")");
                cb.setUserData(rs.getInt("id"));
                studentListView.getItems().add(cb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Error loading students");
        }
    }

    @FXML
    private void markAllPresent() {
        for (CheckBox cb : studentListView.getItems()) {
            cb.setSelected(true);
        }
    }

    @FXML
    private void saveAttendance() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO attendance (student_id, date, status, marked_by) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);

            for (CheckBox cb : studentListView.getItems()) {
                pstmt.setInt(1, (int) cb.getUserData());
                pstmt.setDate(2, java.sql.Date.valueOf(datePicker.getValue()));
                pstmt.setString(3, cb.isSelected() ? "Present" : "Absent");
                pstmt.setInt(4, user.getId());
                pstmt.addBatch();
            }

            pstmt.executeBatch();
            statusLabel.setText("✅ Attendance Saved!");
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateQR() {
        qrContainer.setVisible(true);
        statusLabel.setText("Generating QR Code...");

        // Generate immediately
        generateAndPushCode();

        // Start Timer (Optional: Remove if it causes issues)
        if (qrTimer != null) qrTimer.stop();
        qrTimer = new Timeline(new KeyFrame(Duration.seconds(10), e -> generateAndPushCode()));
        qrTimer.setCycleCount(Animation.INDEFINITE);
        qrTimer.play();
    }

    private void generateAndPushCode() {
        try {
            String secretCode = String.format("%04d", new Random().nextInt(10000));

            // Update DB
            try (Connection conn = DBConnection.getConnection()) {
                // Ensure table exists or row 1 exists
                conn.createStatement().executeUpdate("INSERT INTO live_codes (id, code) VALUES (1, '0000') ON DUPLICATE KEY UPDATE code='" + secretCode + "'");
            }

            // Update UI
            String qrData = "ATTEND:" + secretCode;
            // Check if QrService exists
            try {
                qrView.setImage(QrService.generateQr(qrData, 200, 200));
                codeLabel.setText("Code: " + secretCode);
            } catch (NoClassDefFoundError e) {
                statusLabel.setText("Error: QrService not found. Check dependencies.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
