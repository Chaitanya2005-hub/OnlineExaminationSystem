package com.stark.exam.ui.student;
import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class GrievanceController {
    @FXML private ComboBox<String> categoryBox;
    @FXML private TextArea descField;
    @FXML private Label statusLabel;
    private User user; public void setUser(User u){this.user=u;}

    @FXML public void initialize() { categoryBox.getItems().addAll("Exam Issue", "Results", "Attendance", "Other"); }

    @FXML private void handleSubmit() {
        try(Connection c = DBConnection.getConnection()) {
            PreparedStatement p = c.prepareStatement("INSERT INTO grievances (student_id, category, description) VALUES (?,?,?)");
            p.setInt(1, user.getId());
            p.setString(2, categoryBox.getValue());
            p.setString(3, descField.getText());
            p.executeUpdate();
            statusLabel.setText("✅ Submitted");
            descField.clear();
        } catch(Exception e) { statusLabel.setText("Error"); }
    }
}
