package de.bigbull.marketblocks.util;

import de.bigbull.marketblocks.core.config.Config;

public class NameValidator {
    
    /**
     * Sanitizes a string according to the security configurations (max length and formatting blocks).
     * Used for shop names, marketplace page names, and NPC names.
     * 
     * @param input The raw input string
     * @return The sanitized string
     */
    public static String sanitizeName(String input) {
        if (input == null) {
            return "";
        }
        
        String sanitized = input.trim();
        
        if (Config.BLOCK_FORMATTING_IN_SHOP_NAME.get()) {
            // Strip Minecraft formatting codes using § or &
            sanitized = sanitized.replaceAll("(?i)[§&][0-9A-FK-OR]", "");
        }
        
        int maxLength = Config.MAX_SHOP_NAME_LENGTH.get();
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }
        
        return sanitized;
    }
}
