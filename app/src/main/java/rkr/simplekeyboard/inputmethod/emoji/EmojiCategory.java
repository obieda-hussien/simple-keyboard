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
 * Emoji categories for organization.
 */
public enum EmojiCategory {
    SMILEYS("😊", "Smileys & Emotions"),
    PEOPLE("👨", "People & Body"),
    ANIMALS("🐱", "Animals & Nature"),
    FOOD("🍕", "Food & Drink"),
    ACTIVITIES("⚽", "Activities"),
    TRAVEL("🚗", "Travel & Places"),
    OBJECTS("💡", "Objects"),
    SYMBOLS("❤️", "Symbols"),
    FLAGS("🏁", "Flags");
    
    private final String icon;
    private final String displayName;
    
    EmojiCategory(String icon, String displayName) {
        this.icon = icon;
        this.displayName = displayName;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}