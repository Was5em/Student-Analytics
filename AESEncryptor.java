package securefile.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;

/**
 * Handles AES-256-CBC encryption and decryption of files.
 *
 * File format (encrypted output):
 * ┌──────────────────────────────────────────┐
 * │  MAGIC (8 bytes)  "SFECRPT\0"            │
 * │  VERSION (1 byte) 0x01                   │
 * │  SALT    (16 bytes)                      │
 * │  IV      (16 bytes)                      │
 * │  SHA256  (32 bytes) hash of plaintext    │
 * │  ORIGINAL FILENAME LENGTH (4 bytes)      │
 * │  ORIGINAL FILENAME (variable)            │
 * │  ENCRYPTED CONTENT (variable)            │
 * └──────────────────────────────────────────┘
 */
public class AESEncryptor {

    private static final String ALGORITHM  = "AES/CBC/PKCS5Padding";
    private static final byte[] MAGIC      = {0x53, 0x46, 0x45, 0x43, 0x52, 0x50, 0x54, 0x00};
    private static final byte   VERSION    = 0x01;
    private static final int    IV_SIZE    = 16;
    private static final int    SALT_SIZE  = 16;

    /**
     * Encrypts a file using AES-256-CBC with the given password.
     *
     * @param inputFile  The file to encrypt
     * @param outputFile The destination .enc file
     * @param password   User passphrase
     * @throws Exception on any crypto or I/O error
     */
    public static void encrypt(File inputFile, File outputFile, char[] password) throws Exception {
        // 1. Read plaintext
        byte[] plaintext = Files.readAllBytes(inputFile.toPath());

        // 2. Generate random salt and IV
        SecureRandom rng  = new SecureRandom();
        byte[] salt = new byte[SALT_SIZE];
        byte[] iv   = new byte[IV_SIZE];
        rng.nextBytes(salt);
        rng.nextBytes(iv);

        // 3. Derive 256-bit key from password + salt
        SecretKey key = KeyDerivation.deriveKey(password, salt);

        // 4. Compute SHA-256 hash of plaintext (for integrity check)
        byte[] hash = FileHasher.sha256(plaintext);

        // 5. Encrypt
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] ciphertext = cipher.doFinal(plaintext);

        // 6. Write structured output file
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(outputFile)))) {

            out.write(MAGIC);
            out.writeByte(VERSION);
            out.write(salt);
            out.write(iv);
            out.write(hash);

            // Store original filename so we can restore it on decrypt
            byte[] nameBytes = inputFile.getName().getBytes("UTF-8");
            out.writeInt(nameBytes.length);
            out.write(nameBytes);

            out.write(ciphertext);
        }
    }

    /**
     * Decrypts a .enc file and restores the original file.
     *
     * @param encryptedFile The .enc file to decrypt
     * @param outputDir     Directory where the restored file will be saved
     * @param password      User passphrase
     * @return The restored output file
     * @throws Exception on wrong password, tampered file, or I/O error
     */
    public static File decrypt(File encryptedFile, File outputDir, char[] password) throws Exception {
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(encryptedFile)))) {

            // 1. Validate magic header
            byte[] magic = new byte[MAGIC.length];
            in.readFully(magic);
            for (int i = 0; i < MAGIC.length; i++) {
                if (magic[i] != MAGIC[i]) {
                    throw new IllegalArgumentException("Not a valid SecureFile encrypted file.");
                }
            }

            // 2. Check version
            byte version = in.readByte();
            if (version != VERSION) {
                throw new IllegalArgumentException("Unsupported file version: " + version);
            }

            // 3. Read header fields
            byte[] salt = new byte[SALT_SIZE];
            byte[] iv   = new byte[IV_SIZE];
            byte[] storedHash = new byte[32];
            in.readFully(salt);
            in.readFully(iv);
            in.readFully(storedHash);

            // 4. Read original filename
            int nameLen   = in.readInt();
            byte[] nameBytes = new byte[nameLen];
            in.readFully(nameBytes);
            String originalName = new String(nameBytes, "UTF-8");

            // 5. Read ciphertext
            byte[] ciphertext = in.readAllBytes();

            // 6. Derive key and decrypt
            SecretKey key = KeyDerivation.deriveKey(password, salt);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] plaintext;
            try {
                plaintext = cipher.doFinal(ciphertext);
            } catch (Exception e) {
                throw new SecurityException("Decryption failed — wrong password or corrupted file.");
            }

            // 7. Verify integrity
            byte[] computedHash = FileHasher.sha256(plaintext);
            if (!java.util.Arrays.equals(storedHash, computedHash)) {
                throw new SecurityException("File integrity check FAILED — file may have been tampered with!");
            }

            // 8. Write restored file
            File outputFile = new File(outputDir, "decrypted_" + originalName);
            Files.write(outputFile.toPath(), plaintext);
            return outputFile;
        }
    }
}
