package com.stark.exam.ui.student;

import com.stark.exam.db.DBConnection;
import com.stark.exam.model.User;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ExamInterfaceController {

    @FXML private Label examTitleLabel, timerLabel, questionLabel, qNumLabel;
    @FXML private RadioButton optA, optB, optC, optD;
    @FXML private ToggleGroup optionsGroup;
    @FXML private Button submitBtn, nextBtn;

    private User user;
    private int examId;
    private int durationSeconds;
    private Stage stage;

    private List<Question> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private int warnings = 0;
    private Timeline timeline;
    private Timeline heartbeat; // <--- NEW: For Live Status
    private boolean examEnded = false;

    public void setupExam(User user, int examId, String title, int durationMins, Stage stage) {
        this.user = user;
        this.examId = examId;
        this.stage = stage;
        this.durationSeconds = durationMins * 60;

        examTitleLabel.setText(title);
        loadQuestions();
        startTimer();
        startHeartbeat(); // <--- START SENDING LIVE DATA
        setupSecurity();
        showQuestion();

        // Insert initial status record
        updateLiveStatus("Active");
    }

    // --- NEW: Heartbeat Logic ---
    private void startHeartbeat() {
        heartbeat = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            if (!examEnded) {
                updateLiveStatus("Active");
                checkRemoteTermination(); // Check if Teacher killed the exam
            }
        }));
        heartbeat.setCycleCount(Timeline.INDEFINITE);
        heartbeat.play();
    }

    private void updateLiveStatus(String status) {
        try (Connection conn = DBConnection.getConnection()) {
            // Upsert (Insert or Update)
            String query = "INSERT INTO exam_live_status (student_id, exam_id, current_question, warnings_count, status, last_heartbeat) " +
                    "VALUES (?, ?, ?, ?, ?, NOW()) " +
                    "ON DUPLICATE KEY UPDATE current_question=?, warnings_count=?, status=?, last_heartbeat=NOW()";

            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, examId);
            pstmt.setInt(3, currentIndex + 1);
            pstmt.setInt(4, warnings);
            pstmt.setString(5, status);

            // Update params
            pstmt.setInt(6, currentIndex + 1);
            pstmt.setInt(7, warnings);
            pstmt.setString(8, status);

            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void checkRemoteTermination() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT status FROM exam_live_status WHERE student_id = ? AND exam_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, examId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                if ("Terminated".equalsIgnoreCase(status)) {
                    Platform.runLater(() -> {
                        heartbeat.stop();
                        showAlert("TERMINATED", "Exam terminated by Administrator.");
                        handleSubmit();
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
    // ----------------------------

    private void loadQuestions() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT * FROM questions WHERE exam_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, examId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                questions.add(new Question(
                        rs.getInt("id"), // Need ID for response tracking
                        rs.getString("question_text"),
                        rs.getString("option_a"),
                        rs.getString("option_b"),
                        rs.getString("option_c"),
                        rs.getString("option_d"),
                        rs.getString("correct_answer")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showQuestion() {
        if (questions.isEmpty()) return;
        Question q = questions.get(currentIndex);
        qNumLabel.setText("Question " + (currentIndex + 1) + " of " + questions.size());
        questionLabel.setText(q.text);
        optA.setText(q.a); optB.setText(q.b); optC.setText(q.c); optD.setText(q.d);
        optionsGroup.selectToggle(null);

        if (currentIndex == questions.size() - 1) { nextBtn.setDisable(true); submitBtn.setDisable(false); }
        else { nextBtn.setDisable(false); submitBtn.setDisable(true); }
    }

    @FXML
    private void handleNext() {
        saveAnswer();
        currentIndex++;
        showQuestion();
    }

    @FXML
    private void handleSubmit() {
        if (examEnded) return;
        examEnded = true;
        saveAnswer();
        if (timeline != null) timeline.stop();
        if (heartbeat != null) heartbeat.stop();

        updateLiveStatus("Submitted");
        calculateResult();
        if (stage != null) stage.close();
    }

    private void saveAnswer() {
        RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
        if (selected != null) {
            String ans = "";
            if (selected == optA) ans = "A"; else if (selected == optB) ans = "B";
            else if (selected == optC) ans = "C"; else if (selected == optD) ans = "D";
            questions.get(currentIndex).userAnswer = ans;

            // Save detailed response to DB immediately
            saveResponseToDB(questions.get(currentIndex));
        }
    }

    private void saveResponseToDB(Question q) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO student_responses (student_id, exam_id, question_id, selected_option) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, examId);
            pstmt.setInt(3, q.id);
            pstmt.setString(4, q.userAnswer);
            pstmt.executeUpdate();
        } catch (Exception e) { /* Ignore for now */ }
    }

    private void calculateResult() {
        score = 0;
        for (Question q : questions) {
            if (q.userAnswer != null && q.userAnswer.equals(q.correct)) score++;
        }
        saveResultToDB();
    }

    private void saveResultToDB() {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO results (student_id, exam_id, score, total_marks, security_warnings) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, user.getId());
            pstmt.setInt(2, examId);
            pstmt.setInt(3, score);
            pstmt.setInt(4, questions.size());
            pstmt.setInt(5, warnings);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startTimer() {
        timerLabel.setText(formatTime(durationSeconds));
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            durationSeconds--;
            timerLabel.setText(formatTime(durationSeconds));
            if (durationSeconds <= 0) {
                Platform.runLater(() -> handleSubmit());
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void setupSecurity() {
        stage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !examEnded) {
                warnings++;
                updateLiveStatus("Active"); // Push warning count to DB immediately
                if (warnings >= 3) {
                    Platform.runLater(this::terminateExam);
                } else {
                    Platform.runLater(() -> showAlert("SECURITY WARNING", "Do not switch windows! Warning " + warnings + "/3"));
                }
            }
        });
    }

    private void terminateExam() {
        if (examEnded) return;
        if (timeline != null) timeline.stop();
        updateLiveStatus("Terminated");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("EXAM TERMINATED");
        alert.setHeaderText("Security Violation");
        alert.setContentText("You have switched windows 3 times. Exam Auto-Submitted.");
        alert.showAndWait();
        handleSubmit();
    }

    private String formatTime(int totalSecs) {
        int m = totalSecs / 60; int s = totalSecs % 60; return String.format("%02d:%02d", m, s);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static class Question {
        int id;
        String text, a, b, c, d, correct, userAnswer;
        public Question(int id, String t, String a, String b, String c, String d, String cor) {
            this.id = id; this.text = t; this.a = a; this.b = b; this.c = c; this.d = d; this.correct = cor;
        }
    }
}
