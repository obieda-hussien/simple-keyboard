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

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

/**
 * Utility class for clipboard operations.
 */
public class ClipboardUtils {
    
    private static final int MAX_CLIPBOARD_TEXT_LENGTH = 100;
    
    /**
     * Gets the most recent text from the clipboard.
     * Returns null if clipboard is empty or doesn't contain text.
     */
    public static String getClipboardText(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            return null;
        }
        
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return null;
        }
        
        ClipData.Item item = clipData.getItemAt(0);
        CharSequence text = item.getText();
        
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        
        String clipboardText = text.toString().trim();
        
        // Limit length for suggestion display
        if (clipboardText.length() > MAX_CLIPBOARD_TEXT_LENGTH) {
            clipboardText = clipboardText.substring(0, MAX_CLIPBOARD_TEXT_LENGTH) + "...";
        }
        
        return clipboardText;
    }
    
    /**
     * Checks if the clipboard has useful text content.
     */
    public static boolean hasClipboardText(Context context) {
        String text = getClipboardText(context);
        return !TextUtils.isEmpty(text) && text.length() > 1;
    }
    
    /**
     * Creates a formatted clipboard suggestion.
     */
    public static String createClipboardSuggestion(String clipboardText) {
        if (TextUtils.isEmpty(clipboardText)) {
            return null;
        }
        
        // Format for suggestion display
        return "📋 " + clipboardText;
    }
}