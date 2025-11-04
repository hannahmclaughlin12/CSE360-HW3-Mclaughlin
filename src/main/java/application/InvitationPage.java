package application;


import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * InvitePage class represents the page where an admin can generate an invitation code.
 * The invitation code is displayed upon clicking a button.
 */

public class InvitationPage {
	
	private final DatabaseHelper databaseHelper;
	private final String adminUsername;
	
	public InvitationPage(DatabaseHelper databaseHelper, String adminUsername) {
		this.databaseHelper = databaseHelper;
		this.adminUsername = adminUsername;
	}
	
	/**
     * Displays the Invite Page in the provided primary stage.
     * 
     * @param databaseHelper An instance of DatabaseHelper to handle database operations.
     * @param primaryStage   The primary stage where the scene will be displayed.
     */
	private String rolesList = ""; // Create the String that gets pushed for roles
    public void show(Stage primaryStage) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display the title of the page
	    Label userLabel = new Label("Invite ");
	    userLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
	    
	    Button backButton = new Button("Back");
	    backButton.setOnAction(e -> {
	    	new AdminHomePage(databaseHelper, adminUsername).show(primaryStage);
	    });

		// Keegan:{
		Button adminButton = new Button("Give Admin Permissions"); // Create an admin button
	    adminButton.setOnAction(a -> {
	    	if (!rolesList.contains("admin")) {
	    		rolesList += "admin";
	    	}
	    });
	    Button studentButton = new Button("Give Student Permissions"); // Create a student button
	    studentButton.setOnAction(a -> {
	    	if (!rolesList.contains("student")) {
	    		rolesList += "student";
	    	}
	    });
	    Button reviewerButton = new Button("Give Reviewer Permissions"); // Create a review button
	    reviewerButton.setOnAction(a -> {
	    	if (!rolesList.contains("reviewer")) {
	    		rolesList += "reviewer";
	    	}
	    });
	    Button instructorButton = new Button("Give Instructor Permissions"); // Create an instructor button
	    instructorButton.setOnAction(a -> {
	    	if (!rolesList.contains("instructor")) {
	    		rolesList += "instructor";
	    	}
	    });
	    Button staffButton = new Button("Give Staff Permissions"); // Create a staff button
	    staffButton.setOnAction(a -> {
	    	if (!rolesList.contains("staff")) {
	    		rolesList += "staff";
	    	}
	    }); // }
		
	    // Button to generate the invitation code
	    Button showCodeButton = new Button("Generate Invitation Code");
	    
	    // Label to display the generated invitation code
	    Label inviteCodeLabel = new Label(""); ;
        inviteCodeLabel.setStyle("-fx-font-size: 14px; -fx-font-style: italic;");
        
        showCodeButton.setOnAction(a -> {
        	// Generate the invitation code using the databaseHelper and set it to the label
            String invitationCode = databaseHelper.generateInvitationCode(rolesList); // Push rolesList to this function
            inviteCodeLabel.setText(invitationCode);
        });
	    

        layout.getChildren().addAll(userLabel, showCodeButton, inviteCodeLabel, adminButton, studentButton, reviewerButton, instructorButton, staffButton, backButton);
	    Scene inviteScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(inviteScene);
	    primaryStage.setTitle("Invite Page");
    	
    }
}
