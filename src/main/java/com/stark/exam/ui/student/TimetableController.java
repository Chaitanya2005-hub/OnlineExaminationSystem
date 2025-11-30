package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.sql.Connection;
import java.sql.ResultSet;

public class TimetableController {
    @FXML private TableView<ExamSchedule> timetableTable;
    @FXML private TableColumn<ExamSchedule, String> colSubject;
    @FXML private TableColumn<ExamSchedule, String> colDate;
    @FXML private TableColumn<ExamSchedule, String> colTime;

    @FXML
    public void initialize() {
        colSubject.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().subject));
        colDate.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().date));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().time));
        loadTimetable();
    }

    private void loadTimetable() {
        ObservableList<ExamSchedule> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT title, exam_date, start_time FROM exams WHERE status = 'scheduled'";
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                list.add(new ExamSchedule(
                        rs.getString("title"),
                        rs.getDate("exam_date").toString(),
                        rs.getTime("start_time").toString()
                ));
            }
            timetableTable.setItems(list);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class ExamSchedule {
        String subject, date, time;
        public ExamSchedule(String s, String d, String t) { this.subject = s; this.date = d; this.time = t; }
    }
}
