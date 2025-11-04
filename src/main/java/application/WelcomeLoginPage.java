package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import databasePart1.*;
//add ons for role picker
import javafx.scene.layout.HBox;
import javafx.scene.control.ComboBox;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//end add ons

/**
 * The WelcomeLoginPage class displays a welcome screen for authenticated users.
 * It allows users to navigate to their respective pages based on their role or quit the application.
 */
public class WelcomeLoginPage {
	
	private final DatabaseHelper databaseHelper;

    public WelcomeLoginPage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
    public void show( Stage primaryStage, User user) {
    	
    	VBox layout = new VBox(5);
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    Label welcomeLabel = new Label("Welcome!!");
	    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	 // ==== BEGIN role picker addition ====
	    Label pickLbl = new Label("Choose your role:");
	    ComboBox<String> rolePicker = new ComboBox<>();

	    // 1) Get roles
	    String raw = user.getRole() == null ? "" : user.getRole();
	    Set<String> found = new LinkedHashSet<>();

	    // Match any role word anywhere in the string
	    Pattern p = Pattern.compile("(admin|student|instructor|staff|reviewer)", Pattern.CASE_INSENSITIVE);
	    Matcher m = p.matcher(raw);
	    while (m.find()) {
	        found.add(m.group().toLowerCase());
	    }

	    // If nothing matched simple split
	    if (found.isEmpty()) {
	        String cleaned = raw.replace("[", "").replace("]", "");
	        for (String tok : cleaned.split("[,;\\s]+")) {
	            String t = tok.trim().toLowerCase();
	            if (!t.isEmpty() && p.matcher(t).matches()) {
	                found.add(t);
	            }
	        }
	    }

	    // 2) Feed ComboBox: ONE row per role assign.
	    rolePicker.setItems(FXCollections.observableArrayList(found));
	    if (!found.isEmpty()) {
	        rolePicker.getSelectionModel().select(0);
	    }

	    // Make the pop up reasonably tall
	    rolePicker.setVisibleRowCount(Math.min(found.size(), 6));

	    Button goBtn = new Button("Go");
	    goBtn.setOnAction(e -> {
	        String selected = rolePicker.getSelectionModel().getSelectedItem();
	        if (selected == null || selected.isBlank()) {
	            pickLbl.setText("Choose your role: (please select one)");
	            return;
	        }

	        switch (selected.toLowerCase()) {
	            case "student"    -> new StudentHomePage(databaseHelper, user.getUserName()).show(primaryStage);
	            case "admin"      -> new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
	            case "instructor" -> new InstructorHomePage(databaseHelper).show(primaryStage);
	            case "staff"      -> new StaffHomePage(databaseHelper).show(primaryStage);
	            case "reviewer"   -> new ReviewerHomePage(databaseHelper).show(primaryStage);
	            default           -> new UserHomePage(databaseHelper).show(primaryStage);
	        }
	    });

	    HBox roleRow = new HBox(8, pickLbl, rolePicker, goBtn);
	    roleRow.setStyle("-fx-alignment: center;");
	    // ==== END role picker addition ====

		
	    // Button to navigate to the user's respective page based on their role
	    Button continueButton = new Button("Continue to your Page");
	    continueButton.setOnAction(a -> {
	    	String str_role =user.getRole();
	    	System.out.println(str_role);
	    	
	    	if(str_role.equals("admin")) {
	    		new AdminHomePage(databaseHelper, user.getUserName()).show(primaryStage);
	    	}
	    	else if(str_role.equals("user")) {
	    		new UserHomePage(databaseHelper).show(primaryStage);
	    	}
	    });
	    
	    // Button to quit the application
	    Button quitButton = new Button("Quit");
	    quitButton.setOnAction(a -> {
	    	databaseHelper.closeConnection();
	    	Platform.exit(); // Exit the JavaFX application
	    });

	    layout.getChildren().addAll(welcomeLabel,continueButton,quitButton,roleRow);
	    Scene welcomeScene = new Scene(layout, 800, 400);

	    // Set the scene to primary stage
	    primaryStage.setScene(welcomeScene);
	    primaryStage.setTitle("Welcome Page");
    }
}
