package rkr.simplekeyboard.inputmethod.latin.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Lightweight theme engine shared by the Compose Theme Studio and the low-latency IME View path.
 *
 * The IME never needs Compose to render keys: Theme Studio writes a compact profile and the
 * keyboard reads a precomputed palette/geometry snapshot. This keeps key drawing allocation-free.
 */
object ThemeEngine {
    const val PREF_ENABLED = "pref_theme_studio_enabled"
    const val PREF_PRESET = "pref_theme_studio_preset"
    const val PREF_HUE = "pref_theme_studio_hue"
    const val PREF_SATURATION = "pref_theme_studio_saturation"
    const val PREF_SURFACE_TONE = "pref_theme_studio_surface_tone"
    const val PREF_CORNER_RADIUS = "pref_theme_studio_corner_radius"
    const val PREF_KEY_INSET = "pref_theme_studio_key_inset"
    const val PREF_FONT_SCALE = "pref_theme_studio_font_scale"
    const val PREF_BORDER_STRENGTH = "pref_theme_studio_border_strength"
    const val PREF_HIGH_CONTRAST = "pref_theme_studio_high_contrast"
    const val PREF_DYNAMIC_COLOR = "pref_theme_studio_dynamic_color"

    const val PRESET_GRAPHITE = "graphite"
    const val PRESET_VIOLET = "violet"
    const val PRESET_OCEAN = "ocean"
    const val PRESET_EMERALD = "emerald"
    const val PRESET_ROSE = "rose"
    const val PRESET_AMOLED = "amoled"
    const val PRESET_LIGHT = "light"

    private const val DEFAULT_HUE = 258f
    private const val DEFAULT_SATURATION = 0.62f
    private const val DEFAULT_SURFACE_TONE = 0.17f
    private const val DEFAULT_RADIUS_DP = 12f
    private const val DEFAULT_INSET_DP = 2.5f
    private const val DEFAULT_FONT_SCALE = 1f
    private const val DEFAULT_BORDER = 0.08f

    @JvmStatic
    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(PREF_ENABLED, true)

    @JvmStatic
    fun read(context: Context): ThemeProfile {
        val p = prefs(context)
        val dynamic = p.getBoolean(PREF_DYNAMIC_COLOR, false)
        val dynamicHue = if (dynamic) systemAccentHue(context) else null
        return ThemeProfile(
            enabled = p.getBoolean(PREF_ENABLED, true),
            preset = p.getString(PREF_PRESET, PRESET_GRAPHITE) ?: PRESET_GRAPHITE,
            hue = dynamicHue ?: p.getFloat(PREF_HUE, DEFAULT_HUE),
            saturation = p.getFloat(PREF_SATURATION, DEFAULT_SATURATION).coerceIn(0f, 1f),
            surfaceTone = p.getFloat(PREF_SURFACE_TONE, DEFAULT_SURFACE_TONE).coerceIn(0.02f, 0.96f),
            cornerRadiusDp = p.getFloat(PREF_CORNER_RADIUS, DEFAULT_RADIUS_DP).coerceIn(2f, 28f),
            keyInsetDp = p.getFloat(PREF_KEY_INSET, DEFAULT_INSET_DP).coerceIn(0f, 7f),
            fontScale = p.getFloat(PREF_FONT_SCALE, DEFAULT_FONT_SCALE).coerceIn(0.82f, 1.22f),
            borderStrength = p.getFloat(PREF_BORDER_STRENGTH, DEFAULT_BORDER).coerceIn(0f, 0.4f),
            highContrast = p.getBoolean(PREF_HIGH_CONTRAST, false),
            dynamicColor = dynamic
        )
    }

    @JvmStatic
    fun palette(context: Context): ThemePalette = palette(read(context), isSystemDark(context))

    @JvmStatic
    fun palette(profile: ThemeProfile, systemDark: Boolean): ThemePalette {
        val dark = profile.preset != PRESET_LIGHT && (profile.surfaceTone < 0.5f || systemDark)
        val backgroundTone = when (profile.preset) {
            PRESET_AMOLED -> 0.0f
            PRESET_LIGHT -> max(0.91f, profile.surfaceTone)
            else -> profile.surfaceTone
        }
        val accent = hsl(profile.hue, profile.saturation, if (dark) 0.67f else 0.48f)
        val background = hsl(profile.hue, min(profile.saturation * 0.12f, 0.08f), backgroundTone)
        val keySurface = shiftLightness(background, if (dark) 0.12f else -0.045f)
        val functionalSurface = blend(keySurface, accent, if (dark) 0.10f else 0.075f)
        val toolbar = shiftLightness(background, if (dark) 0.055f else -0.025f)
        val pressed = blend(keySurface, accent, if (dark) 0.33f else 0.22f)
        val border = blend(keySurface, contrastText(keySurface), profile.borderStrength)
        val onKey = contrastText(keySurface, profile.highContrast)
        val onFunctional = contrastText(functionalSurface, profile.highContrast)
        val onAccent = contrastText(accent, true)
        val secondary = blend(onKey, keySurface, if (profile.highContrast) 0.18f else 0.34f)

        return ThemePalette(
            background = background,
            keySurface = keySurface,
            functionalSurface = functionalSurface,
            actionSurface = accent,
            pressedSurface = pressed,
            toolbarSurface = toolbar,
            border = border,
            onKey = onKey,
            onFunctional = onFunctional,
            onAction = onAccent,
            secondaryText = secondary,
            accent = accent
        )
    }

    @JvmStatic
    fun applyPreset(context: Context, preset: String) {
        val p = prefs(context)
        val editor = p.edit().putBoolean(PREF_ENABLED, true).putString(PREF_PRESET, preset)
        when (preset) {
            PRESET_VIOLET -> editor
                .putFloat(PREF_HUE, 258f).putFloat(PREF_SATURATION, 0.68f)
                .putFloat(PREF_SURFACE_TONE, 0.16f)
            PRESET_OCEAN -> editor
                .putFloat(PREF_HUE, 205f).putFloat(PREF_SATURATION, 0.68f)
                .putFloat(PREF_SURFACE_TONE, 0.15f)
            PRESET_EMERALD -> editor
                .putFloat(PREF_HUE, 154f).putFloat(PREF_SATURATION, 0.55f)
                .putFloat(PREF_SURFACE_TONE, 0.145f)
            PRESET_ROSE -> editor
                .putFloat(PREF_HUE, 338f).putFloat(PREF_SATURATION, 0.62f)
                .putFloat(PREF_SURFACE_TONE, 0.16f)
            PRESET_AMOLED -> editor
                .putFloat(PREF_HUE, 258f).putFloat(PREF_SATURATION, 0.55f)
                .putFloat(PREF_SURFACE_TONE, 0.0f)
            PRESET_LIGHT -> editor
                .putFloat(PREF_HUE, 258f).putFloat(PREF_SATURATION, 0.45f)
                .putFloat(PREF_SURFACE_TONE, 0.94f)
            else -> editor
                .putFloat(PREF_HUE, DEFAULT_HUE).putFloat(PREF_SATURATION, DEFAULT_SATURATION)
                .putFloat(PREF_SURFACE_TONE, DEFAULT_SURFACE_TONE)
        }
        editor.apply()
    }

    @JvmStatic
    fun update(
        context: Context,
        hue: Float? = null,
        saturation: Float? = null,
        surfaceTone: Float? = null,
        cornerRadiusDp: Float? = null,
        keyInsetDp: Float? = null,
        fontScale: Float? = null,
        borderStrength: Float? = null,
        highContrast: Boolean? = null,
        dynamicColor: Boolean? = null
    ) {
        val e = prefs(context).edit().putBoolean(PREF_ENABLED, true)
        hue?.let { e.putFloat(PREF_HUE, it.coerceIn(0f, 360f)) }
        saturation?.let { e.putFloat(PREF_SATURATION, it.coerceIn(0f, 1f)) }
        surfaceTone?.let { e.putFloat(PREF_SURFACE_TONE, it.coerceIn(0f, 1f)) }
        cornerRadiusDp?.let { e.putFloat(PREF_CORNER_RADIUS, it.coerceIn(2f, 28f)) }
        keyInsetDp?.let { e.putFloat(PREF_KEY_INSET, it.coerceIn(0f, 7f)) }
        fontScale?.let { e.putFloat(PREF_FONT_SCALE, it.coerceIn(0.82f, 1.22f)) }
        borderStrength?.let { e.putFloat(PREF_BORDER_STRENGTH, it.coerceIn(0f, 0.4f)) }
        highContrast?.let { e.putBoolean(PREF_HIGH_CONTRAST, it) }
        dynamicColor?.let { e.putBoolean(PREF_DYNAMIC_COLOR, it) }
        e.apply()
    }

    @JvmStatic
    fun reset(context: Context) {
        prefs(context).edit()
            .remove(PREF_PRESET)
            .remove(PREF_HUE)
            .remove(PREF_SATURATION)
            .remove(PREF_SURFACE_TONE)
            .remove(PREF_CORNER_RADIUS)
            .remove(PREF_KEY_INSET)
            .remove(PREF_FONT_SCALE)
            .remove(PREF_BORDER_STRENGTH)
            .remove(PREF_HIGH_CONTRAST)
            .remove(PREF_DYNAMIC_COLOR)
            .putBoolean(PREF_ENABLED, true)
            .apply()
    }

    private fun prefs(context: Context): SharedPreferences =
        rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat
            .getDeviceSharedPreferences(context.applicationContext)

    private fun isSystemDark(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

    private fun systemAccentHue(context: Context): Float? {
        if (android.os.Build.VERSION.SDK_INT < 31) return null
        val id = context.resources.getIdentifier("system_accent1_500", "color", "android")
        if (id == 0) return null
        return try {
            val color = context.getColor(id)
            rgbToHue(color)
        } catch (_: Throwable) {
            null
        }
    }

    private fun rgbToHue(color: Int): Float {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val delta = maxC - minC
        if (delta == 0f) return DEFAULT_HUE
        val raw = when (maxC) {
            r -> 60f * (((g - b) / delta) % 6f)
            g -> 60f * (((b - r) / delta) + 2f)
            else -> 60f * (((r - g) / delta) + 4f)
        }
        return if (raw < 0f) raw + 360f else raw
    }

    private fun hsl(hue: Float, saturation: Float, lightness: Float): Int {
        val h = ((hue % 360f) + 360f) % 360f
        val s = saturation.coerceIn(0f, 1f)
        val l = lightness.coerceIn(0f, 1f)
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
        val m = l - c / 2f
        val (rp, gp, bp) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        return Color.rgb(
            ((rp + m) * 255f).toInt().coerceIn(0, 255),
            ((gp + m) * 255f).toInt().coerceIn(0, 255),
            ((bp + m) * 255f).toInt().coerceIn(0, 255)
        )
    }

    private fun shiftLightness(color: Int, delta: Float): Int {
        fun channel(c: Int): Int = (c + delta * 255f).toInt().coerceIn(0, 255)
        return Color.rgb(channel(Color.red(color)), channel(Color.green(color)), channel(Color.blue(color)))
    }

    private fun blend(a: Int, b: Int, amount: Float): Int {
        val t = amount.coerceIn(0f, 1f)
        return Color.rgb(
            (Color.red(a) + (Color.red(b) - Color.red(a)) * t).toInt(),
            (Color.green(a) + (Color.green(b) - Color.green(a)) * t).toInt(),
            (Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t).toInt()
        )
    }

    private fun contrastText(background: Int, forceHigh: Boolean = false): Int {
        val black = Color.rgb(18, 18, 20)
        val white = Color.rgb(248, 248, 250)
        val blackRatio = contrastRatio(background, black)
        val whiteRatio = contrastRatio(background, white)
        if (forceHigh) return if (whiteRatio >= blackRatio) white else black
        return if (whiteRatio >= 4.5 || whiteRatio >= blackRatio) white else black
    }

    private fun contrastRatio(a: Int, b: Int): Double {
        val l1 = luminance(a)
        val l2 = luminance(b)
        return (max(l1, l2) + 0.05) / (min(l1, l2) + 0.05)
    }

    private fun luminance(color: Int): Double {
        fun linear(channel: Int): Double {
            val c = channel / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * linear(Color.red(color)) +
            0.7152 * linear(Color.green(color)) +
            0.0722 * linear(Color.blue(color))
    }
}

data class ThemeProfile(
    val enabled: Boolean,
    val preset: String,
    val hue: Float,
    val saturation: Float,
    val surfaceTone: Float,
    val cornerRadiusDp: Float,
    val keyInsetDp: Float,
    val fontScale: Float,
    val borderStrength: Float,
    val highContrast: Boolean,
    val dynamicColor: Boolean
)

data class ThemePalette(
    val background: Int,
    val keySurface: Int,
    val functionalSurface: Int,
    val actionSurface: Int,
    val pressedSurface: Int,
    val toolbarSurface: Int,
    val border: Int,
    val onKey: Int,
    val onFunctional: Int,
    val onAction: Int,
    val secondaryText: Int,
    val accent: Int
)
