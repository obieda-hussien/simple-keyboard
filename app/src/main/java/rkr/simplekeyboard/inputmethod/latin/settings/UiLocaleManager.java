package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;

import java.util.Locale;

import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;

/**
 * Keeps settings/options language aligned with the keyboard language the user is currently using.
 * This is intentionally separate from Android's global app locale: switching Arabic/English on the
 * keyboard should immediately switch keyboard-owned UI without changing the rest of the device.
 */
public final class UiLocaleManager {
    private static final String PREF_ACTIVE_UI_LOCALE = "pref_active_keyboard_ui_locale";

    private UiLocaleManager() {}

    public static void remember(Context context, Locale locale) {
        if (context == null || locale == null) return;
        PreferenceManagerCompat.getDeviceSharedPreferences(context)
                .edit()
                .putString(PREF_ACTIVE_UI_LOCALE, locale.toLanguageTag())
                .apply();
    }

    public static Locale getRememberedLocale(Context context) {
        if (context == null) return Locale.getDefault();
        SharedPreferences prefs = PreferenceManagerCompat.getDeviceSharedPreferences(context);
        String tag = prefs.getString(PREF_ACTIVE_UI_LOCALE, "");
        if (TextUtils.isEmpty(tag)) return Locale.getDefault();
        Locale locale = Locale.forLanguageTag(tag);
        return TextUtils.isEmpty(locale.getLanguage()) ? Locale.getDefault() : locale;
    }

    public static Context wrap(Context base) {
        if (base == null) return null;
        Locale locale = getRememberedLocale(base);
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        return base.createConfigurationContext(configuration);
    }

    public static Resources localizedResources(Context context) {
        return wrap(context).getResources();
    }
}
