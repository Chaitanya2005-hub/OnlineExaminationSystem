package com.stark.exam.ui.teacher;
import com.stark.exam.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;

public class AnswerSheetController {
    @FXML private TextField erpField;
    @FXML private ComboBox<String> examSelector;
    @FXML private TableView<Response> responseTable;
    @FXML private TableColumn<Response, String> colQ, colSel, colCor;

    @FXML public void initialize() {
        colQ.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().q));
        colSel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().sel));
        colCor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().cor));
        try (Connection c = DBConnection.getConnection()) {
            ResultSet rs = c.createStatement().executeQuery("SELECT id, title FROM exams");
            while(rs.next()) examSelector.getItems().add(rs.getInt("id") + ": " + rs.getString("title"));
        } catch(Exception e) {}
    }

    @FXML private void handleViewSheet() {
        if(erpField.getText().isEmpty() || examSelector.getValue() == null) return;
        try (Connection c = DBConnection.getConnection()) {
            int examId = Integer.parseInt(examSelector.getValue().split(":")[0].trim());
            PreparedStatement p = c.prepareStatement("SELECT id FROM users WHERE erp_id=?");
            p.setString(1, erpField.getText());
            ResultSet uRs = p.executeQuery();
            if(!uRs.next()) return;
            int uid = uRs.getInt("id");

            String sql = "SELECT q.question_text, sr.selected_option, q.correct_answer FROM questions q LEFT JOIN student_responses sr ON q.id=sr.question_id AND sr.student_id=? WHERE q.exam_id=?";
            PreparedStatement p2 = c.prepareStatement(sql);
            p2.setInt(1, uid); p2.setInt(2, examId);
            ResultSet rs = p2.executeQuery();

            var list = FXCollections.<Response>observableArrayList();
            while(rs.next()) list.add(new Response(rs.getString(1), rs.getString(2), rs.getString(3)));
            responseTable.setItems(list);
        } catch(Exception e) { e.printStackTrace(); }
    }
    public static class Response { String q, sel, cor; public Response(String q, String s, String c){this.q=q;this.sel=s;this.cor=c;} }
}
