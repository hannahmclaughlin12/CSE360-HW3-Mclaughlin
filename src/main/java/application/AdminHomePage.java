package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

/**
 * AdminPage class represents the user interface for the admin user.
 * This page displays a simple welcome message for the admin.
 */

public class AdminHomePage {
    /**
     * Displays the admin page in the provided primary stage.
     * @param primaryStage The primary stage where the scene will be displayed.
     */
    
    private final DatabaseHelper databaseHelper;
    private final String adminUsername;

    public AdminHomePage(DatabaseHelper databaseHelper, String adminUsername) {
        this.databaseHelper = databaseHelper;
        this.adminUsername = adminUsername;
    }
    
    public void show(Stage primaryStage) {
        VBox layout = new VBox(15);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
        
        // label to display the welcome message for the admin
        Label adminLabel = new Label("Hello, Admin!");
        adminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label instructionLabel = new Label("Admin Dashboard");
        instructionLabel.setStyle("-fx-font-size: 14px;");

        // Main admin functionality buttons
        Button manageUsersBtn = new Button("Manage Users & Roles");
        manageUsersBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");
        manageUsersBtn.setOnAction(e -> {
            new AdminManagePage(databaseHelper, adminUsername).show(primaryStage);
        });
        
       
        
        Button removeRoleButton = new Button("Delete User Accounts");
        removeRoleButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");
        removeRoleButton.setOnAction(e -> new AccountDeletionPage(databaseHelper, adminUsername).show(primaryStage));

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-pref-width: 250px;");
        logoutBtn.setOnAction(e -> new UserLoginPage(databaseHelper).show(primaryStage));
        
        // Add all buttons to layout
        layout.getChildren().addAll(
            adminLabel, 
            instructionLabel,
            manageUsersBtn, 
           
            removeRoleButton, 
            logoutBtn
        );
        
        Scene adminScene = new Scene(layout, 800, 400);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Admin Dashboard");
    }
}
