package application;

import databasePart1.DatabaseHelper;

import java.sql.SQLException;
import java.util.List;

/**
 * HW3 — Automated test mainline for Hannah McLaughlin.
 *
 * <p>This standalone runner connects to the H2 DB, seeds a few users,
 * then executes five automated tests allocated to me:</p>
 * <ol>
 *   <li>Question with no text</li>
 *   <li>Removing a role a user does not have</li>
 *   <li>Email with two '@' symbols</li>
 *   <li>Email with empty local part (nothing before '@')</li>
 *   <li>Add an answer and then update it</li>
 * </ol>
 *
 * <p>Each test prints a PASS/FAIL line and any useful details.</p>
 *
 * <p><b>How to run:</b> Right-click this file → Run As → Java Application.</p>
 *
 * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
 * <p><b>Design notes:</b> This class mirrors the HW1 “PasswordEvaluationTestingAutomation”
 * pattern (single <code>main</code> that prints human-readable PASS/FAIL lines) so no JUnit
 * is required for HW3. The DB is seeded idempotently so the tests can be re-run safely.</p>
 *
 * @author Hannah McLaughlin
 * @version 1.0
 * @since 1.0
 * @see databasePart1.DatabaseHelper
 * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
 */
public class HW3 {

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Entry point that connects to the database, seeds users, and executes the five
     * allocated automated tests. Results are printed as PASS/FAIL lines to the console.
     * <p>
     * <b>Tests executed:</b>
     * <ol>
     *     <li>Insert a question with empty text (DB should safely store empty string).</li>
     *     <li>Attempt to remove a role the target user does not have (should be rejected).</li>
     *     <li>Validate email with two '@' symbols (should be invalid).</li>
     *     <li>Validate email with empty local part (should be invalid).</li>
     *     <li>Add an answer and then update its text; verify persisted value.</li>
     * </ol>
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    public static void main(String[] args) {
        DatabaseHelper db = new DatabaseHelper();
        try {
            db.connectToDatabase();
            log("Connected to H2 database.");

            // --- Seed users used by tests (idempotent) ---
            seedUser(db, "hannah_admin", "AdminPass1!", "admin", "Hannah Admin", "hannah.admin@example.com");
            seedUser(db, "carol_student", "CarolPass1!", "student", "Carol Student", "carol@example.com");

            // ========== TEST 1: Question with no text ==========
            // Expected behavior: DB layer accepts empty string safely (no crash),
            // and retrieval returns an empty text. (The GUI separately blocks this.)
            try {
                int qId = db.insertQuestion("carol_student", "Empty Text Check", "");
                Question q = db.getQuestion(qId);
                boolean pass = (q != null) && q.getQuestion().isEmpty();
                verdict("T1 Question with no text (DB stores empty safely)", pass,
                        "qId=" + qId + ", retrievedText=\"" + (q == null ? "<null>" : q.getQuestion()) + "\"");
            } catch (Exception e) {
                verdict("T1 Question with no text (exception safety)", false, e.getMessage());
            }

            // ========== TEST 2: Removing role someone doesn’t have ==========
            // Using the AdminService helper you already have.
            try {
                AdminService admin = new AdminService(db);
                // Acting admin tries to remove 'admin' from a pure student
                String msg = admin.removeRole("hannah_admin", "carol_student", Role.admin);
                boolean pass = msg != null && msg.toLowerCase().contains("does not have role");
                verdict("T2 Remove role user doesn’t have (admin from student)", pass, msg);
            } catch (Exception e) {
                verdict("T2 Remove role user doesn’t have (exception safety)", false, e.getMessage());
            }

            // ========== TEST 3: Email with 2 '@' symbols ==========
            try {
                String err = EmailEvaluator.evaluateEmail("han@nah@example.com");
                boolean pass = err != null && !err.isEmpty();
                verdict("T3 Email with two '@' symbols is invalid", pass, err);
            } catch (Exception e) {
                verdict("T3 Email with two '@' symbols (exception safety)", false, e.getMessage());
            }

            // ========== TEST 4: Email with empty local (nothing before '@') ==========
            try {
                String err = EmailEvaluator.evaluateEmail("@example.com");
                boolean pass = err != null && !err.isEmpty();
                verdict("T4 Email empty local part is invalid", pass, err);
            } catch (Exception e) {
                verdict("T4 Email empty local part (exception safety)", false, e.getMessage());
            }

            // ========== TEST 5: Add and update answer (given) ==========
            try {
                int q2Id = db.insertQuestion("carol_student", "Answer Update", "Will this be updated?");
                db.setAnswer("userX", "Initial answer", q2Id);

                Integer answerId = db.getLatestAnswerIdForQuestion(q2Id);
                if (answerId == null) {
                    verdict("T5 Add answer then update text", false, "No answer id returned");
                } else {
                    db.updateAnswerText(answerId, "Revised answer text");
                    String newText = db.getAnswerText(answerId);
                    boolean pass = "Revised answer text".equals(newText);
                    verdict("T5 Add answer then update text", pass,
                            "answerId=" + answerId + ", newText=\"" + newText + "\"");
                }
            } catch (Exception e) {
                verdict("T5 Add answer then update text (exception safety)", false, e.getMessage());
            }

            db.closeConnection();
            log("Connection closed.");
        } catch (SQLException e) {
            e.printStackTrace();
            log("FATAL: " + e.getMessage());
        }
    }

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Seed or update a user’s role string so the tests are repeatable across runs.
     *
     * @param db        shared {@link DatabaseHelper} instance
     * @param username  unique username to create or update
     * @param password  password for the user (plain here for test data)
     * @param roles     comma-separated roles string (e.g. {@code "admin"} or {@code "student,instructor"})
     * @param fullName  user’s display name
     * @param email     user’s email
     * @throws SQLException if the insert or update fails
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    private static void seedUser(DatabaseHelper db, String username, String password, String roles,
                                 String fullName, String email) throws SQLException {
        if (!db.doesUserExist(username)) {
            User u = new User(username, password, Role.student, fullName, email);
            u.setRoles(roles);
            u.cleanRoleList();
            db.register(u);
        } else {
            db.updateUserRoles(username, roles);
        }
    }

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Print a single PASS/FAIL verdict with an optional detail message.
     *
     * @param name   short test name to display
     * @param pass   {@code true} for PASS; {@code false} for FAIL
     * @param detail optional human-readable context (may be {@code null})
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    private static void verdict(String name, boolean pass, String detail) {
        System.out.println((pass ? "[PASS] " : "[FAIL] ") + name +
                (detail == null || detail.isBlank() ? "" : " — " + detail));
    }

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Convenience logger used by the test mainline.
     *
     * @param msg text to print to the console
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    private static void log(String msg) {
        System.out.println(msg);
    }
}
