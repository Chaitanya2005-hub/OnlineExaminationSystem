package com.stark.exam.ui.student;
import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class StudentAssignmentController {
    @FXML private TableView<Assign> table;
    @FXML private TableColumn<Assign, String> colT;
    @FXML private TextArea solField;
    private User user; public void setUser(User u){this.user=u; load();}

    @FXML public void initialize() { colT.setCellValueFactory(d->new SimpleStringProperty(d.getValue().t)); }
    private void load() {
        try(Connection c=DBConnection.getConnection()){
            ResultSet rs=c.createStatement().executeQuery("SELECT id, title FROM assignments");
            var l=FXCollections.<Assign>observableArrayList();
            while(rs.next())l.add(new Assign(rs.getInt(1), rs.getString(2)));
            table.setItems(l);
        }catch(Exception e){}
    }
    @FXML private void handleSubmit() {
        if(table.getSelectionModel().getSelectedItem()==null)return;
        try(Connection c=DBConnection.getConnection()){
            PreparedStatement p=c.prepareStatement("INSERT INTO submissions (assignment_id, student_id, submission_text) VALUES (?,?,?)");
            p.setInt(1, table.getSelectionModel().getSelectedItem().id);
            p.setInt(2, user.getId());
            p.setString(3, solField.getText());
            p.executeUpdate();
            solField.clear();
        }catch(Exception e){}
    }
    public static class Assign{int id; String t; public Assign(int i, String t){this.id=i;this.t=t;}}
}
