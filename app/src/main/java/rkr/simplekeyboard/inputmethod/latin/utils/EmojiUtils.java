/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rkr.simplekeyboard.inputmethod.latin.utils;

/**
 * Utility class for detecting and handling emoji characters.
 */
public class EmojiUtils {
    
    /**
     * Checks if a string contains only emoji characters.
     * Uses Unicode blocks commonly used for emojis.
     */
    public static boolean isEmoji(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = text.trim();
        
        // Check if every character in the string is an emoji
        for (int i = 0; i < trimmed.length(); ) {
            int codePoint = trimmed.codePointAt(i);
            if (!isEmojiCodePoint(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        
        return true;
    }
    
    /**
     * Checks if a Unicode code point represents an emoji character.
     */
    private static boolean isEmojiCodePoint(int codePoint) {
        // Common emoji Unicode blocks
        return (codePoint >= 0x1F600 && codePoint <= 0x1F64F) ||  // Emoticons
               (codePoint >= 0x1F300 && codePoint <= 0x1F5FF) ||  // Misc Symbols and Pictographs
               (codePoint >= 0x1F680 && codePoint <= 0x1F6FF) ||  // Transport and Map
               (codePoint >= 0x1F1E6 && codePoint <= 0x1F1FF) ||  // Regional Indicator Symbols
               (codePoint >= 0x2600 && codePoint <= 0x26FF) ||    // Misc symbols
               (codePoint >= 0x2700 && codePoint <= 0x27BF) ||    // Dingbats
               (codePoint >= 0xE0020 && codePoint <= 0xE007F) ||  // Tags
               (codePoint >= 0xFE00 && codePoint <= 0xFE0F) ||    // Variation Selectors
               (codePoint >= 0x1F900 && codePoint <= 0x1F9FF) ||  // Supplemental Symbols and Pictographs
               (codePoint >= 0x1F018 && codePoint <= 0x1F270) ||  // Additional emoticons
               (codePoint == 0x200D) ||                           // Zero Width Joiner
               (codePoint >= 0x23E9 && codePoint <= 0x23F3) ||   // Media symbols
               (codePoint >= 0x23F8 && codePoint <= 0x23FA);     // More media symbols
    }
    
    /**
     * Extracts individual emoji tokens from a text string.
     * Returns an array of emoji strings found in the text.
     */
    public static String[] extractEmojis(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new String[0];
        }
        
        java.util.List<String> emojis = new java.util.ArrayList<>();
        StringBuilder currentEmoji = new StringBuilder();
        
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            
            if (isEmojiCodePoint(codePoint)) {
                currentEmoji.appendCodePoint(codePoint);
            } else {
                if (currentEmoji.length() > 0) {
                    emojis.add(currentEmoji.toString());
                    currentEmoji.setLength(0);
                }
            }
            
            i += Character.charCount(codePoint);
        }
        
        // Add the last emoji if any
        if (currentEmoji.length() > 0) {
            emojis.add(currentEmoji.toString());
        }
        
        return emojis.toArray(new String[0]);
    }
    
    /**
     * Checks if a character sequence contains any emoji characters.
     */
    public static boolean containsEmoji(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            if (isEmojiCodePoint(codePoint)) {
                return true;
            }
            i += Character.charCount(codePoint);
        }
        
        return false;
    }
}