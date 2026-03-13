package securefile.util;

import java.io.File;

/**
 * File utility helpers.
 */
public class FileUtil {

    /** Human-readable file size (e.g. "1.4 MB") */
    public static String readableSize(long bytes) {
        if (bytes < 1024)             return bytes + " B";
        if (bytes < 1024 * 1024)      return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /** Returns file extension in uppercase (e.g. "PDF") */
    public static String extension(File file) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).toUpperCase() : "FILE";
    }

    /** Truncates a long filename for display */
    public static String shortName(File file, int maxLen) {
        String name = file.getName();
        return name.length() <= maxLen ? name : name.substring(0, maxLen - 3) + "...";
    }

    private FileUtil() {}
}
