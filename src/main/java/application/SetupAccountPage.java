package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * SetupAccountPage class handles the account setup process for new users.
 * Users provide their userName, password, and a valid invitation code to register.
 */
public class SetupAccountPage {
	
    private final DatabaseHelper databaseHelper;
    // DatabaseHelper to handle database operations.
    public SetupAccountPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    /**
     * Displays the Setup Account page in the provided stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    public void show(Stage primaryStage) {
    	// Input fields for userName, password, and invitation code
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter first and last name");
        nameField.setMaxWidth(250);
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter email");
        emailField.setMaxWidth(250);
        
        TextField inviteCodeField = new TextField();
        inviteCodeField.setPromptText("Enter Invitation Code");
        inviteCodeField.setMaxWidth(250);
        
        // Label to display error messages for invalid input or registration issues
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
      //Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });
        

        Button setupButton = new Button("Setup");
        
        setupButton.setOnAction(a -> {
        	// Retrieve user input
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String name = nameField.getText();
            String email = emailField.getText();
            String code = inviteCodeField.getText();
            
            String userErrMessage = UserNameRecognizer.checkForValidUserName(userName);
            String passwordErrMessage = PasswordEvaluator.evaluatePassword(password);
            String nameErrMessage = NameEvaluator.evaluateName(name);
            String emailErrMessage = EmailEvaluator.evaluateEmail(email);
            
            try {
            	// Check if userName is valid
            	if (userErrMessage == ""){
            		// Check if the user already exists
            		if(!databaseHelper.doesUserExist(userName)) {
            			// Check if the password is valid
            			if (passwordErrMessage == "") {
	            			// Check if the name is valid
            				if (nameErrMessage == "") {	
								// Check if the email is valid
	            				if (emailErrMessage == "") {	
            						// Validate the invitation code
	            					String userRoles = databaseHelper.validateInvitationCode(code); // Get the roles string
	            					System.out.println(userRoles);
		            				if(userRoles != null) { // Confirm a string is returned
		            					// Create a new user and register them in the database
				            			User user=new User(userName, password, null, name, email);
				            			user.setRoles(userRoles);
				            			databaseHelper.register(user);
				            			databaseHelper.markInvitationCodeAsUsed(code);
				            			// Navigate to the Welcome Login Page
				            			if(user.numberOfRoles() > 1) {
				            				new WelcomeLoginPage(databaseHelper).show(primaryStage,user);
				            			}
				            			else if (userRoles.equals("student")){
				            				new StudentHomePage(databaseHelper).show(primaryStage);
				            			}
				            			else if (userRoles.equals("admin")) {
				            				new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
				            			}
				            			else if (userRoles.equals("instructor")) {
				            				new InstructorHomePage(databaseHelper).show(primaryStage);
				            			}
				            			else if (userRoles.equals("staff")) {
				            				new StaffHomePage(databaseHelper).show(primaryStage);
				            			}
				            			else if (userRoles.equals("reviewer")) {
				            				new ReviewerHomePage(databaseHelper).show(primaryStage);
				            			}
		            				}
		            			
		            				else {
		            					errorLabel.setText("Please enter a valid invitation code");
		            				}
	            				}
	            				else {
	            					errorLabel.setText(emailErrMessage);
	            				}
	            			}
	            			else {
	            				errorLabel.setText(nameErrMessage);
	            			}
            			}
            			else {
            				errorLabel.setText(passwordErrMessage);
            			}
            		}
            		else {
            			errorLabel.setText("This userName is taken!!.. Please use another to setup an account");
            		}
            	}
            	else {
            		errorLabel.setText(userErrMessage);
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, nameField, emailField, inviteCodeField, setupButton, errorLabel, backButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("Account Setup");
        primaryStage.show();
    }
    
}
