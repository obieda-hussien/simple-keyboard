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
import android.graphics.Typeface;
import android.view.HapticFeedbackConstants;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeManager;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette;

/**
 * Suggestion strip view that displays word suggestions above the keyboard.
 * Uses dynamic theming for consistent appearance.
 */
public class SuggestionStripView extends LinearLayout {
    private static final int MAX_SUGGESTIONS = 5;
    private static final int SUGGESTION_PADDING_DP = 16;
    private static final int SUGGESTION_TEXT_SIZE_SP = 16;
    
    private ThemeManager themeManager;
    private OnSuggestionClickListener suggestionClickListener;
    private OnSuggestionLongClickListener suggestionLongClickListener;
    private Locale languageLocale;
    private OnToggleClickListener toggleClickListener;
    private android.widget.ImageButton toggleButton;
    private final TextView[] suggestionViews = new TextView[MAX_SUGGESTIONS];
    private int visibleSuggestions;
    
    public interface OnSuggestionClickListener {
        void onSuggestionClicked(String suggestion);
    }

    public interface OnSuggestionLongClickListener {
        void onSuggestionLongClicked(String suggestion);
    }
    
    public interface OnToggleClickListener {
        void onToggleClicked();
    }
    
    public SuggestionStripView(Context context) {
        this(context, null);
    }
    
    public SuggestionStripView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public SuggestionStripView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        themeManager = ThemeManager.getInstance(getContext());
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        
        int paddingPx = dpToPx(8);
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        
        // Create toggle button for Gboard-style switching
        createToggleButton();
        
        applyTheme();
    }
    
    /**
     * Creates the toggle button for switching back to toolbar view.
     */
    private void createToggleButton() {
        toggleButton = new android.widget.ImageButton(getContext());
        toggleButton.setImageResource(android.R.drawable.ic_menu_more);
        toggleButton.setContentDescription(getContext().getString(
                rkr.simplekeyboard.inputmethod.R.string.show_keyboard_tools)); // Use system chevron right icon
        toggleButton.setBackground(null); // Remove default button background
        toggleButton.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        
        // Set size and padding
        int buttonSize = dpToPx(40);
        int buttonPadding = dpToPx(8);
        toggleButton.setPadding(buttonPadding, buttonPadding, buttonPadding, buttonPadding);
        
        LayoutParams toggleParams = new LayoutParams(buttonSize, buttonSize);
        toggleParams.gravity = Gravity.CENTER_VERTICAL;
        toggleButton.setLayoutParams(toggleParams);
        
        // Set click listener
        toggleButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (toggleClickListener != null) {
                    toggleClickListener.onToggleClicked();
                }
            }
        });
        
        // Apply ripple effect
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true);
        toggleButton.setBackgroundResource(outValue.resourceId);
        
        addView(toggleButton, 0); // Add as first child (far-left)
    }
    
    /**
     * Apply dynamic theme colors to the suggestion strip.
     */
    private void applyTheme() {
        final int topBarBgColor = themeManager.getTopBarBackgroundColor();
        setBackgroundColor(topBarBgColor);

        if (toggleButton != null) {
            final int iconTintColor = themeManager.getIconTintColor();
            toggleButton.setColorFilter(iconTintColor);
            if (ThemeEngine.isEnabled(getContext())) {
                final ThemePalette palette = ThemeEngine.palette(getContext());
                toggleButton.setBackground(ImeUiKit.roundedBackground(
                        getContext(), palette.getFunctionalSurface(), 20.0f,
                        palette.getBorder(), 0.7f));
            }
        }
    }
    
    /**
     * Refresh theme when preferences change.
     */
    public void refreshTheme() {
        themeManager.refreshTheme();
        applyTheme();
        // Refresh all existing suggestion views
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child instanceof TextView) {
                applySuggestionTheme((TextView) child, i == 1); // Account for toggle button at index 0
            }
        }
    }
    
    /**
     * Updates the suggestion strip with new suggestions.
     */
    public void setSuggestions(List<String> suggestions) {
        int maxVisible = getResources().getConfiguration().smallestScreenWidthDp >= 600
                ? MAX_SUGGESTIONS : 3;
        int count = suggestions == null ? 0 : Math.min(suggestions.size(), maxVisible);
        for (int i = 0; i < MAX_SUGGESTIONS; i++) {
            if (i >= count) {
                if (suggestionViews[i] != null) suggestionViews[i].setVisibility(View.GONE);
                continue;
            }
            if (suggestionViews[i] == null) {
                suggestionViews[i] = addSuggestionView(i == 0);
            }
            TextView view = suggestionViews[i];
            String value = suggestions.get(i);
            if (!value.contentEquals(view.getText())) {
                view.setText(value);
                ImeUiKit.applyTextDirection(view, value, languageLocale);
                view.setTranslationY(dpToPx(4));
                view.setAlpha(0.0f);
                view.animate().translationY(0.0f).alpha(1.0f).setDuration(120L).start();
            }
            applySuggestionTheme(view, i == 0);
            view.setVisibility(View.VISIBLE);
        }
        visibleSuggestions = count;
    }

    private TextView addSuggestionView(boolean isPrimary) {
        TextView suggestionView = new TextView(getContext());
        applySuggestionTheme(suggestionView, isPrimary);
        int paddingPx = dpToPx(SUGGESTION_PADDING_DP);
        suggestionView.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);
        suggestionView.setSingleLine(true);
        suggestionView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        suggestionView.setClickable(true);
        suggestionView.setFocusable(true);
        ImeUiKit.applyPressMotion(suggestionView);
        suggestionView.setOnClickListener(v -> {
            if (suggestionClickListener != null) {
                suggestionClickListener.onSuggestionClicked(suggestionView.getText().toString());
            }
        });
        suggestionView.setOnLongClickListener(v -> {
            if (suggestionLongClickListener == null) return false;
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            suggestionLongClickListener.onSuggestionLongClicked(
                    suggestionView.getText().toString());
            return true;
        });
        LayoutParams params = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        final int margin = dpToPx(3);
        params.setMargins(margin, 0, margin, 0);
        suggestionView.setLayoutParams(params);
        suggestionView.setGravity(Gravity.CENTER);
        addView(suggestionView);
        return suggestionView;
    }

    /**
     * Apply theme styling to a suggestion TextView.
     */
    private void applySuggestionTheme(TextView suggestionView, boolean isPrimary) {
        suggestionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SUGGESTION_TEXT_SIZE_SP);

        if (ThemeEngine.isEnabled(getContext())) {
            final ThemePalette palette = ThemeEngine.palette(getContext());
            final int fill = isPrimary
                    ? palette.getFunctionalSurface() : palette.getKeySurface();
            final int stroke = isPrimary ? palette.getAccent() : palette.getBorder();
            suggestionView.setBackground(ImeUiKit.roundedBackground(
                    getContext(), fill, 18.0f, stroke, isPrimary ? 1.1f : 0.7f));
            suggestionView.setTextColor(isPrimary ? palette.getAccent() : palette.getOnKey());
            suggestionView.setTypeface(null, isPrimary ? Typeface.BOLD : Typeface.NORMAL);
        } else if (isPrimary) {
            suggestionView.setTextColor(themeManager.getAccentColor());
            suggestionView.setTypeface(null, Typeface.BOLD);
        } else {
            suggestionView.setTextColor(themeManager.getSuggestionTextColor());
            suggestionView.setTypeface(null, Typeface.NORMAL);
        }
    }
    
    /**
     * Sets the suggestion click listener.
     */
    public void setOnSuggestionClickListener(OnSuggestionClickListener listener) {
        this.suggestionClickListener = listener;
    }

    public void setOnSuggestionLongClickListener(OnSuggestionLongClickListener listener) {
        this.suggestionLongClickListener = listener;
    }

    public void setLanguageLocale(Locale locale) {
        languageLocale = locale;
        // Keep the tools/toggle slot physically stable; each chip handles its own bidi text.
        setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        for (TextView view : suggestionViews) {
            if (view != null) ImeUiKit.applyTextDirection(view, view.getText(), locale);
        }
    }
    
    /**
     * Sets the toggle click listener.
     */
    public void setOnToggleClickListener(OnToggleClickListener listener) {
        this.toggleClickListener = listener;
    }
    
    /**
     * Clears all suggestions.
     */
    public void clearSuggestions() {
        setSuggestions(null);
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * Returns true if there are suggestions to display.
     */
    public boolean hasSuggestions() {
        return visibleSuggestions > 0;
    }
}