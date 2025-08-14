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

package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.ColorInt;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;
import rkr.simplekeyboard.inputmethod.keyboard.KeyboardTheme;

/**
 * Central theme manager that provides dynamic color theming throughout the keyboard.
 * All colors are loaded at runtime from user preferences, enabling fully customizable theming.
 */
public final class ThemeManager {
    private static final String TAG = ThemeManager.class.getSimpleName();
    
    // Color preference keys
    private static final String PREF_KEY_BACKGROUND_COLOR = "pref_key_background_color";
    private static final String PREF_KEY_TEXT_COLOR = "pref_key_text_color";
    private static final String PREF_KEY_ACCENT_COLOR = "pref_key_accent_color";
    private static final String PREF_KEY_FUNCTIONAL_TEXT_COLOR = "pref_key_functional_text_color";
    private static final String PREF_KEY_PRESSED_COLOR = "pref_key_pressed_color";
    private static final String PREF_TOP_BAR_BACKGROUND_COLOR = "pref_top_bar_background_color";
    private static final String PREF_SUGGESTION_TEXT_COLOR = "pref_suggestion_text_color";
    private static final String PREF_ICON_TINT_COLOR = "pref_icon_tint_color";
    
    // Singleton instance
    private static ThemeManager sInstance;
    private Context mContext;
    private SharedPreferences mPrefs;
    
    // Cached color values
    private int mBackgroundColor = Color.TRANSPARENT;
    private int mKeyTextColor = Color.BLACK;
    private int mAccentColor = Color.BLUE;
    private int mFunctionalTextColor = Color.GRAY;
    private int mKeyPressedColor = Color.LTGRAY;
    private int mTopBarBackgroundColor = Color.WHITE;
    private int mSuggestionTextColor = Color.BLACK;
    private int mIconTintColor = Color.GRAY;
    
    private ThemeManager(Context context) {
        mContext = context.getApplicationContext();
        mPrefs = PreferenceManagerCompat.getDeviceSharedPreferences(mContext);
        loadThemeColors();
    }
    
    public static synchronized ThemeManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new ThemeManager(context);
        }
        return sInstance;
    }
    
    /**
     * Load theme colors from preferences or apply defaults based on current keyboard theme.
     */
    private void loadThemeColors() {
        // Get the current keyboard theme to determine defaults
        KeyboardTheme currentTheme = KeyboardTheme.getKeyboardTheme(mContext);
        boolean isDarkTheme = isDarkTheme(currentTheme);
        
        // Load colors from preferences with theme-appropriate defaults
        mBackgroundColor = mPrefs.getInt(PREF_KEY_BACKGROUND_COLOR, 
            getDefaultBackgroundColor(isDarkTheme));
        mKeyTextColor = mPrefs.getInt(PREF_KEY_TEXT_COLOR, 
            getDefaultKeyTextColor(isDarkTheme));
        mAccentColor = mPrefs.getInt(PREF_KEY_ACCENT_COLOR, 
            getDefaultAccentColor(isDarkTheme));
        mFunctionalTextColor = mPrefs.getInt(PREF_KEY_FUNCTIONAL_TEXT_COLOR, 
            getDefaultFunctionalTextColor(isDarkTheme));
        mKeyPressedColor = mPrefs.getInt(PREF_KEY_PRESSED_COLOR, 
            getDefaultKeyPressedColor(isDarkTheme));
        mTopBarBackgroundColor = mPrefs.getInt(PREF_TOP_BAR_BACKGROUND_COLOR, 
            getDefaultTopBarBackgroundColor(isDarkTheme));
        mSuggestionTextColor = mPrefs.getInt(PREF_SUGGESTION_TEXT_COLOR, 
            getDefaultSuggestionTextColor(isDarkTheme));
        mIconTintColor = mPrefs.getInt(PREF_ICON_TINT_COLOR, 
            getDefaultIconTintColor(isDarkTheme));
            
        Log.d(TAG, "Theme colors loaded - isDark: " + isDarkTheme);
    }
    
    /**
     * Refresh theme colors when preferences change.
     */
    public void refreshTheme() {
        loadThemeColors();
    }
    
    /**
     * Determine if the current theme is dark-based.
     */
    private boolean isDarkTheme(KeyboardTheme theme) {
        int themeId = theme.mThemeId;
        return themeId == KeyboardTheme.THEME_ID_DARK || 
               themeId == KeyboardTheme.THEME_ID_DARK_BORDER ||
               (themeId == KeyboardTheme.THEME_ID_SYSTEM && isSystemDarkTheme()) ||
               (themeId == KeyboardTheme.THEME_ID_SYSTEM_BORDER && isSystemDarkTheme());
    }
    
    /**
     * Check if system is in dark mode.
     */
    private boolean isSystemDarkTheme() {
        int nightModeFlags = mContext.getResources().getConfiguration().uiMode 
            & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
    }
    
    // Default color getters based on theme
    private int getDefaultBackgroundColor(boolean isDark) {
        return isDark ? Color.parseColor("#263238") : Color.parseColor("#ECEFF1");
    }
    
    private int getDefaultKeyTextColor(boolean isDark) {
        return isDark ? Color.parseColor("#CCFFFFFF") : Color.parseColor("#37474F");
    }
    
    private int getDefaultAccentColor(boolean isDark) {
        return isDark ? Color.parseColor("#4DB6AC") : Color.parseColor("#2196F3");
    }
    
    private int getDefaultFunctionalTextColor(boolean isDark) {
        return isDark ? Color.parseColor("#CCFFFFFF") : Color.parseColor("#CC37474F");
    }
    
    private int getDefaultKeyPressedColor(boolean isDark) {
        return isDark ? Color.parseColor("#19FFFFFF") : Color.parseColor("#2637474F");
    }
    
    private int getDefaultTopBarBackgroundColor(boolean isDark) {
        return isDark ? Color.parseColor("#37474F") : Color.parseColor("#FAFAFA");
    }
    
    private int getDefaultSuggestionTextColor(boolean isDark) {
        return isDark ? Color.parseColor("#FFFFFF") : Color.parseColor("#37474F");
    }
    
    private int getDefaultIconTintColor(boolean isDark) {
        return isDark ? Color.parseColor("#CCFFFFFF") : Color.parseColor("#757575");
    }
    
    // Public color getters
    @ColorInt
    public int getBackgroundColor() {
        return mBackgroundColor;
    }
    
    @ColorInt
    public int getKeyTextColor() {
        return mKeyTextColor;
    }
    
    @ColorInt
    public int getAccentColor() {
        return mAccentColor;
    }
    
    @ColorInt
    public int getFunctionalTextColor() {
        return mFunctionalTextColor;
    }
    
    @ColorInt
    public int getKeyPressedColor() {
        return mKeyPressedColor;
    }
    
    @ColorInt
    public int getTopBarBackgroundColor() {
        return mTopBarBackgroundColor;
    }
    
    @ColorInt
    public int getSuggestionTextColor() {
        return mSuggestionTextColor;
    }
    
    @ColorInt
    public int getIconTintColor() {
        return mIconTintColor;
    }
    
    // Color setters for preferences
    public void setBackgroundColor(@ColorInt int color) {
        mBackgroundColor = color;
        mPrefs.edit().putInt(PREF_KEY_BACKGROUND_COLOR, color).apply();
    }
    
    public void setKeyTextColor(@ColorInt int color) {
        mKeyTextColor = color;
        mPrefs.edit().putInt(PREF_KEY_TEXT_COLOR, color).apply();
    }
    
    public void setAccentColor(@ColorInt int color) {
        mAccentColor = color;
        mPrefs.edit().putInt(PREF_KEY_ACCENT_COLOR, color).apply();
    }
    
    public void setFunctionalTextColor(@ColorInt int color) {
        mFunctionalTextColor = color;
        mPrefs.edit().putInt(PREF_KEY_FUNCTIONAL_TEXT_COLOR, color).apply();
    }
    
    public void setKeyPressedColor(@ColorInt int color) {
        mKeyPressedColor = color;
        mPrefs.edit().putInt(PREF_KEY_PRESSED_COLOR, color).apply();
    }
    
    public void setTopBarBackgroundColor(@ColorInt int color) {
        mTopBarBackgroundColor = color;
        mPrefs.edit().putInt(PREF_TOP_BAR_BACKGROUND_COLOR, color).apply();
    }
    
    public void setSuggestionTextColor(@ColorInt int color) {
        mSuggestionTextColor = color;
        mPrefs.edit().putInt(PREF_SUGGESTION_TEXT_COLOR, color).apply();
    }
    
    public void setIconTintColor(@ColorInt int color) {
        mIconTintColor = color;
        mPrefs.edit().putInt(PREF_ICON_TINT_COLOR, color).apply();
    }
}