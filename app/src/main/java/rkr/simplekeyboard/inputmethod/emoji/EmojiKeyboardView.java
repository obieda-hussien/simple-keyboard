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

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeManager;

/**
 * View for displaying emoji keyboard with categories and search functionality.
 * Uses dynamic theming for consistent appearance with the main keyboard.
 */
public class EmojiKeyboardView extends LinearLayout {
    
    private RecyclerView emojiRecyclerView;
    private EditText searchEditText;
    private ImageButton searchClearButton;
    private LinearLayout categoryTabsLayout;
    
    private ThemeManager themeManager;
    private EmojiAdapter emojiAdapter;
    private List<EmojiItem> currentEmojis;
    private EmojiCategory selectedCategory = EmojiCategory.SMILEYS;
    
    private OnEmojiClickListener emojiClickListener;
    private OnBackClickListener backClickListener;
    
    public interface OnEmojiClickListener {
        void onEmojiClicked(String emoji);
    }
    
    public interface OnBackClickListener {
        void onBackClicked();
    }
    
    public EmojiKeyboardView(Context context) {
        super(context);
        init();
    }
    
    public EmojiKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public EmojiKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        themeManager = ThemeManager.getInstance(getContext());
        LayoutInflater.from(getContext()).inflate(R.layout.emoji_keyboard, this, true);
        
        emojiRecyclerView = findViewById(R.id.emoji_recycler_view);
        searchEditText = findViewById(R.id.emoji_search);
        searchClearButton = findViewById(R.id.emoji_search_clear);
        categoryTabsLayout = findViewById(R.id.emoji_category_tabs);
        
        applyTheme();
        setupRecyclerView();
        setupSearchFunctionality();
        setupCategoryTabs();
        
        // Load initial emojis
        loadEmojisByCategory(selectedCategory);
    }
    
    /**
     * Apply dynamic theme colors to emoji keyboard components.
     */
    private void applyTheme() {
        int backgroundColor = themeManager.getBackgroundColor();
        int iconTintColor = themeManager.getIconTintColor();
        int textColor = themeManager.getKeyTextColor();
        
        // Apply background color
        setBackgroundColor(backgroundColor);
        
        // Apply search field styling
        searchEditText.setTextColor(textColor);
        searchEditText.setHintTextColor(textColor & 0x7FFFFFFF); // Semi-transparent
        
        // Apply clear button tint
        searchClearButton.setColorFilter(iconTintColor);
    }
    
    /**
     * Refresh theme when preferences change.
     */
    public void refreshTheme() {
        themeManager.refreshTheme();
        applyTheme();
        // Refresh category tabs
        setupCategoryTabs();
    }
    
    private void setupRecyclerView() {
        currentEmojis = new ArrayList<>();
        emojiAdapter = new EmojiAdapter(currentEmojis);
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 8);
        emojiRecyclerView.setLayoutManager(gridLayoutManager);
        emojiRecyclerView.setAdapter(emojiAdapter);
    }
    
    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    loadEmojisByCategory(selectedCategory);
                } else {
                    searchEmojis(query);
                }
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        searchClearButton.setOnClickListener(v -> {
            searchEditText.setText("");
            loadEmojisByCategory(selectedCategory);
        });
    }
    
    private void setupCategoryTabs() {
        categoryTabsLayout.removeAllViews(); // Clear existing tabs
        
        for (EmojiCategory category : EmojiCategory.values()) {
            Button categoryButton = new Button(getContext());
            categoryButton.setText(category.getIcon());
            categoryButton.setTextSize(20);
            categoryButton.setMinimumWidth(dpToPx(48));
            categoryButton.setMinimumHeight(dpToPx(48));
            
            // Apply dynamic theming to category tabs
            applyCategoryTabTheme(categoryButton, false);
            
            categoryButton.setOnClickListener(v -> {
                selectedCategory = category;
                updateCategorySelection();
                loadEmojisByCategory(category);
                searchEditText.setText(""); // Clear search
            });
            
            categoryTabsLayout.addView(categoryButton);
        }
        updateCategorySelection();
    }
    
    /**
     * Apply theme styling to category tab buttons.
     */
    private void applyCategoryTabTheme(Button button, boolean isSelected) {
        int backgroundColor = themeManager.getBackgroundColor();
        int textColor = themeManager.getKeyTextColor();
        int accentColor = themeManager.getAccentColor();
        
        button.setTextColor(isSelected ? accentColor : textColor);
        button.setBackground(getContext().getDrawable(R.drawable.tab_selector));
        button.setSelected(isSelected);
        button.setAlpha(isSelected ? 1.0f : 0.7f);
    }
    
    private void updateCategorySelection() {
        for (int i = 0; i < categoryTabsLayout.getChildCount(); i++) {
            Button button = (Button) categoryTabsLayout.getChildAt(i);
            boolean isSelected = (i == selectedCategory.ordinal());
            applyCategoryTabTheme(button, isSelected);
        }
    }
    
    private void loadEmojisByCategory(EmojiCategory category) {
        List<EmojiItem> emojis = EmojiData.getEmojisByCategory(category);
        updateEmojiList(emojis);
    }
    
    private void searchEmojis(String query) {
        List<EmojiItem> emojis = EmojiData.searchEmojis(query);
        updateEmojiList(emojis);
    }
    
    private void updateEmojiList(List<EmojiItem> emojis) {
        currentEmojis.clear();
        currentEmojis.addAll(emojis);
        emojiAdapter.notifyDataSetChanged();
    }
    
    public void setOnEmojiClickListener(OnEmojiClickListener listener) {
        this.emojiClickListener = listener;
    }
    
    public void setOnBackClickListener(OnBackClickListener listener) {
        this.backClickListener = listener;
    }
    
    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
    
    /**
     * Adapter for emoji grid.
     */
    private class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder> {
        
        private final List<EmojiItem> emojis;
        
        public EmojiAdapter(List<EmojiItem> emojis) {
            this.emojis = emojis;
        }
        
        @Override
        public EmojiViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.emoji_item, parent, false);
            return new EmojiViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(EmojiViewHolder holder, int position) {
            EmojiItem emoji = emojis.get(position);
            holder.bind(emoji);
        }
        
        @Override
        public int getItemCount() {
            return emojis.size();
        }
        
        class EmojiViewHolder extends RecyclerView.ViewHolder {
            private final TextView emojiTextView;
            
            public EmojiViewHolder(View itemView) {
                super(itemView);
                emojiTextView = (TextView) itemView;
            }
            
            public void bind(EmojiItem emoji) {
                emojiTextView.setText(emoji.getEmoji());
                emojiTextView.setOnClickListener(v -> {
                    if (emojiClickListener != null) {
                        emojiClickListener.onEmojiClicked(emoji.getEmoji());
                    }
                });
            }
        }
    }
}