package com.stark.exam.ui.author;
import com.stark.exam.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class AdminAttendanceController {
    @FXML private TextField searchField;
    @FXML private TableView<Att> table;
    @FXML private TableColumn<Att, String> colD, colS;

    @FXML public void initialize() {
        colD.setCellValueFactory(d->new SimpleStringProperty(d.getValue().d));
        colS.setCellValueFactory(d->new SimpleStringProperty(d.getValue().s));
    }
    @FXML private void handleSearch() {
        try(Connection c=DBConnection.getConnection()){
            PreparedStatement p=c.prepareStatement("SELECT a.date, a.status FROM attendance a JOIN users u ON a.student_id=u.id WHERE u.erp_id=?");
            p.setString(1, searchField.getText());
            ResultSet rs=p.executeQuery();
            var l=FXCollections.<Att>observableArrayList();
            while(rs.next()) l.add(new Att(rs.getString(1), rs.getString(2)));
            table.setItems(l);
        }catch(Exception e){}
    }
    public static class Att{String d,s;public Att(String d,String s){this.d=d;this.s=s;}}
}
