package application;

import databasePart1.DatabaseHelper;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class AdminService {
    private final DatabaseHelper db;

    public AdminService(DatabaseHelper db) {
        this.db = db;
    }

    public List<User> listAllUsers() throws SQLException {
        return db.getAllUsers();
    }

    public String addRole(String actingAdmin, String targetUser, Role roleToAdd) throws SQLException {
        // Validate inputs
        if (targetUser == null || targetUser.trim().isEmpty()) {
            return "Error: Target username is required";
        }
        
        if (!db.doesUserExist(targetUser)) {
            return "Error: User '" + targetUser + "' does not exist";
        }

        // Get current roles
        String currentRoleString = db.getUserRoleSafe(targetUser);
        
        // Parse current roles into a set to remove duplicates and maintain order
        Set<String> currentRoles = new LinkedHashSet<>();
        if (currentRoleString != null && !currentRoleString.trim().isEmpty()) {
            // FIX: Split by comma and trim each role
            String[] rolesArray = currentRoleString.split("\\s*,\\s*");
            for (String role : rolesArray) {
                String trimmedRole = role.trim();
                if (!trimmedRole.isEmpty()) {
                    currentRoles.add(trimmedRole);
                }
            }
        }

        String roleToAddStr = roleToAdd.name().toLowerCase();
        
        // Check if role already exists
        if (currentRoles.contains(roleToAddStr)) {
            return "User '" + targetUser + "' already has role: " + roleToAddStr;
        }

        // Add the new role
        currentRoles.add(roleToAddStr);
        
        // Create new comma-separated role string
        String newRoleString = String.join(", ", currentRoles); // FIX: Added space after comma
        
        // Update in database
        boolean success = db.updateUserRoles(targetUser, newRoleString);
        
        if (success) {
            return "Added role '" + roleToAddStr + "' to user '" + targetUser + "'. Current roles: " + newRoleString;
        } else {
            return "Error: Failed to update roles for user '" + targetUser + "'";
        }
    }

    public String removeRole(String actingAdmin, String targetUser, Role roleToRemove) throws SQLException {
        // Validate inputs
        if (targetUser == null || targetUser.trim().isEmpty()) {
            return "Error: Target username is required";
        }
        
        if (!db.doesUserExist(targetUser)) {
            return "Error: User '" + targetUser + "' does not exist";
        }

        // Get current roles
        String currentRoleString = db.getUserRoleSafe(targetUser);
        
        // Parse current roles into a set
        Set<String> currentRoles = new LinkedHashSet<>();
        if (currentRoleString != null && !currentRoleString.trim().isEmpty()) {
            // FIX: Split by comma and trim each role
            String[] rolesArray = currentRoleString.split("\\s*,\\s*");
            for (String role : rolesArray) {
                String trimmedRole = role.trim();
                if (!trimmedRole.isEmpty()) {
                    currentRoles.add(trimmedRole);
                }
            }
        }

        String roleToRemoveStr = roleToRemove.name().toLowerCase();
        
        // Check if role exists to remove
        if (!currentRoles.contains(roleToRemoveStr)) {
            return "User '" + targetUser + "' does not have role: " + roleToRemoveStr;
        }

        // Special check: prevent removing the last admin
        if ("admin".equals(roleToRemoveStr)) {
            int adminCount = db.countAdmins();
            if (adminCount <= 1 && currentRoles.contains("admin")) {
                return "Error: Cannot remove the last admin user";
            }
        }

        // Remove the role
        currentRoles.remove(roleToRemoveStr);
        
        // Create new comma-separated role string
        String newRoleString = currentRoles.isEmpty() ? "" : String.join(", ", currentRoles); // FIX: Added space after comma
        
        // Update in database
        boolean success = db.updateUserRoles(targetUser, newRoleString);
        
        if (success) {
            if (newRoleString.isEmpty()) {
                return "Removed role '" + roleToRemoveStr + "' from user '" + targetUser + "'. User now has no roles.";
            } else {
                return "Removed role '" + roleToRemoveStr + "' from user '" + targetUser + "'. Current roles: " + newRoleString;
            }
        } else {
            return "Error: Failed to update roles for user '" + targetUser + "'";
        }
    }
}
