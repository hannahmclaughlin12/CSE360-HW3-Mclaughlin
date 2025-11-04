package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * Unified StudentHomePage with all navigation options
 */
public class StudentHomePage {

    private final DatabaseHelper db;
    private final String userName;

    /** New 2-arg ctor used by login and other pages that know the username */
    public StudentHomePage(DatabaseHelper db, String userName) {
        this.db = db;
        this.userName = userName;
    }

    /** Keep the original 1-arg ctor so old call sites still compile */
    public StudentHomePage(DatabaseHelper db) {
        this(db, null);
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 30;");

        String displayName = (userName == null || userName.isBlank()) ? "Student" : userName;
        Label welcomeLabel = new Label("Hello, " + displayName + "!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label instructionLabel = new Label("What would you like to do?");
        instructionLabel.setStyle("-fx-font-size: 14px;");

        // Navigation buttons - Integrated from both versions
        Button askQuestionBtn = new Button("Ask a Question");
        askQuestionBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");
        
        Button updateQuestionsBtn = new Button("Update My Questions");
        updateQuestionsBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");
        
        Button viewQuestionsBtn = new Button("Questions & Answers");
        viewQuestionsBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");

        // Button actions - Integrated functionality
        askQuestionBtn.setOnAction(e -> {
            QuestionAnswerPage askPage = new QuestionAnswerPage(db, userName);
            askPage.showAskQuestionPage(primaryStage);
        });

        updateQuestionsBtn.setOnAction(e -> {
            UpdateQuestionPage updatePage = new UpdateQuestionPage(db, userName);
            updatePage.show(primaryStage);
        });

        viewQuestionsBtn.setOnAction(e -> {
            // Connect to the team's existing QuestionAnswerPage
            QuestionAnswerPage qaPage = new QuestionAnswerPage(db, userName);
            qaPage.show(primaryStage);
        });

        logoutBtn.setOnAction(e -> {
            new SetupLoginSelectionPage(db).show(primaryStage);
        });

        layout.getChildren().addAll(
            welcomeLabel, 
            instructionLabel,
            askQuestionBtn, 
            updateQuestionsBtn, 
            viewQuestionsBtn,
            logoutBtn
        );

        primaryStage.setScene(new Scene(layout, 800, 450));
        primaryStage.setTitle("Student Home Page");
        primaryStage.show();
    }
}
