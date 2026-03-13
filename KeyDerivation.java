package securefile.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

/**
 * Derives a 256-bit AES key from a user password using PBKDF2WithHmacSHA256.
 *
 * Why PBKDF2?
 * - Prevents brute-force attacks via iteration count (310,000 rounds)
 * - Salt prevents rainbow table attacks
 * - Industry standard (NIST SP 800-132)
 */
public class KeyDerivation {

    private static final String    ALGORITHM       = "PBKDF2WithHmacSHA256";
    private static final int       ITERATIONS      = 310_000;   // NIST recommended 2023
    private static final int       KEY_LENGTH_BITS = 256;

    /**
     * Derives a 256-bit AES SecretKey from a password and salt.
     *
     * @param password User-provided passphrase (char[] — cleared after use)
     * @param salt     Random 16-byte salt (stored in encrypted file header)
     * @return AES-256 SecretKey
     */
    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        spec = null;  // help GC clear sensitive data
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Securely wipes a password char array from memory.
     * Always call this after key derivation.
     */
    public static void clearPassword(char[] password) {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private KeyDerivation() {}
}
