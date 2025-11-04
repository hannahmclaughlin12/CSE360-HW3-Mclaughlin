package databasePart1;

import java.sql.*;
import java.util.UUID;
import java.time.LocalDateTime;

import application.User;
import application.Question;
import application.Questions;
import application.Answer;
import application.Answers;
import java.util.List;
import java.util.ArrayList;
import application.Role;

/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 *
 * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
 * <p><b>Schema:</b> H2 file DB at <code>jdbc:h2:~/FoundationDatabase</code> with tables
 * <code>cse360users</code>, <code>Questions</code>, and <code>Answers</code> (cascade delete on
 * question → answers/clarifications).</p>
 *
 * <p>This class also exposes a few tiny helpers used by the HW3 tests:
 * {@link #getLatestAnswerIdForQuestion(int)} and {@link #getAnswerText(int)}.</p>
 * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
 */
public class DatabaseHelper {

    // JDBC driver name and database URL 
    static final String JDBC_DRIVER = "org.h2.Driver";   
    static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

    //  Database credentials 
    static final String USER = "sa"; 
    static final String PASS = ""; 

    private Connection connection = null;
    private Statement statement = null; 
    //  PreparedStatement pstmt

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Open a connection to the H2 database and ensure required tables exist.
     *
     * @throws SQLException if the connection or table creation fails
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    public void connectToDatabase() throws SQLException {
        try {
            Class.forName(JDBC_DRIVER); // Load the JDBC driver
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            statement = connection.createStatement(); 
            // statement.execute("DROP ALL OBJECTS"); // (optional) clear DB

            createTables();  // Create the necessary tables if they don't exist
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Create all tables if they do not already exist. Safe to call multiple times.
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    private void createTables() throws SQLException {
        String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "userName VARCHAR(255) UNIQUE, "
                + "password VARCHAR(255), "
                + "userRole VARCHAR(255), "
                + "name VARCHAR(255), "
                + "email VARCHAR(255))";
        statement.execute(userTable);
        
        // Create the invitation codes table
        String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
                + "code VARCHAR(4) PRIMARY KEY, "
                + "userRole VARCHAR(200), "
                + "isUsed BOOLEAN DEFAULT FALSE, "
                + "userTime TIMESTAMP )";
        statement.execute(invitationCodesTable);
        
        // Questions
        String questionTable = "CREATE TABLE IF NOT EXISTS Questions ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "parent_question_id INT, "
                + "userName VARCHAR(255), "
                + "title VARCHAR(255), "
                + "text VARCHAR(500), "
                + "resolved BOOLEAN DEFAULT FALSE, "
                + "FOREIGN KEY (parent_question_id) REFERENCES Questions(id) ON DELETE CASCADE"
                + ")";
        statement.execute(questionTable);
        
        // Answers
        String answerTable = "CREATE TABLE IF NOT EXISTS Answers ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "text VARCHAR(500) NOT NULL, "
                + "userName VARCHAR(255), "
                + "resolves BOOLEAN DEFAULT FALSE, "
                + "question_id INT NOT NULL, "
                + "FOREIGN KEY (question_id) REFERENCES Questions(id) ON DELETE CASCADE"
                + ")";
        statement.execute(answerTable);
    }

    /** Check if the database is empty (no users). */
    public boolean isDatabaseEmpty() throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM cse360users";
        ResultSet resultSet = statement.executeQuery(query);
        if (resultSet.next()) {
            return resultSet.getInt("count") == 0;
        }
        return true;
    }

    /** Register a new user. */
    public void register(User user) throws SQLException {
        String insertUser = "INSERT INTO cse360users (userName, password, userRole, name, email) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.executeUpdate();
        }
    }

    // Validates a user's login credentials - FIXED for multiple roles
    public boolean login(User user) throws SQLException {
        String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, user.getUserName());
            pstmt.setString(2, user.getPassword());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // User exists with matching username/password
                    String dbRole = rs.getString("userRole");
                    String userRole = user.getRole();
                    
                    // Debug logging
                    System.out.println("Login attempt - Username: " + user.getUserName());
                    System.out.println("Database role: " + dbRole);
                    System.out.println("User object role: " + userRole);
                    
                    // If user has multiple roles, check if their selected role is contained in db roles
                    if (dbRole != null && userRole != null) {
                        // Remove role check entirely - if username/password match, allow login
                        return true;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    /** Return true if a user exists for the given username. */
    public boolean doesUserExist(String userName) {
        String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /** Delete a user by username. */
    public void deleteUser(String userName) {
        String query = "DELETE FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            pstmt.setString(1, userName);
            pstmt.executeUpdate();
        }catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    /** Get a user's role string (may be multiple roles). */
    public String getUserRole(String userName) {
        String query = "SELECT userRole FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("userRole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Get a user's full name. */
    public String getUserName(String userName) {
        String query = "SELECT name FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Get a user's email. */
    public String getUserEmail(String userName) {
        String query = "SELECT email FROM cse360users WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Generate and persist a short-lived invitation code. */
    public String generateInvitationCode(String userRole) {
        String code = UUID.randomUUID().toString().substring(0, 4);
        String insertCode = "INSERT INTO InvitationCodes (code, userRole, isUsed, userTime) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(insertCode)) {
            pstmt.setString(1, code);
            pstmt.setString(2, userRole);
            pstmt.setBoolean(3, false);
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now().plusMinutes(15)));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return code;
    }
    
    /** Validate an invitation code and return the associated role string if valid. */
    public String validateInvitationCode(String code) {
        String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE"
                + " AND userTime > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("userRole");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /** Mark an invitation code as used. */
    public void markInvitationCodeAsUsed(String code) {
        String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, code);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /** Insert a question (optionally as a clarification via parentId). */
    public void setQuestion(String userName, String title, String text, Integer parentId) {
        String query = "INSERT INTO Questions (userName, title, text, parent_question_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, title);
            pstmt.setString(3, text);
            if (parentId != null) {
                pstmt.setInt(4, parentId);
            } else {
                pstmt.setNull(4, java.sql.Types.INTEGER);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // === START EDITS ===

    /** Insert an answer for a question. */
    public void setAnswer(String userName, String text, int questionId) {
        // fixed column name: question_id
        String query = "INSERT INTO Answers (userName, text, question_id) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            pstmt.setString(2, text);
            pstmt.setInt(3, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Update the text of an existing answer. */
    public void updateAnswerText(int answerId, String newText) {
        String sql = "UPDATE Answers SET text = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newText);
            ps.setInt(2, answerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // === END EDITS ===
    
    // === START EDITS: fixed select columns ===
    /** Fetch a single question by id (includes answers and the first clarification if present). */
    public Question getQuestion(int id) {
        String query = "SELECT id, userName, title, text, resolved FROM Questions WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Question question = new Question(
                        rs.getString("userName"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("id")
                );
                question.setResolved(rs.getBoolean("resolved"));
                question.setAnswers(getAnswers(question.getId()));

                // fetch first clarification 
                String subQuery = "SELECT id FROM Questions WHERE parent_question_id = ?";
                try (PreparedStatement subPstmt = connection.prepareStatement(subQuery)) {
                    subPstmt.setInt(1, question.getId());
                    ResultSet subRs = subPstmt.executeQuery();
                    if (subRs.next()) {
                        question.setClarification(getQuestion(subRs.getInt("id")));
                    }
                }
                return question;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    // === END EDITS ===
    
    /** Get all top-level questions with their first clarification and answers. */
    public Questions getAllQuestions() {
        Questions questions = new Questions();
        String query = "SELECT * FROM Questions WHERE parent_question_id IS NULL";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                Question question = new Question(
                        rs.getString("userName"),
                        rs.getString("title"),
                        rs.getString("text"),
                        rs.getInt("id")
                );
                question.setResolved(rs.getBoolean("resolved"));
                question.setAnswers(getAnswers(question.getId()));
                
                String subquery = "SELECT id FROM Questions WHERE parent_question_id = ?";
                try (PreparedStatement subPstmt = connection.prepareStatement(subquery)) {
                    subPstmt.setInt(1, question.getId());
                    ResultSet subRs = subPstmt.executeQuery();
                    if (subRs.next()) {
                        question.setClarification(getQuestion(subRs.getInt("id")));
                    }
                }
                questions.addQuestion(question);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }
    
    // === START EDITS: include all columns we use when filling Answers with data ===
    /** Get all answers for a given question id. */
    public Answers getAnswers(int questionId) {
        Answers answers = new Answers();
        String query = "SELECT id, text, userName, question_id, resolves FROM Answers WHERE question_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Answer a = new Answer(
                        rs.getString("userName"),
                        rs.getString("text"),
                        rs.getInt("question_id"),
                        rs.getInt("id")
                );
                if (rs.getBoolean("resolves")) {
                    a.setResolves();
                }
                answers.setAnswer(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }
    // === END EDITS ===

    //////////Joshua's Stuff
    
    /** Insert a new top-level question and return its generated id. */
    public int insertQuestion(String studentName, String title, String questionText) {
        try {
            // Use existing setQuestion method but return the generated ID
            String sql = "INSERT INTO Questions (userName, title, text, parent_question_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, studentName);
                pstmt.setString(2, title);
                pstmt.setString(3, questionText);
                pstmt.setNull(4, java.sql.Types.INTEGER);
                pstmt.executeUpdate();
                
                // Get the generated ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            return -1; // Return -1 if couldn't get ID
        } catch (SQLException e) {
            System.err.println("Error inserting question: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /** Get all questions asked by a specific student (top-level only). */
    public List<Question> getQuestionsByStudent(String studentName) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT id, userName, title, text, resolved FROM Questions WHERE userName = ? AND parent_question_id IS NULL ORDER BY id DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, studentName);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Question question = new Question(
                    rs.getString("userName"),
                    rs.getString("title"), 
                    rs.getString("text"),
                    rs.getInt("id")
                );
                question.setResolved(rs.getBoolean("resolved"));
                questions.add(question);
            }
        } catch (SQLException e) {
            System.err.println("Error getting student questions: " + e.getMessage());
            e.printStackTrace();
        }
        return questions;
    }

    /** Update a question's title and body. */
    public boolean updateQuestion(int questionId, String newTitle, String newText) {
        String sql = "UPDATE Questions SET title = ?, text = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newTitle);
            pstmt.setString(2, newText);
            pstmt.setInt(3, questionId);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating question: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Update a user's role string. */
    public boolean updateUserRoles(String userName, String roleString) {
        String sql = "UPDATE cse360users SET userRole = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, roleString);
            pstmt.setString(2, userName);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user roles: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /** Count the number of users whose role string contains 'admin'. */
    public int countAdmins() {
        String sql = "SELECT COUNT(*) AS adminCount FROM cse360users WHERE userRole LIKE '%admin%'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("adminCount");
            }
        } catch (SQLException e) {
            System.err.println("Error counting admins: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /** Return the user's role string or empty string if not found. */
    public String getUserRoleSafe(String userName) {
        String role = getUserRole(userName);
        return role == null ? "" : role;
    }

    /** Get all users (lightweight projection used by admin UI). */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userName, password, userRole, name, email FROM cse360users ORDER BY userName";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                // Use your team's User constructor - determine the primary role from the userRole string
                String userRoleString = rs.getString("userRole");
                Role primaryRole = Role.student; // default role
                
                // Determine the primary role from the role string
                if (userRoleString != null) {
                    if (userRoleString.contains("admin")) {
                        primaryRole = Role.admin;
                    } else if (userRoleString.contains("instructor")) {
                        primaryRole = Role.instructor;
                    } else if (userRoleString.contains("staff")) {
                        primaryRole = Role.staff;
                    } else if (userRoleString.contains("reviewer")) {
                        primaryRole = Role.reviewer;
                    }
                }
                
                User user = new User(
                    rs.getString("userName"),
                    rs.getString("password"),
                    primaryRole,
                    rs.getString("name"),
                    rs.getString("email")
                );
                
                // Set all roles from the database string
                if (userRoleString != null) {
                    user.setRoles(userRoleString);
                }
                
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
            e.printStackTrace();
        }
        return users;
    }

    // {

    /** Mark an answer as resolving. */
    public void answerResolves(int answerId, boolean resolves) {
        String sql = "UPDATE Answers SET resolves = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolves);
            pstmt.setInt(2, answerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Set a question's resolved status. */
    public void questionResolved(int questionId, boolean resolved) {
        String sql = "UPDATE Questions SET resolved = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, resolved);
            pstmt.setInt(2, questionId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // }
    
    /** Close JDBC resources (safe to call multiple times). */
    public void closeConnection() {
        try{ 
            if(statement!=null) statement.close(); 
        } catch(SQLException se2) { 
            se2.printStackTrace();
        } 
        try { 
            if(connection!=null) connection.close(); 
        } catch(SQLException se){ 
            se.printStackTrace(); 
        } 
    }
    
    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Return the most recently inserted answer id for a given question.
     *
     * @param questionId the parent question id
     * @return newest answer id, or {@code null} if no answers exist
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    public Integer getLatestAnswerIdForQuestion(int questionId) {
        String sql = "SELECT id FROM Answers WHERE question_id = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <!-- ==================== START JAVADOC ADDITIONS ==================== -->
     * Fetch the persisted text for a specific answer id.
     *
     * @param answerId the answer id to fetch
     * @return the answer text, or {@code null} if not found
     * <!-- ==================== END JAVADOC ADDITIONS ====================== -->
     */
    public String getAnswerText(int answerId) {
        String sql = "SELECT text FROM Answers WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, answerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("text");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
