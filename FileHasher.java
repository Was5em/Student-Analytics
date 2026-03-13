package securefile.crypto;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;

/**
 * Computes SHA-256 hashes for file integrity verification.
 */
public class FileHasher {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Computes SHA-256 hash of a byte array.
     */
    public static byte[] sha256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
        return digest.digest(data);
    }

    /**
     * Computes SHA-256 hash of a file on disk.
     */
    public static byte[] sha256(File file) throws Exception {
        return sha256(Files.readAllBytes(file.toPath()));
    }

    /**
     * Returns SHA-256 hash as a readable hex string (for display).
     */
    public static String sha256Hex(byte[] data) throws Exception {
        byte[] hash = sha256(data);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Compares two hash byte arrays in constant time to prevent timing attacks.
     */
    public static boolean verifyHash(byte[] expected, byte[] actual) {
        if (expected.length != actual.length) return false;
        int diff = 0;
        for (int i = 0; i < expected.length; i++) {
            diff |= expected[i] ^ actual[i];
        }
        return diff == 0;
    }

    private FileHasher() {}
}
