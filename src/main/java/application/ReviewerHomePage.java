package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//=== EDITS START ===
import javafx.scene.control.Button;
import databasePart1.DatabaseHelper;
//=== EDITS END ===

/**
 * This page displays a simple welcome message for the user.
 */

public class ReviewerHomePage {
	 // === EDITS START ===
    private final DatabaseHelper databaseHelper;

    public ReviewerHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    // === EDITS END ===

    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label userLabel = new Label("Hello, Reviewer!");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		// === EDITS START ===
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));
        // === EDITS END ===

	     // edit layout for logout button 
        layout.getChildren().addAll(userLabel, logoutBtn);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Reviewer Page");
    	
    }
}
