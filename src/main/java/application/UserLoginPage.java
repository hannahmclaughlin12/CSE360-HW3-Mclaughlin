package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

import databasePart1.*;

/**
 * The UserLoginPage class provides a login interface for users to access their accounts.
 * It validates the user's credentials and navigates to the appropriate page upon successful login.
 */
public class UserLoginPage {
	
    private final DatabaseHelper databaseHelper;

    public UserLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public void show(Stage primaryStage) {
    	// Input field for the user's userName, password
        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter userName");
        userNameField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);
        
        // Label to display error messages
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
        
        //Back button
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
        	new SetupLoginSelectionPage(databaseHelper).show(primaryStage);
        });

        Button loginButton = new Button("Login");
        
        loginButton.setOnAction(a -> {
        	// Retrieve user inputs
            String userName = userNameField.getText();
            String password = passwordField.getText();
            String name = databaseHelper.getUserName(userName);
            String email = databaseHelper.getUserEmail(userName);
            
            try {
            	User user=new User(userName, password, null, name, email);
            	WelcomeLoginPage welcomeLoginPage = new WelcomeLoginPage(databaseHelper);
            	
            	// Retrieve the user's role from the database using userName
            	String userRole = databaseHelper.getUserRole(userName);
            	
            	if(userRole!=null) {
            		user.setRoles(userRole);
            		user.cleanRoleList();
            		
            		//Navigates to proper page after login
            		if(databaseHelper.login(user)) {
            			if(user.numberOfRoles() > 1) {
            				welcomeLoginPage.show(primaryStage,user);
            			}
            			else if (userRole.equals("student")){
            				// inside the successful-login branch where userRole.equals("student")
            				new StudentHomePage(databaseHelper, user.getUserName()).show(primaryStage);
            			}
            			else if (userRole.equals("admin")) {
            				new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
            			}
            			else if (userRole.equals("instructor")) {
            				new InstructorHomePage(databaseHelper).show(primaryStage);
            			}
            			else if (userRole.equals("staff")) {
            				new StaffHomePage(databaseHelper).show(primaryStage);
            			}
            			else if (userRole.equals("reviewer")) {
            				new ReviewerHomePage(databaseHelper).show(primaryStage);
            			}
            			else {
            				new UserHomePage(databaseHelper).show(primaryStage);
            			}
            		}
            		else {
            			// Display an error if the login fails
                        errorLabel.setText("Error logging in");
            		}
            	}
            	else {
            		// Display an error if the account does not exist
                    errorLabel.setText("user account doesn't exists");
            	}
            	
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            } 
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");
        layout.getChildren().addAll(userNameField, passwordField, loginButton, errorLabel, backButton);

        primaryStage.setScene(new Scene(layout, 800, 400));
        primaryStage.setTitle("User Login");
        primaryStage.show();
    }
}
