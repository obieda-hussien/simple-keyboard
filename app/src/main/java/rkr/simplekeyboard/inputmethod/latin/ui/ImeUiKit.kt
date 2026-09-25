package rkr.simplekeyboard.inputmethod.latin.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import java.util.Locale

/** Small allocation-conscious helpers shared by IME utility surfaces. */
object ImeUiKit {
    @JvmStatic
    fun dp(context: Context, value: Float): Int =
        (value * context.resources.displayMetrics.density + 0.5f).toInt()

    @JvmStatic
    @JvmOverloads
    fun roundedBackground(
        context: Context,
        fill: Int,
        radiusDp: Float,
        stroke: Int = Color.TRANSPARENT,
        strokeWidthDp: Float = 0f
    ): GradientDrawable = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(fill)
        cornerRadius = dp(context, radiusDp).toFloat()
        if (strokeWidthDp > 0f && Color.alpha(stroke) != 0) {
            setStroke(dp(context, strokeWidthDp).coerceAtLeast(1), stroke)
        }
    }

    @JvmStatic
    fun applyPressMotion(view: View) {
        view.isHapticFeedbackEnabled = true
        view.setOnTouchListener { v, event ->
            if (!v.isEnabled) return@setOnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(0.965f).scaleY(0.965f).alpha(0.88f)
                        .setDuration(65L).start()
                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    v.animate().cancel()
                    v.animate()
                        .scaleX(1f).scaleY(1f).alpha(1f)
                        .setDuration(110L).start()
                }
            }
            false
        }
    }

    @JvmStatic
    fun animateIn(view: View) {
        view.animate().cancel()
        view.alpha = 0f
        view.scaleX = 0.975f
        view.scaleY = 0.975f
        view.translationY = dp(view.context, 8f).toFloat()
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f).scaleX(1f).scaleY(1f).translationY(0f)
            .setDuration(150L).start()
    }

    @JvmStatic
    fun animateOut(view: View, endAction: Runnable?) {
        view.animate().cancel()
        view.animate()
            .alpha(0f).scaleX(0.985f).scaleY(0.985f)
            .translationY(dp(view.context, 5f).toFloat())
            .setDuration(100L)
            .withEndAction {
                view.visibility = View.GONE
                view.alpha = 1f
                view.scaleX = 1f
                view.scaleY = 1f
                view.translationY = 0f
                endAction?.run()
            }
            .start()
    }

    @JvmStatic
    fun applyTextDirection(view: TextView, value: CharSequence?, fallbackLocale: Locale?) {
        val text = value?.toString().orEmpty()
        val direction = when {
            containsStrongRtl(text) -> View.TEXT_DIRECTION_RTL
            containsStrongLtr(text) -> View.TEXT_DIRECTION_LTR
            fallbackLocale != null &&
                TextUtils.getLayoutDirectionFromLocale(fallbackLocale) == View.LAYOUT_DIRECTION_RTL ->
                View.TEXT_DIRECTION_RTL
            else -> View.TEXT_DIRECTION_LTR
        }
        view.textDirection = direction
        view.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }

    @JvmStatic
    fun layoutDirection(locale: Locale?): Int =
        if (locale != null &&
            TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL
        ) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR

    private fun containsStrongRtl(text: String): Boolean {
        for (ch in text) {
            if (Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                Character.getDirectionality(ch) == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC
            ) return true
        }
        return false
    }

    private fun containsStrongLtr(text: String): Boolean {
        for (ch in text) {
            if (Character.getDirectionality(ch) == Character.DIRECTIONALITY_LEFT_TO_RIGHT) return true
        }
        return false
    }
}
