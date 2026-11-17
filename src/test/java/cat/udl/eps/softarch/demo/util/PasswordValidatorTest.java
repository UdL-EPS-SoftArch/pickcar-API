package cat.udl.eps.softarch.demo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    void acceptsLettersAndNumbersWithMinLength() {
        assertTrue(PasswordValidator.isValid("abcd1234"));
        assertTrue(PasswordValidator.isValid("Passw0rd-long"));
    }

    @Test
    void rejectsNullAndShortPasswords() {
        assertFalse(PasswordValidator.isValid(null));
        assertFalse(PasswordValidator.isValid("abc123"));
    }

    @Test
    void rejectsOnlyLettersOrOnlyDigits() {
        assertFalse(PasswordValidator.isValid("abcdefgh"));
        assertFalse(PasswordValidator.isValid("12345678"));
        assertFalse(PasswordValidator.isValid("!!!!????"));
    }

    @Test
    void validateThrowsDescriptiveMessages() {
        assertEquals("Password must be at least 8 characters long",
                assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validate("a1")).getMessage());
        assertEquals("Password must contain both letters and numbers",
                assertThrows(IllegalArgumentException.class, () -> PasswordValidator.validate("abcdefgh")).getMessage());
        assertDoesNotThrow(() -> PasswordValidator.validate("abcd1234"));
    }
}
