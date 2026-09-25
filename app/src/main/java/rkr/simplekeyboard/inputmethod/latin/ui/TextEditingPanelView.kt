package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import rkr.simplekeyboard.inputmethod.R
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette
import java.util.Locale

class TextEditingPanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    companion object {
        const val ACTION_LEFT = 1
        const val ACTION_UP = 2
        const val ACTION_DOWN = 3
        const val ACTION_RIGHT = 4
        const val ACTION_HOME = 5
        const val ACTION_END = 6
        const val ACTION_SELECT_ALL = 7
        const val ACTION_COPY = 8
        const val ACTION_CUT = 9
        const val ACTION_PASTE = 10
        const val ACTION_DELETE = 11
        const val ACTION_UNDO = 12
        const val ACTION_REDO = 13
    }

    interface Listener {
        fun onEditingAction(action: Int, extendSelection: Boolean)
        fun onCloseTextEditingPanel()
    }

    private data class ActionButton(val action: Int, val view: TextView)
    private val handler = Handler(Looper.getMainLooper())
    private val header = LinearLayout(context)
    private val title = TextView(context)
    private val close = TextView(context)
    private val grid = GridLayout(context)
    private val buttons = ArrayList<ActionButton>()
    private lateinit var selectModeButton: TextView
    private var selectionMode = false
    private var listener: Listener? = null
    private var palette: ThemePalette = ThemeEngine.palette(context)
    private var repeatRunnable: Runnable? = null

    init {
        orientation = VERTICAL
        minimumHeight = ImeUiKit.dp(context, 236f)
        setPadding(
            ImeUiKit.dp(context, 11f),
            ImeUiKit.dp(context, 7f),
            ImeUiKit.dp(context, 11f),
            ImeUiKit.dp(context, 8f)
        )

        header.orientation = HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        title.setText(R.string.text_editing_title)
        title.textSize = 20f
        title.setTypeface(null, Typeface.BOLD)
        header.addView(title, LayoutParams(0, ImeUiKit.dp(context, 38f), 1f))
        close.text = "→"
        close.textSize = 25f
        close.gravity = Gravity.CENTER
        close.contentDescription = context.getString(R.string.close_panel)
        close.setOnClickListener { listener?.onCloseTextEditingPanel() }
        ImeUiKit.applyPressMotion(close)
        header.addView(close, LayoutParams(ImeUiKit.dp(context, 46f), ImeUiKit.dp(context, 38f)))
        addView(header)

        grid.columnCount = 4
        grid.rowCount = 4
        grid.alignmentMode = GridLayout.ALIGN_BOUNDS
        addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        addRepeatable(ACTION_LEFT, "‹", R.string.edit_move_left)
        addRepeatable(ACTION_UP, "⌃", R.string.edit_move_up)
        addRepeatable(ACTION_DOWN, "⌄", R.string.edit_move_down)
        addRepeatable(ACTION_RIGHT, "›", R.string.edit_move_right)

        addAction(ACTION_HOME, "|‹", R.string.edit_home)
        selectModeButton = addToggleSelect()
        addAction(ACTION_SELECT_ALL, "▣", R.string.edit_select_all)
        addAction(ACTION_END, "›|", R.string.edit_end)

        addAction(ACTION_UNDO, "↶", R.string.edit_undo)
        addAction(ACTION_COPY, "⧉", R.string.edit_copy)
        addAction(ACTION_PASTE, "▤", R.string.edit_paste)
        addAction(ACTION_REDO, "↷", R.string.edit_redo)

        addAction(ACTION_CUT, "✂", R.string.edit_cut)
        addRepeatable(ACTION_DELETE, "⌫", R.string.a11y_key_delete)
        addSpacer()
        addSpacer()

        refreshTheme()
    }

    fun setListener(value: Listener?) {
        listener = value
    }

    fun setLanguageLocale(locale: Locale?) {
        layoutDirection = ImeUiKit.layoutDirection(locale)
    }

    fun updateState(hasSelection: Boolean, canPaste: Boolean) {
        buttons.forEach { item ->
            val enabled = when (item.action) {
                ACTION_COPY, ACTION_CUT -> hasSelection
                ACTION_PASTE -> canPaste
                else -> true
            }
            item.view.isEnabled = enabled
            item.view.alpha = if (enabled) 1f else 0.34f
        }
    }

    fun refreshTheme() {
        palette = ThemeEngine.palette(context)
        setBackgroundColor(palette.background)
        title.setTextColor(palette.onKey)
        close.setTextColor(palette.onKey)
        close.background = ImeUiKit.roundedBackground(
            context, palette.functionalSurface, 20f, palette.border, 0.7f
        )
        buttons.forEach { item -> styleAction(item.view, item.view === selectModeButton && selectionMode) }
    }

    override fun onDetachedFromWindow() {
        stopRepeating()
        super.onDetachedFromWindow()
    }

    private fun addToggleSelect(): TextView {
        val view = makeActionView("I↔", R.string.edit_select)
        view.setOnClickListener {
            selectionMode = !selectionMode
            styleAction(view, selectionMode)
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
        addToGrid(view)
        return view
    }

    private fun addAction(action: Int, glyph: String, label: Int): TextView {
        val view = makeActionView(glyph, label)
        view.setOnClickListener { listener?.onEditingAction(action, selectionMode) }
        ImeUiKit.applyPressMotion(view)
        buttons += ActionButton(action, view)
        addToGrid(view)
        return view
    }

    private fun addRepeatable(action: Int, glyph: String, label: Int): TextView {
        val view = makeActionView(glyph, label)
        view.isHapticFeedbackEnabled = true
        view.setOnTouchListener { v, event ->
            if (!v.isEnabled) return@setOnTouchListener true
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate().scaleX(0.965f).scaleY(0.965f).alpha(0.88f)
                        .setDuration(65L).start()
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    listener?.onEditingAction(action, selectionMode)
                    startRepeating(action)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopRepeating()
                    v.animate().cancel()
                    v.animate().scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(100L).start()
                }
            }
            true
        }
        buttons += ActionButton(action, view)
        addToGrid(view)
        return view
    }

    private fun startRepeating(action: Int) {
        stopRepeating()
        val runnable = object : Runnable {
            override fun run() {
                listener?.onEditingAction(action, selectionMode)
                handler.postDelayed(this, 58L)
            }
        }
        repeatRunnable = runnable
        handler.postDelayed(runnable, 360L)
    }

    private fun stopRepeating() {
        repeatRunnable?.let(handler::removeCallbacks)
        repeatRunnable = null
    }

    private fun makeActionView(glyph: String, label: Int): TextView =
        TextView(context).apply {
            text = glyph + "\n" + context.getString(label)
            textSize = 13f
            gravity = Gravity.CENTER
            maxLines = 2
            setTypeface(null, Typeface.NORMAL)
            isClickable = true
            isFocusable = true
            setPadding(
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 3f),
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 3f)
            )
        }

    private fun addToGrid(view: View) {
        grid.addView(view, GridLayout.LayoutParams().apply {
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
    }

    private fun addSpacer() {
        addToGrid(View(context))
    }

    private fun styleAction(view: TextView, selected: Boolean) {
        val fill = if (selected) palette.actionSurface else palette.functionalSurface
        val stroke = if (selected) palette.actionSurface else palette.border
        view.background = ImeUiKit.roundedBackground(context, fill, 16f, stroke, 0.7f)
        view.setTextColor(if (selected) palette.onAction else palette.onFunctional)
    }
}
