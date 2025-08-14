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

package rkr.simplekeyboard.inputmethod.latin.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import rkr.simplekeyboard.inputmethod.R;

/**
 * Top bar component for accessing keyboard features like emoji and clipboard.
 * Fixed height toolbar that follows the Gboard model.
 */
public class KeyboardTopBarView extends LinearLayout {
    
    private Button emojiButton;
    private Button clipboardButton;
    private Button settingsButton;
    private Button toggleButton;
    
    private OnTopBarActionListener actionListener;
    
    public interface OnTopBarActionListener {
        void onEmojiButtonClicked();
        void onClipboardButtonClicked();
        void onSettingsButtonClicked();
        void onToggleButtonClicked(); // New toggle functionality
    }
    
    public KeyboardTopBarView(Context context) {
        super(context);
        init();
    }
    
    public KeyboardTopBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public KeyboardTopBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.keyboard_top_bar, this, true);
        
        emojiButton = findViewById(R.id.top_bar_emoji_button);
        clipboardButton = findViewById(R.id.top_bar_clipboard_button);
        settingsButton = findViewById(R.id.top_bar_settings_button);
        toggleButton = findViewById(R.id.top_bar_toggle_button);
        
        setupButtons();
    }
    
    private void setupButtons() {
        emojiButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onEmojiButtonClicked();
            }
        });
        
        clipboardButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onClipboardButtonClicked();
            }
        });
        
        settingsButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onSettingsButtonClicked();
            }
        });
        
        toggleButton.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onToggleButtonClicked();
            }
        });
    }
    
    public void setOnTopBarActionListener(OnTopBarActionListener listener) {
        this.actionListener = listener;
    }
    
    /**
     * Updates the emoji button appearance based on keyboard state.
     */
    public void setEmojiMode(boolean isEmojiMode) {
        if (isEmojiMode) {
            emojiButton.setText("🔤"); // ABC button when in emoji mode
            emojiButton.setAlpha(1.0f);
        } else {
            emojiButton.setText("😊"); // Emoji button when in text mode
            emojiButton.setAlpha(0.7f);
        }
    }
    
    /**
     * Updates the toggle button appearance based on current view state.
     */
    public void setShowingSuggestions(boolean showingSuggestions) {
        if (showingSuggestions) {
            toggleButton.setText("⬆"); // Up arrow when showing suggestions
            toggleButton.setAlpha(1.0f);
        } else {
            toggleButton.setText("⬇"); // Down arrow when showing toolbar
            toggleButton.setAlpha(0.7f);
        }
    }
}