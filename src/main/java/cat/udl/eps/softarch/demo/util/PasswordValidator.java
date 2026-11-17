package cat.udl.eps.softarch.demo.util;

/**
 * Password policy: at least {@value #MIN_LENGTH} characters, mixing letters and digits.
 */
public final class PasswordValidator {

    public static final int MIN_LENGTH = 8;

    private PasswordValidator() {
    }

    /** @return true if the password satisfies the policy; false for null or non-compliant values. */
    public static boolean isValid(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    /**
     * @throws IllegalArgumentException with a descriptive message if the password is not valid
     */
    public static void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }
        if (!isValid(password)) {
            throw new IllegalArgumentException("Password must contain both letters and numbers");
        }
    }
}
