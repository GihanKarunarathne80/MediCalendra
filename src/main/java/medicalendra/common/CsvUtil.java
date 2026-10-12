package medicalendra.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared utility for consistent CSV serialization and parsing across entities.
 * Handles backslash-escaped commas (\,) and backslash-escaped backslashes (\\),
 * skips comment and blank lines, and guarantees full field extraction without
 * relying on zero-width lookbehind regular expressions.
 */
public final class CsvUtil {

    private CsvUtil() {}

    /**
     * Checks whether a line is blank or is a recognized comment line (starting with // or #).
     */
    public static boolean isCommentOrBlank(String line) {
        if (line == null) {
            return true;
        }
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#");
    }

    /**
     * Escapes backslashes first, then commas.
     */
    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace(",", "\\,");
    }

    /**
     * Unescapes escaped commas and escaped backslashes.
     */
    public static String unescape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\,", ",").replace("\\\\", "\\");
    }

    /**
     * Parses a CSV line into individual unescaped fields using a deterministic character scanner.
     */
    public static List<String> parseLine(String line) {
        if (line == null) {
            return Collections.emptyList();
        }
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inEscape = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inEscape) {
                if (c == ',' || c == '\\') {
                    sb.append(c);
                } else {
                    sb.append('\\').append(c);
                }
                inEscape = false;
            } else if (c == '\\') {
                inEscape = true;
            } else if (c == ',') {
                fields.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        if (inEscape) {
            sb.append('\\');
        }
        fields.add(sb.toString().trim());
        return fields;
    }

    /**
     * Joins field values into a single CSV line with proper escaping.
     */
    public static String toCsvLine(String... fields) {
        if (fields == null || fields.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escape(fields[i]));
        }
        return sb.toString();
    }
}
