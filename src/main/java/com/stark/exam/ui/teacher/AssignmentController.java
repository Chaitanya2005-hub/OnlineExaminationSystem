package com.stark.exam.ui.teacher;
import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class AssignmentController {
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private DatePicker datePicker;
    private User user; public void setUser(User u){this.user=u;}

    @FXML private void handlePost() {
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement p = c.prepareStatement("INSERT INTO assignments (title, description, due_date, created_by) VALUES (?,?,?,?)");
            p.setString(1, titleField.getText());
            p.setString(2, descField.getText());
            p.setDate(3, java.sql.Date.valueOf(datePicker.getValue()));
            p.setInt(4, user.getId());
            p.executeUpdate();
            titleField.clear(); descField.clear();
        } catch(Exception e) { e.printStackTrace(); }
    }
}
