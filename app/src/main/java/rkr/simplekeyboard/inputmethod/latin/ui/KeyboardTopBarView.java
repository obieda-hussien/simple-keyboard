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
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeManager;

/**
 * Top bar component for accessing keyboard features like emoji and clipboard.
 * Fixed height toolbar that follows the Gboard model with dynamic theming.
 */
public class KeyboardTopBarView extends LinearLayout {
    
    private ImageButton emojiButton;
    private ImageButton clipboardButton;
    private ImageButton settingsButton;
    private ImageButton toggleButton;
    
    private ThemeManager themeManager;
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
        themeManager = ThemeManager.getInstance(getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.keyboard_top_bar, this, true);
        
        emojiButton = findViewById(R.id.top_bar_emoji_button);
        clipboardButton = findViewById(R.id.top_bar_clipboard_button);
        settingsButton = findViewById(R.id.top_bar_settings_button);
        toggleButton = findViewById(R.id.top_bar_toggle_button);
        
        applyTheme();
        setupButtons();
    }
    
    /**
     * Apply dynamic theme colors to all components.
     */
    private void applyTheme() {
        int topBarBgColor = themeManager.getTopBarBackgroundColor();
        int iconTintColor = themeManager.getIconTintColor();
        
        // Apply background color
        setBackgroundColor(topBarBgColor);
        
        // Apply icon tints
        emojiButton.setColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN);
        clipboardButton.setColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN);
        settingsButton.setColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN);
        toggleButton.setColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN);
    }
    
    /**
     * Refresh theme when preferences change.
     */
    public void refreshTheme() {
        themeManager.refreshTheme();
        applyTheme();
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
            emojiButton.setImageResource(R.drawable.ic_abc); // ABC icon when in emoji mode
            emojiButton.setAlpha(1.0f);
            emojiButton.setContentDescription("Switch to text keyboard");
        } else {
            emojiButton.setImageResource(R.drawable.ic_emoji); // Emoji icon when in text mode  
            emojiButton.setAlpha(0.8f);
            emojiButton.setContentDescription("Switch to emoji keyboard");
        }
        applyTheme(); // Reapply tint after icon change
    }
    
    /**
     * Updates the toggle button appearance based on current view state.
     */
    public void setShowingSuggestions(boolean showingSuggestions) {
        if (showingSuggestions) {
            toggleButton.setImageResource(R.drawable.ic_expand_less); // Up arrow when showing suggestions
            toggleButton.setAlpha(1.0f);
            toggleButton.setContentDescription("Show toolbar");
        } else {
            toggleButton.setImageResource(R.drawable.ic_expand_more); // Down arrow when showing toolbar
            toggleButton.setAlpha(0.8f);
            toggleButton.setContentDescription("Show suggestions");
        }
        applyTheme(); // Reapply tint after icon change
    }
}