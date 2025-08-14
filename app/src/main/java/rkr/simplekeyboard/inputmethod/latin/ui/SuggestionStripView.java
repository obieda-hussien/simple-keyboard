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
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import rkr.simplekeyboard.inputmethod.latin.settings.ThemeManager;

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
    
    public interface OnSuggestionClickListener {
        void onSuggestionClicked(String suggestion);
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
        
        applyTheme();
    }
    
    /**
     * Apply dynamic theme colors to the suggestion strip.
     */
    private void applyTheme() {
        int topBarBgColor = themeManager.getTopBarBackgroundColor();
        setBackgroundColor(topBarBgColor);
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
                applySuggestionTheme((TextView) child, i == 0);
            }
        }
    }
    
    /**
     * Updates the suggestion strip with new suggestions.
     */
    public void setSuggestions(List<String> suggestions) {
        removeAllViews();
        
        if (suggestions == null || suggestions.isEmpty()) {
            // Don't change visibility - stay within the fixed container
            return;
        }
        
        int suggestionCount = Math.min(suggestions.size(), MAX_SUGGESTIONS);
        for (int i = 0; i < suggestionCount; i++) {
            String suggestion = suggestions.get(i);
            addSuggestionView(suggestion, i == 0); // First suggestion is primary
        }
    }
    
    private void addSuggestionView(String suggestion, boolean isPrimary) {
        TextView suggestionView = new TextView(getContext());
        suggestionView.setText(suggestion);
        
        // Apply theme styling
        applySuggestionTheme(suggestionView, isPrimary);
        
        // Set padding
        int paddingPx = dpToPx(SUGGESTION_PADDING_DP);
        suggestionView.setPadding(paddingPx, paddingPx / 2, paddingPx, paddingPx / 2);
        
        // Set click behavior
        suggestionView.setClickable(true);
        suggestionView.setFocusable(true);
        
        // Add ripple effect for modern Android versions
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        suggestionView.setBackgroundResource(outValue.resourceId);
        
        suggestionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (suggestionClickListener != null) {
                    suggestionClickListener.onSuggestionClicked(suggestion);
                }
            }
        });
        
        // Set layout parameters
        LayoutParams layoutParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f);
        suggestionView.setLayoutParams(layoutParams);
        suggestionView.setGravity(Gravity.CENTER);
        
        addView(suggestionView);
    }
    
    /**
     * Apply theme styling to a suggestion TextView.
     */
    private void applySuggestionTheme(TextView suggestionView, boolean isPrimary) {
        suggestionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SUGGESTION_TEXT_SIZE_SP);
        
        if (isPrimary) {
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
    
    /**
     * Clears all suggestions.
     */
    public void clearSuggestions() {
        removeAllViews();
        // Don't change visibility - stay within the fixed container
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * Returns true if there are suggestions to display.
     */
    public boolean hasSuggestions() {
        return getChildCount() > 0;
    }
}