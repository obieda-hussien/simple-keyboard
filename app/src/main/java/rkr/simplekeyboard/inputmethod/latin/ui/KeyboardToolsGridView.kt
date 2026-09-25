package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import rkr.simplekeyboard.inputmethod.R
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette
import java.util.Locale

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
        val icon: TextView,
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
            ImeUiKit.dp(context, 12f),
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 12f),
            ImeUiKit.dp(context, 10f)
        )
        minimumHeight = ImeUiKit.dp(context, 236f)

        grid.columnCount = 2
        grid.rowCount = 4
        grid.alignmentMode = GridLayout.ALIGN_BOUNDS
        addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))

        addTool("☺", R.string.tool_emoji) { listener?.onEmojiTool() }
        addTool("▣", R.string.tool_clipboard) { listener?.onClipboardTool() }
        addTool("I↔", R.string.tool_text_editing) { listener?.onTextEditingTool() }
        addTool("A↔ع", R.string.tool_translate) { listener?.onTranslateTool() }
        addTool("●", R.string.tool_voice) { listener?.onVoiceTool() }
        addTool("▧", R.string.tool_image) { listener?.onImageTool() }
        addTool("◐", R.string.tool_theme) { listener?.onThemeTool() }
        addTool("⚙", R.string.tool_settings) { listener?.onSettingsTool() }

        refreshTheme()
    }

    fun setListener(value: Listener?) {
        listener = value
    }

    fun setLanguageLocale(locale: Locale?) {
        // Keep tool positions physically stable for muscle memory; only labels follow text direction.
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
                22f,
                palette.border,
                0.8f
            )
            card.icon.setTextColor(palette.onFunctional)
            card.label.setTextColor(palette.onFunctional)
        }
    }

    private fun addTool(iconText: String, labelRes: Int, action: () -> Unit) {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setPadding(
                ImeUiKit.dp(context, 8f),
                ImeUiKit.dp(context, 9f),
                ImeUiKit.dp(context, 8f),
                ImeUiKit.dp(context, 9f)
            )
            setOnClickListener { action() }
        }
        ImeUiKit.applyPressMotion(card)

        val icon = TextView(context).apply {
            text = iconText
            textSize = if (iconText.length > 2) 18f else 25f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val label = TextView(context).apply {
            setText(labelRes)
            textSize = 13f
            gravity = Gravity.CENTER
            maxLines = 1
        }
        card.addView(icon, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        card.addView(label, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        val params = GridLayout.LayoutParams().apply {
            width = 0
            height = 0
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(
                ImeUiKit.dp(context, 5f),
                ImeUiKit.dp(context, 5f),
                ImeUiKit.dp(context, 5f),
                ImeUiKit.dp(context, 5f)
            )
        }
        grid.addView(card, params)
        cards += ToolCard(card, icon, label)
    }
}
