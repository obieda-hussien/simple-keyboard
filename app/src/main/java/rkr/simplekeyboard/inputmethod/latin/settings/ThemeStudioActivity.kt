package rkr.simplekeyboard.inputmethod.latin.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import rkr.simplekeyboard.inputmethod.R
import kotlin.math.roundToInt

/**
 * Compose-only editor for appearance. The actual IME remains on the optimized custom View renderer.
 */
class ThemeStudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ThemeStudioScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeStudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(ThemeEngine.read(context)) }

    fun persist(updated: ThemeProfile) {
        profile = updated
        ThemeEngine.update(
            context = context,
            hue = updated.hue,
            saturation = updated.saturation,
            surfaceTone = updated.surfaceTone,
            cornerRadiusDp = updated.cornerRadiusDp,
            keyInsetDp = updated.keyInsetDp,
            fontScale = updated.fontScale,
            borderStrength = updated.borderStrength,
            highContrast = updated.highContrast,
            dynamicColor = updated.dynamicColor
        )
        ThemeManager.getInstance(context).refreshTheme()
    }

    val palette = ThemeEngine.palette(profile, androidx.compose.foundation.isSystemInDarkTheme())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.theme_studio_title)) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.theme_studio_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(palette.toolbarSurface),
                    titleContentColor = Color(palette.onKey),
                    navigationIconContentColor = Color(palette.onKey)
                )
            )
        },
        containerColor = Color(palette.background)
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.theme_studio_preview),
                color = Color(palette.onKey),
                style = MaterialTheme.typography.titleMedium
            )
            KeyboardPreview(profile, palette)

            Text(
                text = stringResource(R.string.theme_studio_presets),
                color = Color(palette.onKey),
                style = MaterialTheme.typography.titleMedium
            )
            PresetRows(profile, palette) { preset ->
                ThemeEngine.applyPreset(context, preset)
                profile = ThemeEngine.read(context)
                ThemeManager.getInstance(context).refreshTheme()
            }

            HorizontalDivider(color = Color(palette.border))

            ThemeSlider(
                label = stringResource(R.string.theme_studio_hue),
                value = profile.hue,
                valueRange = 0f..360f,
                valueText = "${profile.hue.roundToInt()}°",
                palette = palette,
                onChanged = { persist(profile.copy(hue = it, dynamicColor = false)) }
            )
            ThemeSlider(
                label = stringResource(R.string.theme_studio_saturation),
                value = profile.saturation,
                valueRange = 0f..1f,
                valueText = "${(profile.saturation * 100).roundToInt()}%",
                palette = palette,
                onChanged = { persist(profile.copy(saturation = it)) }
            )
            ThemeSlider(
                label = stringResource(R.string.theme_studio_surface_tone),
                value = profile.surfaceTone,
                valueRange = 0.02f..0.96f,
                valueText = "${(profile.surfaceTone * 100).roundToInt()}%",
                palette = palette,
                onChanged = { persist(profile.copy(surfaceTone = it)) }
            )
            ThemeSlider(
                label = stringResource(R.string.theme_studio_corner_radius),
                value = profile.cornerRadiusDp,
                valueRange = 2f..28f,
                valueText = "${profile.cornerRadiusDp.roundToInt()} dp",
                palette = palette,
                onChanged = { persist(profile.copy(cornerRadiusDp = it)) }
            )
            ThemeSlider(
                label = stringResource(R.string.theme_studio_key_spacing),
                value = profile.keyInsetDp,
                valueRange = 0f..7f,
                valueText = "${String.format("%.1f", profile.keyInsetDp)} dp",
                palette = palette,
                onChanged = { persist(profile.copy(keyInsetDp = it)) }
            )
            ThemeSlider(
                label = stringResource(R.string.theme_studio_font_scale),
                value = profile.fontScale,
                valueRange = 0.82f..1.22f,
                valueText = "${(profile.fontScale * 100).roundToInt()}%",
                palette = palette,
                onChanged = { persist(profile.copy(fontScale = it)) }
            )
            ThemeSlider(
                label = stringResource(R.string.theme_studio_border),
                value = profile.borderStrength,
                valueRange = 0f..0.4f,
                valueText = "${(profile.borderStrength * 100).roundToInt()}%",
                palette = palette,
                onChanged = { persist(profile.copy(borderStrength = it)) }
            )

            ToggleRow(
                title = stringResource(R.string.theme_studio_dynamic_color),
                subtitle = stringResource(R.string.theme_studio_dynamic_color_summary),
                checked = profile.dynamicColor,
                palette = palette
            ) {
                ThemeEngine.update(context, dynamicColor = it)
                profile = ThemeEngine.read(context)
                ThemeManager.getInstance(context).refreshTheme()
            }

            ToggleRow(
                title = stringResource(R.string.theme_studio_high_contrast),
                subtitle = stringResource(R.string.theme_studio_high_contrast_summary),
                checked = profile.highContrast,
                palette = palette
            ) { persist(profile.copy(highContrast = it)) }

            Button(
                onClick = {
                    ThemeEngine.reset(context)
                    profile = ThemeEngine.read(context)
                    ThemeManager.getInstance(context).refreshTheme()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.theme_studio_reset))
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PresetRows(
    profile: ThemeProfile,
    palette: ThemePalette,
    onPreset: (String) -> Unit
) {
    val presets = listOf(
        ThemeEngine.PRESET_GRAPHITE to stringResource(R.string.theme_preset_graphite),
        ThemeEngine.PRESET_VIOLET to stringResource(R.string.theme_preset_violet),
        ThemeEngine.PRESET_OCEAN to stringResource(R.string.theme_preset_ocean),
        ThemeEngine.PRESET_EMERALD to stringResource(R.string.theme_preset_emerald),
        ThemeEngine.PRESET_ROSE to stringResource(R.string.theme_preset_rose),
        ThemeEngine.PRESET_AMOLED to stringResource(R.string.theme_preset_amoled),
        ThemeEngine.PRESET_LIGHT to stringResource(R.string.theme_preset_light)
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.chunked(3).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (id, title) ->
                    val selected = profile.preset == id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selected) Color(palette.actionSurface) else Color(palette.keySurface),
                                RoundedCornerShape(18.dp)
                            )
                            .border(
                                1.dp,
                                if (selected) Color(palette.actionSurface) else Color(palette.border),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable { onPreset(id) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            title,
                            color = if (selected) Color(palette.onAction) else Color(palette.onKey),
                            maxLines = 1,
                            fontSize = 12.sp
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun KeyboardPreview(profile: ThemeProfile, palette: ThemePalette) {
    val shape = RoundedCornerShape(profile.cornerRadiusDp.dp)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(palette.background))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("أنا", "عايز", "تمام").forEachIndexed { index, value ->
                    Text(
                        value,
                        color = if (index == 0) Color(palette.accent) else Color(palette.onKey),
                        fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
            listOf(
                listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح", "ج"),
                listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م", "ك", "ة"),
                listOf("ئ", "ء", "ؤ", "ر", "لا", "ى", "أ")
            ).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(profile.keyInsetDp.dp.coerceAtLeast(1.dp))
                ) {
                    row.forEach { label ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .background(Color(palette.keySurface), shape)
                                .border(
                                    0.8.dp,
                                    Color(palette.border),
                                    shape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                label,
                                color = Color(palette.onKey),
                                fontSize = (18 * profile.fontScale).sp
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(profile.keyInsetDp.dp.coerceAtLeast(1.dp))
            ) {
                PreviewKey("🌐", palette, profile, Modifier.weight(0.8f))
                PreviewKey("العربية", palette, profile, Modifier.weight(3.2f))
                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(46.dp)
                        .background(Color(palette.actionSurface), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("→", color = Color(palette.onAction), fontSize = 25.sp)
                }
            }
        }
    }
}

@Composable
private fun PreviewKey(
    label: String,
    palette: ThemePalette,
    profile: ThemeProfile,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(46.dp)
            .background(Color(palette.functionalSurface), RoundedCornerShape(profile.cornerRadiusDp.dp))
            .border(0.8.dp, Color(palette.border), RoundedCornerShape(profile.cornerRadiusDp.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color(palette.onFunctional), fontSize = (15 * profile.fontScale).sp)
    }
}

@Composable
private fun ThemeSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    palette: ThemePalette,
    onChanged: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = Color(palette.onKey), modifier = Modifier.weight(1f))
            Text(valueText, color = Color(palette.secondaryText), fontSize = 12.sp)
        }
        Slider(value = value, onValueChange = onChanged, valueRange = valueRange)
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    palette: ThemePalette,
    onChecked: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color(palette.onKey))
            Text(subtitle, color = Color(palette.secondaryText), fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
