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

/**
 * Suggestion strip view that displays word suggestions above the keyboard.
 */
public class SuggestionStripView extends LinearLayout {
    private static final int MAX_SUGGESTIONS = 5;
    private static final int SUGGESTION_PADDING_DP = 16;
    private static final int SUGGESTION_TEXT_SIZE_SP = 16;
    
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
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        
        int paddingPx = dpToPx(8);
        setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        // Add divider line at bottom
        setBackgroundResource(android.R.drawable.bottom_bar);
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
        
        // Set text appearance
        suggestionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, SUGGESTION_TEXT_SIZE_SP);
        suggestionView.setTextColor(isPrimary ? Color.parseColor("#1976D2") : Color.parseColor("#424242"));
        suggestionView.setTypeface(null, isPrimary ? Typeface.BOLD : Typeface.NORMAL);
        
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