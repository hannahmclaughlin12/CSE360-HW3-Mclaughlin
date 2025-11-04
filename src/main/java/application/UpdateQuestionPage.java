package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.util.List;

public class UpdateQuestionPage {

    private final DatabaseHelper db;
    private final String currentUser;

    public UpdateQuestionPage(DatabaseHelper db, String currentUser) {
        this.db = db;
        this.currentUser = currentUser;
    }

    public void show(Stage stage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center-left;");

        Label titleLabel = new Label("Update Your Questions");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // List to display user's questions
        ListView<Question> questionsList = new ListView<>();
        questionsList.setPrefHeight(200);
        
        // Custom cell factory to display question titles
        questionsList.setCellFactory(param -> new ListCell<Question>() {
            @Override
            protected void updateItem(Question question, boolean empty) {
                super.updateItem(question, empty);
                if (empty || question == null) {
                    setText(null);
                } else {
                    setText("ID: " + question.getId() + " - " + question.getTitle() + 
                           (question.getResolved() ? " ✓" : ""));
                }
            }
        });

        TextField questionTitle = new TextField();
        questionTitle.setPromptText("Question title...");
        questionTitle.setPrefWidth(400);

        TextArea questionBody = new TextArea();
        questionBody.setPromptText("Question details...");
        questionBody.setPrefRowCount(6);
        questionBody.setPrefWidth(400);

        Button loadQuestionsBtn = new Button("Load My Questions");
        Button updateBtn = new Button("Update Selected Question");
        Button backBtn = new Button("Back to Student Home");

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #2E8B57;");

        // Load user's questions
        loadQuestionsBtn.setOnAction(e -> {
            try {
                List<Question> userQuestions = db.getQuestionsByStudent(currentUser);
                questionsList.getItems().setAll(userQuestions);
                statusLabel.setText("Loaded " + userQuestions.size() + " question(s)");
                statusLabel.setStyle("-fx-text-fill: #2E8B57;");
            } catch (Exception ex) {
                statusLabel.setText("Error loading questions: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #FF0000;");
                ex.printStackTrace();
            }
        });

        // When a question is selected, populate the edit fields
        questionsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                questionTitle.setText(newVal.getTitle());
                questionBody.setText(newVal.getQuestion());
                updateBtn.setDisable(false);
            } else {
                updateBtn.setDisable(true);
            }
        });

        // Update the selected question
        updateBtn.setOnAction(e -> {
            Question selectedQuestion = questionsList.getSelectionModel().getSelectedItem();
            if (selectedQuestion == null) {
                statusLabel.setText("Please select a question to update");
                statusLabel.setStyle("-fx-text-fill: #FF0000;");
                return;
            }

            String newTitle = questionTitle.getText().trim();
            String newBody = questionBody.getText().trim();

            if (newTitle.isEmpty() || newBody.isEmpty()) {
                statusLabel.setText("Title and body cannot be empty");
                statusLabel.setStyle("-fx-text-fill: #FF0000;");
                return;
            }

            try {
                boolean success = db.updateQuestion(selectedQuestion.getId(), newTitle, newBody);
                if (success) {
                    statusLabel.setText("Question updated successfully!");
                    statusLabel.setStyle("-fx-text-fill: #2E8B57;");
                    // Refresh the list
                    loadQuestionsBtn.fire();
                } else {
                    statusLabel.setText("Failed to update question");
                    statusLabel.setStyle("-fx-text-fill: #FF0000;");
                }
            } catch (Exception ex) {
                statusLabel.setText("Error updating question: " + ex.getMessage());
                statusLabel.setStyle("-fx-text-fill: #FF0000;");
                ex.printStackTrace();
            }
        });

        backBtn.setOnAction(e -> {
            StudentHomePage studentHome = new StudentHomePage(db, currentUser);
            studentHome.show(stage);
        });

        // Initially disable update button until question is selected
        updateBtn.setDisable(true);

        layout.getChildren().addAll(
            titleLabel,
            loadQuestionsBtn,
            new Label("Your Questions:"),
            questionsList,
            new Label("Edit Question Title:"), questionTitle,
            new Label("Edit Question Details:"), questionBody,
            updateBtn,
            backBtn,
            statusLabel
        );

        Scene scene = new Scene(layout, 700, 600);
        stage.setScene(scene);
        stage.setTitle("Update Questions");
        stage.show();
    }
}
