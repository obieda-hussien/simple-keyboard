package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import rkr.simplekeyboard.inputmethod.R
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette
import java.util.Locale

/**
 * Compact 4x2 tools dashboard. Icons are app-owned vectors instead of device-default glyphs so
 * shape, tint and spacing remain consistent across Android versions and OEM skins.
 */
class KeyboardToolsGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    interface Listener {
        fun onEmojiTool()
        fun onClipboardTool()
        fun onTextEditingTool()
        fun onTranslateTool()
        fun onVoiceTool()
        fun onImageTool()
        fun onThemeTool()
        fun onSettingsTool()
    }

    private data class ToolCard(
        val view: LinearLayout,
        val icon: ImageView,
        val label: TextView
    )

    private val grid = GridLayout(context)
    private val cards = ArrayList<ToolCard>(8)
    private var listener: Listener? = null
    private var palette: ThemePalette = ThemeEngine.palette(context)

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        setPadding(
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 9f),
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 9f)
        )
        minimumHeight = ImeUiKit.dp(context, 236f)

        grid.columnCount = 4
        grid.rowCount = 2
        grid.alignmentMode = GridLayout.ALIGN_BOUNDS
        addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        addTool(R.drawable.ic_emoji, R.string.tool_emoji) { listener?.onEmojiTool() }
        addTool(R.drawable.ic_clipboard, R.string.tool_clipboard) { listener?.onClipboardTool() }
        addTool(R.drawable.ic_text_edit, R.string.tool_text_editing) { listener?.onTextEditingTool() }
        addTool(R.drawable.ic_translate, R.string.tool_translate) { listener?.onTranslateTool() }
        addTool(R.drawable.ic_mic, R.string.tool_voice) { listener?.onVoiceTool() }
        addTool(R.drawable.ic_image, R.string.tool_image) { listener?.onImageTool() }
        addTool(R.drawable.ic_palette, R.string.tool_theme) { listener?.onThemeTool() }
        addTool(R.drawable.ic_settings, R.string.tool_settings) { listener?.onSettingsTool() }

        refreshTheme()
    }

    fun setListener(value: Listener?) {
        listener = value
    }

    fun setLanguageLocale(locale: Locale?) {
        // Positions are physical and stable; only text direction changes inside each card.
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        cards.forEach { ImeUiKit.applyTextDirection(it.label, it.label.text, locale) }
    }

    fun refreshTheme() {
        palette = ThemeEngine.palette(context)
        setBackgroundColor(palette.background)
        cards.forEach { card ->
            card.view.background = ImeUiKit.roundedBackground(
                context,
                palette.functionalSurface,
                18f,
                palette.border,
                0.75f
            )
            card.icon.imageTintList = ColorStateList.valueOf(palette.onFunctional)
            card.label.setTextColor(palette.onFunctional)
        }
    }

    private fun addTool(iconRes: Int, labelRes: Int, action: () -> Unit) {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            contentDescription = context.getString(labelRes)
            setPadding(
                ImeUiKit.dp(context, 7f),
                ImeUiKit.dp(context, 8f),
                ImeUiKit.dp(context, 7f),
                ImeUiKit.dp(context, 7f)
            )
            setOnClickListener { action() }
        }
        ImeUiKit.applyPressMotion(card)

        val icon = ImageView(context).apply {
            setImageResource(iconRes)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
        }
        val label = TextView(context).apply {
            setText(labelRes)
            textSize = 11.5f
            gravity = Gravity.CENTER
            maxLines = 1
            setTypeface(null, Typeface.MEDIUM)
        }

        card.addView(icon, LayoutParams(ImeUiKit.dp(context, 28f), ImeUiKit.dp(context, 28f)).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        })
        card.addView(label, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            topMargin = ImeUiKit.dp(context, 5f)
        })

        grid.addView(card, GridLayout.LayoutParams().apply {
            width = 0
            height = 0
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 4f)
            )
        })
        cards += ToolCard(card, icon, label)
    }
}
