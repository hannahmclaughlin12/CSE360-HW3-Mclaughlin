package application;

/** Minimal centralized validators used by the UI. Additive: no one else is forced to use it. */
public class InputValidator {

    public static boolean isValidUserName(String username) {
        if (username == null) return false;
        return username.matches("^[A-Za-z][A-Za-z0-9._-]{3,15}$"); // first alpha, 4-16 chars
    }

    public static boolean isValidEmail(String email) {
        if (email == null) return false;
        return email.matches("^[A-Za-z0-9+_.%-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        if (password.length() > 20) return false;
        if (password.matches(".*\\s+.*")) return false; // no whitespace
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[~`!@#$%^&*()_+\\-={}:;\"'<>.,?/\\\\\\[\\]|].*");
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    public static boolean isValidName(String name) {
        if (name == null) return false;
        return name.matches("^[A-Za-z ]{1,60}$");
    }
}
