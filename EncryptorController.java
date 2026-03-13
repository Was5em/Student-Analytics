package securefile.controller;

import securefile.crypto.AESEncryptor;
import securefile.crypto.FileHasher;
import securefile.model.EncryptedFile;

import java.io.File;
import java.nio.file.Files;

/**
 * Business logic layer — bridges GUI and crypto engine.
 */
public class EncryptorController {

    /**
     * Encrypts the given file with the provided password.
     *
     * @param inputFile Source file to encrypt
     * @param password  User passphrase (will be cleared after use)
     * @return EncryptedFile result with status and metadata
     */
    public EncryptedFile encrypt(File inputFile, char[] password) {
        if (inputFile == null || !inputFile.exists()) {
            return fail(inputFile, null, EncryptedFile.Operation.ENCRYPT, "Input file not found.");
        }
        if (password == null || password.length == 0) {
            return fail(inputFile, null, EncryptedFile.Operation.ENCRYPT, "Password cannot be empty.");
        }

        try {
            // Output file: same location, add .enc extension
            File outputFile = new File(inputFile.getParent(),
                    inputFile.getName() + ".enc");

            // Compute hash of original for display
            byte[] originalBytes = Files.readAllBytes(inputFile.toPath());
            String hashHex = FileHasher.sha256Hex(originalBytes);

            AESEncryptor.encrypt(inputFile, outputFile, password);

            return new EncryptedFile(inputFile, outputFile,
                    EncryptedFile.Operation.ENCRYPT,
                    EncryptedFile.Status.SUCCESS,
                    "File encrypted successfully.",
                    hashHex);

        } catch (Exception e) {
            return fail(inputFile, null, EncryptedFile.Operation.ENCRYPT,
                    "Encryption error: " + e.getMessage());
        }
    }

    /**
     * Decrypts a .enc file with the provided password.
     *
     * @param encFile  The encrypted .enc file
     * @param password User passphrase
     * @return EncryptedFile result with status and restored file path
     */
    public EncryptedFile decrypt(File encFile, char[] password) {
        if (encFile == null || !encFile.exists()) {
            return fail(encFile, null, EncryptedFile.Operation.DECRYPT, "Encrypted file not found.");
        }
        if (password == null || password.length == 0) {
            return fail(encFile, null, EncryptedFile.Operation.DECRYPT, "Password cannot be empty.");
        }

        try {
            File outputDir  = encFile.getParentFile();
            File outputFile = AESEncryptor.decrypt(encFile, outputDir, password);

            byte[] restoredBytes = Files.readAllBytes(outputFile.toPath());
            String hashHex = FileHasher.sha256Hex(restoredBytes);

            return new EncryptedFile(encFile, outputFile,
                    EncryptedFile.Operation.DECRYPT,
                    EncryptedFile.Status.SUCCESS,
                    "File decrypted and integrity verified ✓",
                    hashHex);

        } catch (SecurityException e) {
            return fail(encFile, null, EncryptedFile.Operation.DECRYPT, e.getMessage());
        } catch (Exception e) {
            return fail(encFile, null, EncryptedFile.Operation.DECRYPT,
                    "Decryption error: " + e.getMessage());
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────

    private EncryptedFile fail(File src, File out, EncryptedFile.Operation op, String msg) {
        return new EncryptedFile(src, out, op, EncryptedFile.Status.FAILED, msg, null);
    }
}
