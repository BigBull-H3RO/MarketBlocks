package de.bigbull.marketblocks.util;

import java.util.regex.Pattern;

public class NameValidator {

    public static final int MAX_SHOP_NAME_LENGTH = 32;
    public static final int MAX_PAGE_NAME_LENGTH = 64;
    public static final int MAX_NPC_NAME_LENGTH = 32;

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)[§&]#[0-9A-F]{6}");
    private static final Pattern EXTENDED_HEX_PATTERN = Pattern.compile("(?i)[§&]x([§&][0-9A-F]){6}");
    private static final Pattern FORMATTING_PATTERN = Pattern.compile("(?i)[§&][0-9A-FK-OR]");

    /**
     * Sanitizes a string according to length limits and formatting blocks.
     * Used for shop names.
     * 
     * @param input The raw input string
     * @return The sanitized string
     */
    public static String sanitizeName(String input) {
        return sanitizeWithLengthLimit(input, MAX_SHOP_NAME_LENGTH);
    }

    /**
     * Sanitizes a marketplace page name (up to 64 characters).
     */
    public static String sanitizePageName(String input) {
        return sanitizeWithLengthLimit(input, MAX_PAGE_NAME_LENGTH);
    }

    /**
     * Sanitizes an NPC name (up to 32 characters).
     */
    public static String sanitizeNpcName(String input) {
        return sanitizeWithLengthLimit(input, MAX_NPC_NAME_LENGTH);
    }

    private static String sanitizeWithLengthLimit(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        
        String sanitized = input.trim();
        sanitized = stripColorCodes(sanitized);
        sanitized = sanitized.replace("|", "");
        
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized;
    }

    private static String stripColorCodes(String input) {
        String result = HEX_COLOR_PATTERN.matcher(input).replaceAll("");
        result = EXTENDED_HEX_PATTERN.matcher(result).replaceAll("");
        return FORMATTING_PATTERN.matcher(result).replaceAll("");
    }
}
