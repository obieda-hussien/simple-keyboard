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
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeManager;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette;

/**
 * Top bar component for accessing keyboard features like emoji and clipboard.
 * Fixed height toolbar that follows the Gboard model with dynamic theming.
 */
public class KeyboardTopBarView extends LinearLayout {
    
    private ImageButton emojiButton;
    private ImageButton clipboardButton;
    private ImageButton imageButton;
    private ImageButton voiceButton;
    private ImageButton toolsButton;
    private ImageButton toggleButton;
    
    private ThemeManager themeManager;
    private OnTopBarActionListener actionListener;
    
    public interface OnTopBarActionListener {
        void onEmojiButtonClicked();
        void onClipboardButtonClicked();
        void onClipboardHistoryRequested();
        void onImageButtonClicked();
        void onVoiceButtonClicked();
        void onToolsButtonClicked();
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
        imageButton = findViewById(R.id.top_bar_image_button);
        voiceButton = findViewById(R.id.top_bar_voice_button);
        toolsButton = findViewById(R.id.top_bar_settings_button);
        toggleButton = findViewById(R.id.top_bar_toggle_button);
        
        applyTheme();
        setupButtons();
    }
    
    /**
     * Apply dynamic theme colors to all components.
     */
    private void applyTheme() {
        final int topBarBgColor = themeManager.getTopBarBackgroundColor();
        final int iconTintColor = themeManager.getIconTintColor();
        setBackgroundColor(topBarBgColor);

        final ImageButton[] buttons = {
                emojiButton, clipboardButton, imageButton, voiceButton, toolsButton, toggleButton
        };
        for (ImageButton button : buttons) {
            if (button == null) continue;
            button.setColorFilter(iconTintColor, PorterDuff.Mode.SRC_IN);
        }

        final ImageButton[] surfaced = { voiceButton, toolsButton, toggleButton };
        if (ThemeEngine.isEnabled(getContext())) {
            final ThemePalette palette = ThemeEngine.palette(getContext());
            for (ImageButton button : surfaced) {
                if (button != null) {
                    button.setBackground(ImeUiKit.roundedBackground(
                            getContext(), palette.getFunctionalSurface(), 22.0f,
                            palette.getBorder(), 0.7f));
                }
            }
        } else {
            for (ImageButton button : surfaced) {
                if (button != null) button.setBackgroundResource(R.drawable.button_selector);
            }
        }
    }
    
    /**
     * Refresh theme when preferences change.
     */
    public void refreshTheme() {
        themeManager.refreshTheme();
        applyTheme();
    }
    
    private void setupButtons() {
        final ImageButton[] buttons = {
                emojiButton, clipboardButton, imageButton, voiceButton, toolsButton, toggleButton
        };
        for (ImageButton button : buttons) {
            if (button != null) ImeUiKit.applyPressMotion(button);
        }

        emojiButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEmojiButtonClicked();
        });

        clipboardButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onClipboardButtonClicked();
        });
        clipboardButton.setOnLongClickListener(v -> {
            if (actionListener == null) return false;
            ImeUiKit.haptic(v);
            actionListener.onClipboardHistoryRequested();
            return true;
        });

        imageButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onImageButtonClicked();
        });

        voiceButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onVoiceButtonClicked();
        });

        toolsButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onToolsButtonClicked();
        });

        toggleButton.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onToggleButtonClicked();
        });
    }
    
    public void setOnTopBarActionListener(OnTopBarActionListener listener) {
        this.actionListener = listener;
    }

    public void setMediaActionsEnabled(boolean imageEnabled, boolean voiceEnabled) {
        imageButton.setEnabled(imageEnabled);
        imageButton.setAlpha(imageEnabled ? 0.85f : 0.35f);
        voiceButton.setEnabled(voiceEnabled);
        voiceButton.setAlpha(voiceEnabled ? 0.85f : 0.35f);
    }
    
    public void setPanelOpen(boolean panelOpen) {
        if (toolsButton == null) return;
        toolsButton.setImageResource(panelOpen ? R.drawable.ic_clear : R.drawable.ic_tools_grid);
        toolsButton.setContentDescription(getContext().getString(
                panelOpen ? R.string.close_panel : R.string.show_keyboard_tools));
        toolsButton.setAlpha(panelOpen ? 1.0f : 0.88f);
        applyTheme();
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
