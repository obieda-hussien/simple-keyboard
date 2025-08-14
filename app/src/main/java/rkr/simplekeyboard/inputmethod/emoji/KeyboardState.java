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
 * Represents the current state of the keyboard UI for managing emoji search functionality.
 * This enum is used to track whether we're in normal alphabet mode, emoji browsing mode,
 * or emoji search mode where the alphabet keyboard is shown within the emoji panel.
 */
public enum KeyboardState {
    /** Normal alphabet keyboard is shown */
    ALPHABET,
    
    /** Emoji grid is shown for browsing emojis by category */
    EMOJI_BROWSE,
    
    /** Alphabet keyboard is shown within emoji panel for search input */
    EMOJI_SEARCH
}