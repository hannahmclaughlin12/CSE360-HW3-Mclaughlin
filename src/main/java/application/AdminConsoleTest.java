package application;

import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * AdminConsoleTest
 *
 * Seeds a small set of users (idempotent), then runs through the admin role tests:
 *  - listing all users
 *  - attempting to remove admin from self (should fail)
 *  - removing admin from another user (allowed when safe)
 *  - preventing removal of the last admin
 *  - adding a role to a user
 *
 * Run as: Right-click -> Run As -> Java Application
 */
public class AdminConsoleTest {

    public static void main(String[] args) {
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();

            // If your DB schema is old you can call db.upgradeTables(); here for local testing,
            // or delete ~/FoundationDatabase.mv.db so the schema is recreated. Be careful: deleting
            // removes local data.
            db.upgradeTables();

            // Seed users (idempotent): alice, bob, carol, dave
            seedUser(db, "alice", "AlicePass1!", "admin,instructor", "Alice Admin", "alice@example.com");
            seedUser(db, "bob", "BobPass1!", "admin", "Bob Builder", "bob@example.com");
            seedUser(db, "carol", "CarolPass1!", "student", "Carol Student", "carol@example.com");
            seedUser(db, "dave", "DavePass1!", "student", "Dave Student", "dave@example.com");

            AdminService adminSvc = new AdminService(db);

            System.out.println("\n=== Initial users ===");
            printList(adminSvc.listAllUsers());

            System.out.println("\n=== Attempt: alice removes admin from herself (should fail) ===");
            try {
                adminSvc.removeRoleFromUser("alice", "alice", UserRole.admin);
            } catch (Exception e) {
                System.out.println("Expected failure: " + e.getMessage());
            }

            System.out.println("\n=== Bob removes admin from Carol (Carol is not admin so no change) ===");
            try {
                adminSvc.removeRoleFromUser("bob", "carol", UserRole.admin);
            } catch (Exception e) {
                System.out.println("Unexpected: " + e.getMessage());
            }

            System.out.println("\n=== Bob removes admin from alice (allowed because bob is acting and there will still be an admin) ===");
            try {
                adminSvc.removeRoleFromUser("bob", "alice", UserRole.admin);
                System.out.println("Role 'admin' removed from alice");
            } catch (Exception e) {
                System.out.println("Unexpected failure: " + e.getMessage());
            }
            printList(adminSvc.listAllUsers());

            System.out.println("\n=== Now Bob tries to remove admin from himself (should fail) ===");
            try {
                adminSvc.removeRoleFromUser("bob", "bob", UserRole.admin);
            } catch (Exception e) {
                System.out.println("Expected failure: " + e.getMessage());
            }

            System.out.println("\n=== Remove second admin to test last-admin protection ===");
            try {
                System.out.println("Attempting to remove admin role from bob (this would leave zero admins)...");
                // Use alice as acting admin for attempt (alice currently not admin after earlier removal)
                adminSvc.removeRoleFromUser("alice", "bob", UserRole.admin);
            } catch (Exception e) {
                System.out.println("Expected/Handled: " + e.getMessage());
            }

            System.out.println("\n=== Add a role to carol (student -> reviewer) ===");
            try {
                adminSvc.addRoleToUser("bob", "carol", UserRole.reviewer);
                System.out.println("Role 'reviewer' added to carol");
            } catch (Exception e) {
                System.out.println("Unexpected: " + e.getMessage());
            }
            printList(adminSvc.listAllUsers());

            db.closeConnection();
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedUser(DatabaseHelper db, String username, String password, String roleString, String fullName, String email) throws SQLException {
        if (!db.doesUserExist(username)) {
            // Create user with a primary role (first role in list) to satisfy constructor,
            // then setRoles to the desired comma-separated list before registering.
            UserRole primary = UserRole.student;
            if (roleString != null && roleString.toLowerCase().contains("admin")) primary = UserRole.admin;
            User u = new User(username, password, primary, fullName, email);
            u.setRoles(roleString);
            u.cleanRoleList();
            db.register(u);
        } else {
            // Ensure existing user has the expected role string (idempotent)
            db.updateUserRoles(username, roleString == null ? "" : roleString);
        }
    }

    private static void printList(List<User> users) {
        users.forEach(u -> System.out.println(u.getUserName() + " | " + u.getName() + " | " + u.getEmail() + " | [" + u.getRole() + "]"));
    }
}
