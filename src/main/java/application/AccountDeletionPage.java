package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import databasePart1.DatabaseHelper;


/**
 * This page allows for an admin to remove user accounts
 */

public class AccountDeletionPage {

    private final DatabaseHelper databaseHelper;
    private final String adminUsername;

    public AccountDeletionPage(DatabaseHelper databaseHelper, String adminUsername) {
        this.databaseHelper = databaseHelper;
        this.adminUsername = adminUsername;
    }

    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Username");
        userNameField.setMaxWidth(250);
	    
	    Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> new AdminHomePage(databaseHelper, adminUsername).show(primaryStage));
        
        Button userButton = new Button("Delete Account");
        
        userButton.setOnAction(a -> {
        	String userName = userNameField.getText();
        	
        	//Checks if user exists
	        if(databaseHelper.doesUserExist(userName)) {
	        	//Checks that user is not current admin
	        	if (!userName.equals(adminUsername)) {
	        		//Creates a popup to confirm account deletion
	        		showDeleteConfirmation(userName);
	        	}
	        	else {
	        		errorLabel.setText("You can't delete your own account! Please enter another username");
	        	}
	        }
	        else {
				errorLabel.setText("This username does not exist! Please enter another username");
			}
        });
        

	     //layout for fields and buttons
        layout.getChildren().addAll(userNameField, userButton, errorLabel, backBtn);
	    Scene userScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(userScene);
	    primaryStage.setTitle("Account Deletion Page");   	
    }
    
    private void showDeleteConfirmation(String userName) {
    	Stage popup = new Stage();
    	popup.setTitle("Confirm Account Deletion");
    	
    	Label message = new Label("Are you sure you want to delete "  + userName + "'s account?");
    	message.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
    	
    	Button confirm = new Button("Yes");
    	
    	confirm.setOnAction(a -> {
    		databaseHelper.deleteUser(userName);
    		popup.close();
    		
    		Stage result = new Stage();
    		
    		Label resultMessage = new Label(userName + "'s account has been deleted.");
    		
    		Button close = new Button("Close");
    		close.setOnAction(b -> result.close());
    		
    		VBox resultLayout = new VBox(10, resultMessage, close);
    		resultLayout.setStyle("-fx-alignment: center; -fx-padding: 20;");
    		result.setScene(new Scene(resultLayout, 400, 200));
    		result.show();
    	});
    	
    	Button cancel = new Button("Cancel");
    	cancel.setOnAction(c -> popup.close());
    	
    	VBox layout = new VBox(15, message, confirm, cancel);
    	layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
    	Scene scene = new Scene(layout, 400, 200);
    	
    	popup.setScene(scene);
    	popup.show();
    }
}
