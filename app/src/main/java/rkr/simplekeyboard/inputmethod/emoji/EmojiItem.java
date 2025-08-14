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

package rkr.simplekeyboard.inputmethod.emoji;

/**
 * Represents an emoji item with its unicode character, description, and keywords.
 */
public class EmojiItem {
    private final String emoji;
    private final String description;
    private final String[] keywords;
    private final EmojiCategory category;
    
    public EmojiItem(String emoji, String description, EmojiCategory category, String... keywords) {
        this.emoji = emoji;
        this.description = description;
        this.category = category;
        this.keywords = keywords;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String[] getKeywords() {
        return keywords;
    }
    
    public EmojiCategory getCategory() {
        return category;
    }
    
    /**
     * Checks if this emoji matches the given search query.
     */
    public boolean matches(String query) {
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        
        String lowerQuery = query.toLowerCase().trim();
        
        // Check description
        if (description.toLowerCase().contains(lowerQuery)) {
            return true;
        }
        
        // Check keywords
        for (String keyword : keywords) {
            if (keyword.toLowerCase().contains(lowerQuery)) {
                return true;
            }
        }
        
        return false;
    }
}