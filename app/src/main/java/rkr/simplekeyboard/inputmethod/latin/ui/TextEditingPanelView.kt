package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import rkr.simplekeyboard.inputmethod.R
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette
import java.util.Locale

/**
 * Gboard-like text editing surface. Action cells are intentionally icon-only: labels live in
 * contentDescription so the panel stays compact, scan-friendly and language-neutral.
 */
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

    private data class ActionButton(val action: Int, val view: ImageButton, val labelRes: Int)

    private val handler = Handler(Looper.getMainLooper())
    private val header = LinearLayout(context)
    private val title = TextView(context)
    private val close = ImageButton(context)
    private val grid = GridLayout(context)
    private val buttons = ArrayList<ActionButton>(14)
    private lateinit var selectModeButton: ImageButton
    private var selectionMode = false
    private var listener: Listener? = null
    private var palette: ThemePalette = ThemeEngine.palette(context)
    private var repeatRunnable: Runnable? = null

    init {
        orientation = VERTICAL
        minimumHeight = ImeUiKit.dp(context, 236f)
        setPadding(
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 7f),
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 9f)
        )

        header.orientation = HORIZONTAL
        header.gravity = Gravity.CENTER_VERTICAL
        header.setPadding(ImeUiKit.dp(context, 2f), 0, 0, ImeUiKit.dp(context, 3f))

        title.setText(R.string.text_editing_title)
        title.textSize = 18f
        title.setTypeface(null, Typeface.BOLD)
        title.gravity = Gravity.CENTER_VERTICAL
        header.addView(title, LayoutParams(0, ImeUiKit.dp(context, 38f), 1f))

        close.setImageResource(R.drawable.ic_clear)
        close.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
        close.setPadding(
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 10f),
            ImeUiKit.dp(context, 10f)
        )
        close.contentDescription = context.getString(R.string.close_panel)
        close.setOnClickListener { listener?.onCloseTextEditingPanel() }
        ImeUiKit.applyPressMotion(close)
        header.addView(close, LayoutParams(ImeUiKit.dp(context, 40f), ImeUiKit.dp(context, 40f)))
        addView(header, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))

        grid.columnCount = 4
        grid.rowCount = 4
        grid.alignmentMode = GridLayout.ALIGN_BOUNDS
        addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f))

        // Navigation row.
        addRepeatable(ACTION_LEFT, R.drawable.ic_edit_left, R.string.edit_move_left)
        addRepeatable(ACTION_UP, R.drawable.ic_edit_up, R.string.edit_move_up)
        addRepeatable(ACTION_DOWN, R.drawable.ic_edit_down, R.string.edit_move_down)
        addRepeatable(ACTION_RIGHT, R.drawable.ic_edit_right, R.string.edit_move_right)

        // Boundary and selection row.
        addAction(ACTION_HOME, R.drawable.ic_edit_home, R.string.edit_home)
        selectModeButton = addToggleSelect()
        addAction(ACTION_SELECT_ALL, R.drawable.ic_edit_select_all, R.string.edit_select_all)
        addAction(ACTION_END, R.drawable.ic_edit_end, R.string.edit_end)

        // History and clipboard row.
        addAction(ACTION_UNDO, R.drawable.ic_edit_undo, R.string.edit_undo)
        addAction(ACTION_COPY, R.drawable.ic_edit_copy, R.string.edit_copy)
        addAction(ACTION_PASTE, R.drawable.ic_edit_paste, R.string.edit_paste)
        addAction(ACTION_REDO, R.drawable.ic_edit_redo, R.string.edit_redo)

        // Destructive row: two balanced wide actions instead of two empty grid cells.
        addAction(ACTION_CUT, R.drawable.ic_edit_cut, R.string.edit_cut, span = 2)
        addRepeatable(ACTION_DELETE, R.drawable.ic_edit_delete, R.string.a11y_key_delete, span = 2)

        refreshTheme()
    }

    fun setListener(value: Listener?) {
        listener = value
    }

    fun setLanguageLocale(locale: Locale?) {
        // Cursor arrows stay physical. Only the title follows the selected language direction.
        layoutDirection = View.LAYOUT_DIRECTION_LTR
        title.text = ImeUiKit.string(context, locale, R.string.text_editing_title)
        close.contentDescription = ImeUiKit.string(context, locale, R.string.close_panel)
        buttons.forEach {
            it.view.contentDescription = ImeUiKit.string(context, locale, it.labelRes)
        }
        if (::selectModeButton.isInitialized) {
            selectModeButton.contentDescription = ImeUiKit.string(context, locale, R.string.edit_select)
        }
        ImeUiKit.applyTextDirection(title, title.text, locale)
        title.gravity = if (ImeUiKit.layoutDirection(locale) == View.LAYOUT_DIRECTION_RTL)
            Gravity.RIGHT or Gravity.CENTER_VERTICAL
        else Gravity.LEFT or Gravity.CENTER_VERTICAL
    }

    fun updateState(hasSelection: Boolean, canPaste: Boolean) {
        buttons.forEach { item ->
            val enabled = when (item.action) {
                ACTION_COPY, ACTION_CUT -> hasSelection
                ACTION_PASTE -> canPaste
                else -> true
            }
            item.view.isEnabled = enabled
            item.view.alpha = if (enabled) 1f else 0.32f
        }
    }

    fun refreshTheme() {
        palette = ThemeEngine.palette(context)
        setBackgroundColor(palette.background)
        title.setTextColor(palette.onKey)
        styleIconButton(close, selected = false)
        buttons.forEach { item ->
            styleIconButton(item.view, item.view === selectModeButton && selectionMode)
        }
    }

    override fun onDetachedFromWindow() {
        stopRepeating()
        super.onDetachedFromWindow()
    }

    private fun addToggleSelect(): ImageButton {
        val view = makeActionButton(R.drawable.ic_edit_select, R.string.edit_select)
        view.setOnClickListener {
            selectionMode = !selectionMode
            styleIconButton(view, selectionMode)
            ImeUiKit.haptic(view)
        }
        addToGrid(view)
        return view
    }

    private fun addAction(action: Int, iconRes: Int, labelRes: Int, span: Int = 1): ImageButton {
        val view = makeActionButton(iconRes, labelRes)
        view.setOnClickListener { listener?.onEditingAction(action, selectionMode) }
        ImeUiKit.applyPressMotion(view)
        buttons += ActionButton(action, view, labelRes)
        addToGrid(view, span)
        return view
    }

    private fun addRepeatable(
        action: Int,
        iconRes: Int,
        labelRes: Int,
        span: Int = 1
    ): ImageButton {
        val view = makeActionButton(iconRes, labelRes)
        view.isHapticFeedbackEnabled = true
        view.setOnTouchListener { v, event ->
            if (!v.isEnabled) return@setOnTouchListener true
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate().scaleX(0.95f).scaleY(0.95f).alpha(0.84f)
                        .setDuration(55L).start()
                    ImeUiKit.haptic(v)
                    listener?.onEditingAction(action, selectionMode)
                    startRepeating(action)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    stopRepeating()
                    v.animate().cancel()
                    v.animate().scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(95L).start()
                }
            }
            true
        }
        buttons += ActionButton(action, view, labelRes)
        addToGrid(view, span)
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

    private fun makeActionButton(iconRes: Int, labelRes: Int): ImageButton =
        ImageButton(context).apply {
            setImageResource(iconRes)
            contentDescription = context.getString(labelRes)
            scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
            isClickable = true
            isFocusable = true
            setPadding(
                ImeUiKit.dp(context, 13f),
                ImeUiKit.dp(context, 10f),
                ImeUiKit.dp(context, 13f),
                ImeUiKit.dp(context, 10f)
            )
        }

    private fun addToGrid(view: View, span: Int = 1) {
        grid.addView(view, GridLayout.LayoutParams().apply {
            width = 0
            height = 0
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, span, span.toFloat())
            rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            setMargins(
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 4f),
                ImeUiKit.dp(context, 4f)
            )
        })
    }

    private fun styleIconButton(view: ImageButton, selected: Boolean) {
        val fill = if (selected) palette.actionSurface else palette.functionalSurface
        val stroke = if (selected) palette.actionSurface else palette.border
        val tint = if (selected) palette.onAction else palette.onFunctional
        view.background = ImeUiKit.roundedBackground(context, fill, 16f, stroke, 0.75f)
        view.imageTintList = ColorStateList.valueOf(tint)
    }
}
