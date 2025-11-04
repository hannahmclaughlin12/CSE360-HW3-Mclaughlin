package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The SetupAdmin class handles the setup process for creating an administrator account.
 * This is intended to be used by the first user to initialize the system with admin credentials.
 */
public class AdminSetupPage {
	
    private final DatabaseHelper databaseHelper;

    public AdminSetupPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input fields for userName, password, name, and email
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter Admin userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your first and last name");
        nameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String name = nameField.getText();
            String email = emailField.getText();
            
            String userErrMessage = UserNameRecognizer.checkForValidUserName(userName);
            String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);
            String nameErrMessage = NameEvaluator.evaluateName(name);
            String emailErrMessage = EmailEvaluator.evaluateEmail(email);
            
            //Check if userName is valid
            if (userErrMessage != "") {
            	errorLabel.setText(userErrMessage);
            }
            else if (passwordErrMessage != "") {
            	errorLabel.setText(passwordErrMessage);
            }
            else if (nameErrMessage != "") {
            	errorLabel.setText(nameErrMessage);
            }
            else if (emailErrMessage != "" ) {
            	errorLabel.setText(emailErrMessage);
            }
            
            else {
            	try {
            		// Create a new User object with admin role and register in the database
            		User user=new User(userName, password, Role.admin, name, email);
            		databaseHelper.register(user);
                	System.out.println("Administrator setup completed.");
                
                	// Navigate to the Login Page
                	new UserLoginPage(databaseHelper).show(primaryStage);
            	} catch (SQLException e) {
                	System.err.println("Database error: " + e.getMessage());
                	e.printStackTrace();
            	}
            }
        });
    

        VBox layout = new VBox(10, userNameField, passwordField, nameField, emailField, setupButton, errorLabel);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Administrator Setup");
        primaryStage.show();
    }

]
