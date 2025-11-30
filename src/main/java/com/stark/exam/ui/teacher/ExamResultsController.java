package com.stark.exam.ui.teacher;
import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class ExamResultsController {
    @FXML private ComboBox<String> examSelector;
    @FXML private TableView<Res> resultTable;
    @FXML private TableColumn<Res, String> colN;
    @FXML private TableColumn<Res, Number> colS;
    private User user; public void setUser(User u){this.user=u;}

    @FXML public void initialize() {
        colN.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().n));
        colS.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().s));
        try (Connection c = DBConnection.getConnection()) {
            ResultSet rs = c.createStatement().executeQuery("SELECT id, title FROM exams");
            while(rs.next()) examSelector.getItems().add(rs.getInt("id") + ": " + rs.getString("title"));
        } catch(Exception e) {}
    }
    @FXML private void handleLoad() {
        if(examSelector.getValue()==null)return;
        try (Connection c = DBConnection.getConnection()) {
            int eid = Integer.parseInt(examSelector.getValue().split(":")[0].trim());
            PreparedStatement p = c.prepareStatement("SELECT u.full_name, r.score FROM results r JOIN users u ON r.student_id=u.id WHERE r.exam_id=?");
            p.setInt(1, eid);
            ResultSet rs = p.executeQuery();
            var list = FXCollections.<Res>observableArrayList();
            while(rs.next()) list.add(new Res(rs.getString(1), rs.getInt(2)));
            resultTable.setItems(list);
        } catch(Exception e) {}
    }
    public static class Res { String n; int s; public Res(String n, int s){this.n=n;this.s=s;} }
}
