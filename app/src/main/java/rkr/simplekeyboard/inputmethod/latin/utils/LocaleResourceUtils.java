/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Locale;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.common.LocaleUtils;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;

/**
 * A helper class to deal with displaying locales.
  */
public final class LocaleResourceUtils {
    // This reference class {@link R} must be located in the same package as LatinIME.java.
    private static final String RESOURCE_PACKAGE_NAME = R.class.getPackage().getName();

    private static volatile boolean sInitialized = false;
    private static final Object sInitializeLock = new Object();
    private static Resources sResources;
    private static Context sContext;
    private static volatile Locale sDisplayLocaleOverride;
    // Exceptional locale whose name should be displayed in Locale.ROOT.
    private static final HashMap<String, Integer> sExceptionalLocaleDisplayedInRootLocale =
            new HashMap<>();
    // Exceptional locale to locale name resource id map.
    private static final HashMap<String, Integer> sExceptionalLocaleToNameIdsMap = new HashMap<>();
    private static final String LOCALE_NAME_RESOURCE_PREFIX =
            "string/locale_name_";
    private static final String LOCALE_NAME_RESOURCE_IN_ROOT_LOCALE_PREFIX =
            "string/locale_name_in_root_locale_";

    private LocaleResourceUtils() {
        // Intentional empty constructor for utility class.
    }

    // Note that this initialization method can be called multiple times.
    public static void init(final Context context) {
        synchronized (sInitializeLock) {
            if (sInitialized == false) {
                initLocked(context);
                sInitialized = true;
            }
        }
    }

    private static void initLocked(final Context context) {
        final Resources res = context.getResources();
        sContext = context.getApplicationContext();
        sResources = res;

        final String[] exceptionalLocaleInRootLocale = res.getStringArray(
                R.array.locale_displayed_in_root_locale);
        for (int i = 0; i < exceptionalLocaleInRootLocale.length; i++) {
            final String localeString = exceptionalLocaleInRootLocale[i];
            final String resourceName = LOCALE_NAME_RESOURCE_IN_ROOT_LOCALE_PREFIX + localeString;
            final int resId = res.getIdentifier(resourceName, null, RESOURCE_PACKAGE_NAME);
            sExceptionalLocaleDisplayedInRootLocale.put(localeString, resId);
        }

        final String[] exceptionalLocales = res.getStringArray(R.array.locale_exception_keys);
        for (int i = 0; i < exceptionalLocales.length; i++) {
            final String localeString = exceptionalLocales[i];
            final String resourceName = LOCALE_NAME_RESOURCE_PREFIX + localeString;
            final int resId = res.getIdentifier(resourceName, null, RESOURCE_PACKAGE_NAME);
            sExceptionalLocaleToNameIdsMap.put(localeString, resId);
        }
    }

    /**
     * Override the locale used by keyboard-owned language labels. The keyboard's selected
     * language is intentionally allowed to differ from the device locale.
     */
    public static void setDisplayLocale(final Locale locale) {
        sDisplayLocaleOverride = locale;
    }

    private static Locale getUiDisplayLocale() {
        final Locale override = sDisplayLocaleOverride;
        return override != null ? override : sResources.getConfiguration().locale;
    }

    private static Resources getResourcesForLocale(final Locale locale) {
        if (sContext == null || locale == null) return sResources;
        final Configuration configuration = new Configuration(sResources.getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return sContext.createConfigurationContext(configuration).getResources();
    }

    private static Locale getDisplayLocale(final String localeString) {
        if (sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            return Locale.ROOT;
        }
        return LocaleUtils.constructLocaleFromString(localeString);
    }

    /**
     * Get the full display name of the locale in the system's locale.
     * For example in an English system, en_US: "English (US)", fr_CA: "French (Canada)"
     * @param localeString the locale to display.
     * @return the full display name of the locale.
     */
    public static String getLocaleDisplayNameInSystemLocale(
            final String localeString) {
        final Locale displayLocale = getUiDisplayLocale();
        return getLocaleDisplayNameInternal(localeString, displayLocale);
    }

    /**
     * Get the full display name of the locale in its locale.
     * For example, en_US: "English (US)", fr_CA: "Français (Canada)"
     * @param localeString the locale to display.
     * @return the full display name of the locale.
     */
    public static String getLocaleDisplayNameInLocale(final String localeString) {
        final Locale displayLocale = getDisplayLocale(localeString);
        return getLocaleDisplayNameInternal(localeString, displayLocale);
    }

    /**
     * Get the display name of the language in the system's locale.
     * For example in an English system, en_US: "English", fr_CA: "French"
     * @param localeString the locale to display.
     * @return the display name of the language.
     */
    public static String getLanguageDisplayNameInSystemLocale(
            final String localeString) {
        final Locale displayLocale = getUiDisplayLocale();
        final String languageString;
        if (sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            languageString = localeString;
        } else {
            languageString = LocaleUtils.constructLocaleFromString(localeString).getLanguage();
        }
        return getLocaleDisplayNameInternal(languageString, displayLocale);
    }

    /**
     * Get the display name of the language in its locale.
     * For example, en_US: "English", fr_CA: "Français"
     * @param localeString the locale to display.
     * @return the display name of the language.
     */
    public static String getLanguageDisplayNameInLocale(final String localeString) {
        final Locale displayLocale = getDisplayLocale(localeString);
        final String languageString;
        if (sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            languageString = localeString;
        } else {
            languageString = LocaleUtils.constructLocaleFromString(localeString).getLanguage();
        }
        return getLocaleDisplayNameInternal(languageString, displayLocale);
    }

    private static String getLocaleDisplayNameInternal(final String localeString,
                                                       final Locale displayLocale) {
        final Integer exceptionalNameResId;
        if (displayLocale.equals(Locale.ROOT)
                && sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            exceptionalNameResId = sExceptionalLocaleDisplayedInRootLocale.get(localeString);
        } else if (sExceptionalLocaleToNameIdsMap.containsKey(localeString)) {
            exceptionalNameResId = sExceptionalLocaleToNameIdsMap.get(localeString);
        } else {
            exceptionalNameResId = null;
        }

        final String displayName;
        if (exceptionalNameResId != null) {
            displayName = getResourcesForLocale(displayLocale).getString(exceptionalNameResId);
        } else {
            displayName = LocaleUtils.constructLocaleFromString(localeString)
                    .getDisplayName(displayLocale);
        }
        return StringUtils.capitalizeFirstCodePoint(displayName, displayLocale);
    }
}
