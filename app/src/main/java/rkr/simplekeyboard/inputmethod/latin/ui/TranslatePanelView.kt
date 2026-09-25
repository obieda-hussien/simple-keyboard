package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.graphics.Typeface
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import rkr.simplekeyboard.inputmethod.R
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeEngine
import rkr.simplekeyboard.inputmethod.latin.settings.ThemePalette
import java.util.Locale

class TranslatePanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    interface Listener {
        fun onTranslateRequested(text: String)
        fun onReplaceTranslation(text: String)
        fun onInsertTranslation(text: String)
        fun onCloseTranslatePanel()
    }

    private val top = LinearLayout(context)
    private val sourceLanguage = TextView(context)
    private val arrow = TextView(context)
    private val targetLanguage = TextView(context)
    private val close = TextView(context)
    private val source = TextView(context)
    private val result = TextView(context)
    private val actions = LinearLayout(context)
    private val translateButton = TextView(context)
    private val replaceButton = TextView(context)
    private val insertButton = TextView(context)
    private var listener: Listener? = null
    private var locale: Locale? = null
    private var canReplaceSource = false
    private var palette: ThemePalette = ThemeEngine.palette(context)

    init {
        orientation = VERTICAL
        minimumHeight = ImeUiKit.dp(context, 236f)
        setPadding(
            ImeUiKit.dp(context, 12f),
            ImeUiKit.dp(context, 8f),
            ImeUiKit.dp(context, 12f),
            ImeUiKit.dp(context, 9f)
        )

        top.orientation = HORIZONTAL
        top.gravity = Gravity.CENTER_VERTICAL
        styleChip(sourceLanguage)
        styleChip(targetLanguage)
        sourceLanguage.setText(R.string.translate_auto)
        targetLanguage.setText(R.string.translate_external_app)
        arrow.text = "↔"
        arrow.textSize = 20f
        arrow.gravity = Gravity.CENTER
        close.text = "→"
        close.textSize = 25f
        close.gravity = Gravity.CENTER
        close.contentDescription = context.getString(R.string.close_panel)
        close.setOnClickListener { listener?.onCloseTranslatePanel() }
        ImeUiKit.applyPressMotion(close)

        top.addView(sourceLanguage, LayoutParams(0, ImeUiKit.dp(context, 38f), 1f))
        top.addView(arrow, LayoutParams(ImeUiKit.dp(context, 42f), ImeUiKit.dp(context, 38f)))
        top.addView(targetLanguage, LayoutParams(0, ImeUiKit.dp(context, 38f), 1f))
        top.addView(close, LayoutParams(ImeUiKit.dp(context, 44f), ImeUiKit.dp(context, 38f)))
        addView(top)

        source.textSize = 15f
        source.maxLines = 3
        source.ellipsize = TextUtils.TruncateAt.END
        source.setPadding(
            ImeUiKit.dp(context, 14f),
            ImeUiKit.dp(context, 11f),
            ImeUiKit.dp(context, 14f),
            ImeUiKit.dp(context, 11f)
        )
        addView(source, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply {
            topMargin = ImeUiKit.dp(context, 7f)
            bottomMargin = ImeUiKit.dp(context, 6f)
        })

        result.textSize = 15f
        result.maxLines = 3
        result.ellipsize = TextUtils.TruncateAt.END
        result.setPadding(
            ImeUiKit.dp(context, 14f),
            ImeUiKit.dp(context, 11f),
            ImeUiKit.dp(context, 14f),
            ImeUiKit.dp(context, 11f)
        )
        result.setText(R.string.translate_result_hint)
        addView(result, LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply {
            bottomMargin = ImeUiKit.dp(context, 7f)
        })

        actions.orientation = HORIZONTAL
        actions.gravity = Gravity.CENTER
        configureAction(translateButton, R.string.translate_action) {
            val value = source.text?.toString().orEmpty()
            if (value.isNotBlank()) listener?.onTranslateRequested(value)
        }
        configureAction(replaceButton, R.string.translate_replace) {
            val value = result.text?.toString().orEmpty()
            if (value.isNotBlank() && value != context.getString(R.string.translate_result_hint)) {
                listener?.onReplaceTranslation(value)
            }
        }
        configureAction(insertButton, R.string.translate_insert) {
            val value = result.text?.toString().orEmpty()
            if (value.isNotBlank() && value != context.getString(R.string.translate_result_hint)) {
                listener?.onInsertTranslation(value)
            }
        }
        actions.addView(translateButton, LayoutParams(0, ImeUiKit.dp(context, 42f), 1f))
        actions.addView(replaceButton, LayoutParams(0, ImeUiKit.dp(context, 42f), 1f).apply {
            marginStart = ImeUiKit.dp(context, 6f)
        })
        actions.addView(insertButton, LayoutParams(0, ImeUiKit.dp(context, 42f), 1f).apply {
            marginStart = ImeUiKit.dp(context, 6f)
        })
        addView(actions)

        refreshTheme()
    }

    fun setListener(value: Listener?) {
        listener = value
    }

    fun setLanguageLocale(value: Locale?) {
        locale = value
        // Structural controls stay in fixed positions; source/result text independently follows bidi.
        layoutDirection = android.view.View.LAYOUT_DIRECTION_LTR
        sourceLanguage.text = value?.displayLanguage?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.translate_auto)
        ImeUiKit.applyTextDirection(sourceLanguage, sourceLanguage.text, value)
        ImeUiKit.applyTextDirection(targetLanguage, targetLanguage.text, value)
    }

    fun setCanReplaceSource(value: Boolean) {
        canReplaceSource = value
        updateResultActions()
    }

    fun setSourceText(value: CharSequence?) {
        source.text = value?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.translate_source_hint)
        ImeUiKit.applyTextDirection(source, source.text, locale)
        setResultText(null)
    }

    fun setResultText(value: CharSequence?) {
        result.text = value?.takeIf { it.isNotBlank() }
            ?: context.getString(R.string.translate_result_hint)
        ImeUiKit.applyTextDirection(result, result.text, locale)
        updateResultActions()
    }

    private fun updateResultActions() {
        val hasResult = result.text?.toString()?.let {
            it.isNotBlank() && it != context.getString(R.string.translate_result_hint)
        } == true
        replaceButton.isEnabled = hasResult && canReplaceSource
        insertButton.isEnabled = hasResult
        replaceButton.alpha = if (replaceButton.isEnabled) 1f else 0.36f
        insertButton.alpha = if (insertButton.isEnabled) 1f else 0.36f
    }

    fun refreshTheme() {
        palette = ThemeEngine.palette(context)
        setBackgroundColor(palette.background)
        listOf(sourceLanguage, targetLanguage, close).forEach {
            it.background = ImeUiKit.roundedBackground(
                context, palette.functionalSurface, 18f, palette.border, 0.7f
            )
            it.setTextColor(palette.onFunctional)
        }
        arrow.setTextColor(palette.secondaryText)
        source.background = ImeUiKit.roundedBackground(
            context, palette.keySurface, 18f, palette.border, 0.8f
        )
        source.setTextColor(palette.onKey)
        result.background = ImeUiKit.roundedBackground(
            context, palette.functionalSurface, 18f, palette.border, 0.8f
        )
        result.setTextColor(palette.onFunctional)
        translateButton.background = ImeUiKit.roundedBackground(
            context, palette.actionSurface, 18f, palette.actionSurface, 0.7f
        )
        translateButton.setTextColor(palette.onAction)
        listOf(replaceButton, insertButton).forEach {
            it.background = ImeUiKit.roundedBackground(
                context, palette.functionalSurface, 18f, palette.border, 0.7f
            )
            it.setTextColor(palette.onFunctional)
        }
    }

    private fun styleChip(view: TextView) {
        view.textSize = 13f
        view.gravity = Gravity.CENTER
        view.maxLines = 1
        view.ellipsize = TextUtils.TruncateAt.END
        view.setTypeface(null, Typeface.BOLD)
        view.setPadding(ImeUiKit.dp(context, 8f), 0, ImeUiKit.dp(context, 8f), 0)
    }

    private fun configureAction(view: TextView, label: Int, action: () -> Unit) {
        view.setText(label)
        view.textSize = 13f
        view.gravity = Gravity.CENTER
        view.isClickable = true
        view.isFocusable = true
        view.setOnClickListener { action() }
        ImeUiKit.applyPressMotion(view)
    }
}
