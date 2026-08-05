package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.core.config.Config;

public class NameValidator {
    
    /**
     * Sanitizes a string according to the security configurations (max length and formatting blocks).
     * Used for shop names.
     * 
     * @param input The raw input string
     * @return The sanitized string
     */
    public static String sanitizeName(String input) {
        return sanitizeWithLengthLimit(input, Config.MAX_SHOP_NAME_LENGTH.get());
    }

    /**
     * Sanitizes a marketplace page name (up to 64 characters).
     */
    public static String sanitizePageName(String input) {
        return sanitizeWithLengthLimit(input, 64);
    }

    /**
     * Sanitizes an NPC name (up to 32 characters).
     */
    public static String sanitizeNpcName(String input) {
        return sanitizeWithLengthLimit(input, 32);
    }

    private static String sanitizeWithLengthLimit(String input, int maxLength) {
        if (input == null) {
            return "";
        }
        
        String sanitized = input.trim();
        
        if (Config.BLOCK_FORMATTING_IN_SHOP_NAME.get()) {
            sanitized = stripColorCodes(sanitized);
        }
        
        sanitized = sanitized.replace("|", "");
        
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized;
    }

    private static String stripColorCodes(String input) {
        return input.replaceAll("(?i)[§&]#[0-9A-F]{6}", "")
                    .replaceAll("(?i)[§&]x([§&][0-9A-F]){6}", "")
                    .replaceAll("(?i)[§&][0-9A-FK-OR]", "");
    }
}
