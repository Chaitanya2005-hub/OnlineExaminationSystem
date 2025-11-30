package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FeeController {
    @FXML private Label lblTotal, lblPaid, lblPending, lblStatus;
    @FXML private ProgressBar feeProgress;
    private User user;

    public void setUser(User user) {
        this.user = user;
        loadFeeDetails();
    }

    private void loadFeeDetails() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM fees WHERE student_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total_fee");
                double paid = rs.getDouble("paid_fee");
                double pending = total - paid;
                String status = rs.getString("status");

                lblTotal.setText(String.format("₹ %.2f", total));
                lblPaid.setText(String.format("₹ %.2f", paid));
                lblPending.setText(String.format("₹ %.2f", pending));
                lblStatus.setText(status);

                // Styling
                if (status.equals("Approved")) lblStatus.setStyle("-fx-text-fill: green;");
                else lblStatus.setStyle("-fx-text-fill: orange;");

                feeProgress.setProgress(paid / total);
            } else {
                lblStatus.setText("No Fee Record Found.");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
