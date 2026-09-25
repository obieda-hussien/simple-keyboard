package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import rkr.simplekeyboard.inputmethod.R
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette
import java.util.Locale

class ClipboardPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    interface Listener {
        fun onPasteClipboardItem(text: String)
        fun onToggleClipboardPin(text: String)
        fun onClearClipboardHistory()
        fun onCloseClipboardPanel()
    }

    private val header = LinearLayout(context)
    private val title = TextView(context)
    private val clear = TextView(context)
    private val close = TextView(context)
    private val scroll = ScrollView(context)
    private val grid = GridLayout(context)
    private val cardViews = ArrayList<View>()
    private var listener: Listener? = null
    private var locale: Locale? = null
    private var palette: ThemePalette = ThemeEngine.palette(context)

    init {
        orientation = VERTICAL
        minimumHeight = ImeUiKit.dp(context, 236f)
        setPadding(
            ImeUiKit.dp(context, 12f),
            ImeUiKit.dp(context, 8f),
            ImeUiKit.dp(context, 12f),
            ImeUiKit.dp(context, 8f)
        )

        header.orientation = HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        title.setText(R.string.clipboard_panel_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        header.addView(title, LayoutParams(0, ImeUiKit.dp(context, 42f), 1f))

        clear.setText(R.string.clipboard_history_clear)
        clear.gravity = Gravity.CENTER
        clear.textSize = 12f
        clear.setPadding(ImeUiKit.dp(context, 12f), 0, ImeUiKit.dp(context, 12f), 0)
        clear.setOnClickListener { listener?.onClearClipboardHistory() }
        ImeUiKit.applyPressMotion(clear)
        header.addView(clear, LayoutParams(LayoutParams.WRAP_CONTENT, ImeUiKit.dp(context, 38f)))

        close.text = "✕"
        close.gravity = Gravity.CENTER
        close.textSize = 19f
        close.contentDescription = context.getString(R.string.clipboard_history_close)
        close.setOnClickListener { listener?.onCloseClipboardPanel() }
        ImeUiKit.applyPressMotion(close)
        header.addView(close, LayoutParams(ImeUiKit.dp(context, 42f), ImeUiKit.dp(context, 42f)))
        addView(header, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        grid.columnCount = 2
        grid.alignmentMode = GridLayout.ALIGN_BOUNDS
        scroll.isFillViewport = true
        scroll.addView(grid, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ))
        addView(scroll, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        refreshTheme()
    }

    fun setListener(value: Listener?) {
        listener = value
    }

    fun setLanguageLocale(value: Locale?) {
        locale = value
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        ImeUiKit.applyTextDirection(title, title.text, value)
        title.gravity = if (ImeUiKit.layoutDirection(value) == View.LAYOUT_DIRECTION_RTL)
            Gravity.RIGHT or Gravity.CENTER_VERTICAL else Gravity.LEFT or Gravity.CENTER_VERTICAL
    }

    fun setEntries(entries: List<String>?, pinned: Set<String>?) {
        grid.removeAllViews()
        cardViews.clear()
        val safeEntries = entries.orEmpty()
        if (safeEntries.isEmpty()) {
            val empty = TextView(context).apply {
                setText(R.string.clipboard_history_empty)
                textSize = 15f
                gravity = Gravity.CENTER
                setPadding(16, 32, 16, 32)
            }
            grid.addView(empty, GridLayout.LayoutParams().apply {
                width = 0
                height = ImeUiKit.dp(context, 110f)
                columnSpec = GridLayout.spec(0, 2, 1f)
            })
            cardViews += empty
            refreshTheme()
            return
        }

        safeEntries.forEach { entry ->
            addEntryCard(entry, pinned?.contains(entry) == true)
        }
        refreshTheme()
    }

    fun refreshTheme() {
        palette = ThemeEngine.palette(context)
        setBackgroundColor(palette.background)
        title.setTextColor(palette.onKey)
        clear.setTextColor(palette.accent)
        close.setTextColor(palette.onKey)
        clear.background = ImeUiKit.roundedBackground(
            context, palette.functionalSurface, 18f, palette.border, 0.7f
        )
        close.background = ImeUiKit.roundedBackground(
            context, palette.functionalSurface, 21f, palette.border, 0.7f
        )
        cardViews.forEach { view ->
            view.background = ImeUiKit.roundedBackground(
                context, palette.keySurface, 18f, palette.border, 0.8f
            )
            if (view is LinearLayout) {
                for (i in 0 until view.childCount) {
                    (view.getChildAt(i) as? TextView)?.let {
                        it.setTextColor(if (i == 1) palette.accent else palette.onKey)
                    }
                }
            } else if (view is TextView) {
                view.setTextColor(palette.secondaryText)
            }
        }
    }

    private fun addEntryCard(text: String, isPinned: Boolean) {
        val card = LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            isClickable = true
            isFocusable = true
            setPadding(
                ImeUiKit.dp(context, 13f),
                ImeUiKit.dp(context, 10f),
                ImeUiKit.dp(context, 13f),
                ImeUiKit.dp(context, 9f)
            )
            contentDescription = context.getString(R.string.clipboard_history_paste) + ": " + text
            setOnClickListener { listener?.onPasteClipboardItem(text) }
            setOnLongClickListener {
                ImeUiKit.haptic(this)
                listener?.onToggleClipboardPin(text)
                true
            }
        }
        ImeUiKit.applyPressMotion(card)

        val preview = TextView(context).apply {
            this.text = text.replace('\n', ' ')
            textSize = 14f
            maxLines = 3
            ellipsize = TextUtils.TruncateAt.END
            gravity = Gravity.START or Gravity.CENTER_VERTICAL
            ImeUiKit.applyTextDirection(this, this.text, locale)
        }
        val state = TextView(context).apply {
            setText(if (isPinned) R.string.clipboard_pinned else R.string.clipboard_long_press_pin)
            textSize = 10.5f
            maxLines = 1
            gravity = Gravity.START
        }
        card.addView(preview, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))
        card.addView(state, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        grid.addView(card, GridLayout.LayoutParams().apply {
            width = 0
            height = ImeUiKit.dp(context, 104f)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(
                ImeUiKit.dp(context, 5f),
                ImeUiKit.dp(context, 5f),
                ImeUiKit.dp(context, 5f),
                ImeUiKit.dp(context, 5f)
            )
        })
        cardViews += card
    }
}
