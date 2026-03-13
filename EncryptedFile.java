package securefile.model;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Metadata model representing an encrypted file operation result.
 */
public class EncryptedFile {

    public enum Operation { ENCRYPT, DECRYPT }
    public enum Status    { SUCCESS, FAILED }

    private final File        sourceFile;
    private final File        outputFile;
    private final Operation   operation;
    private final Status      status;
    private final String      message;
    private final LocalDateTime timestamp;
    private final String      sha256Hash;

    public EncryptedFile(File sourceFile, File outputFile, Operation operation,
                         Status status, String message, String sha256Hash) {
        this.sourceFile = sourceFile;
        this.outputFile = outputFile;
        this.operation  = operation;
        this.status     = status;
        this.message    = message;
        this.sha256Hash = sha256Hash;
        this.timestamp  = LocalDateTime.now();
    }

    public File        getSourceFile() { return sourceFile; }
    public File        getOutputFile() { return outputFile; }
    public Operation   getOperation()  { return operation; }
    public Status      getStatus()     { return status; }
    public String      getMessage()    { return message; }
    public String      getSha256Hash() { return sha256Hash; }
    public LocalDateTime getTimestamp(){ return timestamp; }

    public boolean isSuccess() { return status == Status.SUCCESS; }

    @Override
    public String toString() {
        return String.format("[%s] %s → %s | %s | %s",
                operation, sourceFile.getName(),
                outputFile != null ? outputFile.getName() : "—",
                status, message);
    }
}
