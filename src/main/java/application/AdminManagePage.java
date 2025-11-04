package application;

import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;
import javafx.scene.input.ClipboardContent;

/**
 * AdminManagePage - list all users and allow adding/removing roles, generating invitations.
 */
public class AdminManagePage {

    private final AdminService adminService;
    private final DatabaseHelper db;
    private final String actingAdmin;

    public AdminManagePage(DatabaseHelper db, String actingAdmin) {
        this.db = db;
        this.actingAdmin = actingAdmin;
        this.adminService = new AdminService(db);
    }

    public void show(Stage stage) {
        TextArea usersArea = new TextArea();
        usersArea.setEditable(false);
        usersArea.setPrefHeight(240);

        TextField targetUserField = new TextField();
        targetUserField.setPromptText("target userName");

        ComboBox<Role> roleBox = new ComboBox<>();
        roleBox.setItems(FXCollections.observableArrayList(Role.admin, Role.student, Role.instructor, Role.staff, Role.reviewer));
        roleBox.setValue(Role.student);

        Label msg = new Label();

        Button refreshBtn = new Button("Refresh Users");
        refreshBtn.setOnAction(e -> {
            try { 
                refreshUsers(usersArea); 
                msg.setText(""); 
            } catch (SQLException ex) { 
                msg.setText("DB error: " + ex.getMessage()); 
            }
        });

        Button addBtn = new Button("Add Role");
        addBtn.setOnAction(e -> {
            String target = targetUserField.getText().trim();
            Role r = roleBox.getValue();
            if (target.isEmpty()) { 
                msg.setText("Enter target userName"); 
                return; 
            }
            try {
                String res = adminService.addRole(actingAdmin, target, r);
                msg.setText(res);
                refreshUsers(usersArea);
            } catch (SQLException ex) {
                msg.setText("DB error: " + ex.getMessage());
            }
        });

        Button removeBtn = new Button("Remove Role");
        removeBtn.setOnAction(e -> {
            String target = targetUserField.getText().trim();
            Role r = roleBox.getValue();
            if (target.isEmpty()) { 
                msg.setText("Enter target userName"); 
                return; 
            }
            try {
                String res = adminService.removeRole(actingAdmin, target, r);
                msg.setText(res);
                refreshUsers(usersArea);
            } catch (SQLException ex) {
                msg.setText("DB error: " + ex.getMessage());
            }
        });

        // Button to generate invitation codes
        Button generateInviteBtn = new Button("Generate Invitation");
        generateInviteBtn.setOnAction(e -> {
            Role selectedRole = roleBox.getValue();
            if (selectedRole == null) {
                msg.setText("Please select a role for the invitation");
                return;
            }
            try {
                String invitationCode = db.generateInvitationCode(selectedRole.name().toLowerCase());
                if (invitationCode != null) {
                    msg.setText("Invitation code for " + selectedRole + ": " + invitationCode);
                    
                    // Show the invitation code in a dialog for better visibility
                    showInvitationDialog(invitationCode, selectedRole);
                } else {
                    msg.setText("Failed to generate invitation code");
                }
            } catch (Exception ex) {
                msg.setText("Error generating invitation: " + ex.getMessage());
            }
        });

        Button backBtn = new Button("Back to Admin Home");
        backBtn.setOnAction(e -> {
            new AdminHomePage(db, actingAdmin).show(stage);
        });

        // Layout organization
        HBox roleControls = new HBox(8, new Label("Role:"), roleBox, addBtn, removeBtn, generateInviteBtn);
        roleControls.setStyle("-fx-alignment: center-left;");
        
        HBox targetControl = new HBox(8, new Label("Target User:"), targetUserField, refreshBtn);
        targetControl.setStyle("-fx-alignment: center-left;");

        VBox layout = new VBox(10, 
            new Label("User Management - Add/Remove Roles"),
            usersArea, 
            targetControl, 
            roleControls, 
            msg,
            backBtn
        );
        layout.setStyle("-fx-padding: 15; -fx-alignment: center-left;");

        // initial refresh
        try { 
            refreshUsers(usersArea); 
        } catch (SQLException ex) { 
            msg.setText("DB error: " + ex.getMessage()); 
        }

        stage.setScene(new Scene(layout, 850, 500));
        stage.setTitle("Admin - Manage Users & Roles");
        stage.show();
    }

    /**
     * Shows a dialog with the generated invitation code
     */
    private void showInvitationDialog(String invitationCode, Role role) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Invitation Code Generated");
        
        VBox dialogVBox = new VBox(10);
        dialogVBox.setStyle("-fx-padding: 20; -fx-alignment: center;");
        
        Label titleLabel = new Label("Invitation Code Generated");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        Label codeLabel = new Label("Code: " + invitationCode);
        codeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2E8B57;");
        
        Label roleLabel = new Label("Role: " + role);
        roleLabel.setStyle("-fx-font-size: 14px;");
        
        Label instructionsLabel = new Label("Share this code with the user to register with the specified role.");
        instructionsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        instructionsLabel.setWrapText(true);
        
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> dialogStage.close());
        
        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(invitationCode);
            javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
            copyButton.setText("Copied!");
        });
        
        HBox buttonBox = new HBox(10, copyButton, closeButton);
        buttonBox.setStyle("-fx-alignment: center;");
        
        dialogVBox.getChildren().addAll(titleLabel, codeLabel, roleLabel, instructionsLabel, buttonBox);
        
        Scene dialogScene = new Scene(dialogVBox, 400, 200);
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }

    private void refreshUsers(TextArea usersArea) throws SQLException {
        List<User> users = adminService.listAllUsers();
        StringBuilder sb = new StringBuilder();
        sb.append("Username | Name | Email | Roles\n");
        sb.append("----------------------------------------\n");
        
        for (User u : users) {
            // Fix the role formatting
            String roleString = formatRoleString(u.getRole());
            
            sb.append(u.getUserName()).append(" | ")
              .append(u.getName() == null ? "" : u.getName()).append(" | ")
              .append(u.getEmail() == null ? "" : u.getEmail()).append(" | [")
              .append(roleString).append("]\n");
        }
        usersArea.setText(sb.toString());
    }

    /**
     * Fixes role string formatting by adding commas between roles
     */
    private String formatRoleString(String roleString) {
        if (roleString == null || roleString.trim().isEmpty()) {
            return "";
        }
        
        // If it's already formatted with commas, return as is
        if (roleString.contains(",")) {
            return roleString;
        }
        
        // Parse concatenated roles and add commas
        StringBuilder formatted = new StringBuilder();
        String temp = roleString.toLowerCase();
        
        java.util.List<String> foundRoles = new java.util.ArrayList<>();
        if (temp.contains("admin")) foundRoles.add("admin");
        if (temp.contains("student")) foundRoles.add("student");
        if (temp.contains("instructor")) foundRoles.add("instructor");
        if (temp.contains("staff")) foundRoles.add("staff");
        if (temp.contains("reviewer")) foundRoles.add("reviewer");
        
        return String.join(", ", foundRoles);
    }
    
}
