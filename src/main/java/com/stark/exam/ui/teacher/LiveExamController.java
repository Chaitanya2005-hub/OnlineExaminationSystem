package com.stark.exam.ui.teacher;

import com.stark.exam.db.DBConnection;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LiveExamController {

    @FXML private TableView<LiveStatus> liveTable;
    @FXML private TableColumn<LiveStatus, String> colStudent;
    @FXML private TableColumn<LiveStatus, String> colExam;
    @FXML private TableColumn<LiveStatus, String> colStatus;
    @FXML private TableColumn<LiveStatus, Number> colWarnings;
    @FXML private Label lastUpdatedLabel;

    private Timeline timeline;

    @FXML
    public void initialize() {
        colStudent.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().studentName));
        colExam.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().examTitle));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().status));
        colWarnings.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().warnings));

        // Row Factory for Coloring (Yellow = Request, Red = Cheating)
        liveTable.setRowFactory(tv -> new TableRow<LiveStatus>() {
            @Override
            protected void updateItem(LiveStatus item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.warnings > 0) {
                    setStyle("-fx-background-color: #ffcdd2;"); // Red
                } else if ("Requested".equals(item.status)) {
                    setStyle("-fx-background-color: #fff9c4;"); // Yellow
                } else {
                    setStyle("");
                }
            }
        });

        addApproveButton();
        refreshData();

        // Auto-refresh every 2 seconds to see new requests fast
        timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> refreshData()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void addApproveButton() {
        TableColumn<LiveStatus, Void> colAction = new TableColumn<>("Action");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Approve");
            {
                btn.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    LiveStatus data = getTableView().getItems().get(getIndex());
                    if ("Requested".equals(data.status)) approveRequest(data.requestId);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    LiveStatus data = getTableView().getItems().get(getIndex());
                    setGraphic("Requested".equals(data.status) ? btn : null);
                }
            }
        });
        liveTable.getColumns().add(colAction);
    }

    private void approveRequest(int requestId) {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "UPDATE exam_requests SET status = 'Approved' WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, requestId);
            pstmt.executeUpdate();
            refreshData(); // Immediate update
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshData() {
        ObservableList<LiveStatus> data = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection()) {
            // 1. Fetch Pending Requests
            String reqQuery = "SELECT r.id, u.full_name, e.title FROM exam_requests r " +
                    "JOIN users u ON r.student_id = u.id " +
                    "JOIN exams e ON r.exam_id = e.id " +
                    "WHERE r.status = 'Requested'";

            ResultSet rsReq = conn.createStatement().executeQuery(reqQuery);
            while (rsReq.next()) {
                data.add(new LiveStatus(rsReq.getInt("id"), rsReq.getString("full_name"), rsReq.getString("title"), "Requested", 0));
            }

            // 2. Fetch Active Exams (Warnings)
            String resQuery = "SELECT u.full_name, e.title, r.security_warnings FROM results r " +
                    "JOIN users u ON r.student_id = u.id " +
                    "JOIN exams e ON r.exam_id = e.id " +
                    "WHERE r.score = 0 AND r.status != 'completed'"; // Simple logic for active

            ResultSet rsRes = conn.createStatement().executeQuery(resQuery);
            while (rsRes.next()) {
                data.add(new LiveStatus(0, rsRes.getString("full_name"), rsRes.getString("title"), "Active", rsRes.getInt("security_warnings")));
            }

            liveTable.setItems(data);
            lastUpdatedLabel.setText("Updated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static class LiveStatus {
        int requestId;
        String studentName, examTitle, status;
        int warnings;
        public LiveStatus(int rid, String s, String e, String stat, int w) {
            this.requestId = rid; this.studentName = s; this.examTitle = e; this.status = stat; this.warnings = w;
        }
        // Getters for PropertyValueFactory
        public String getStudentName() { return studentName; }
        public String getExamTitle() { return examTitle; }
        public String getStatus() { return status; }
        public int getWarnings() { return warnings; }
    }
}
