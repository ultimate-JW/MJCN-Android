package com.ultimatejw.mjcn.ui.common

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.ultimatejw.mjcn.R

class ToggleSwitchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val density = resources.displayMetrics.density
    private val trackW = 46f * density
    private val trackH = 26f * density
    private val thumbR = 10f * density
    private val thumbPad = 2f * density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val thumbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

    private val colorOn = ContextCompat.getColor(context, R.color.primary)
    private val colorOff = Color.parseColor("#D9D9D9")

    var onCheckedChangeListener: ((Boolean) -> Unit)? = null

    private var _isChecked = false
    private var thumbX = thumbPad + thumbR
    private var animator: ValueAnimator? = null

    var isChecked: Boolean
        get() = _isChecked
        set(value) {
            if (_isChecked == value) return
            _isChecked = value
            if (isLaidOut) {
                animateThumb(value)
            } else {
                thumbX = if (value) trackW - thumbPad - thumbR else thumbPad + thumbR
                invalidate()
            }
            onCheckedChangeListener?.invoke(value)
        }

    init {
        isClickable = true
        isFocusable = true
        setOnClickListener { isChecked = !isChecked }
    }

    private fun animateThumb(checked: Boolean) {
        val target = if (checked) trackW - thumbPad - thumbR else thumbPad + thumbR
        animator?.cancel()
        animator = ValueAnimator.ofFloat(thumbX, target).apply {
            duration = 200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                thumbX = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(trackW.toInt(), trackH.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        trackPaint.color = if (_isChecked) colorOn else colorOff
        canvas.drawRoundRect(RectF(0f, 0f, trackW, trackH), trackH / 2f, trackH / 2f, trackPaint)
        canvas.drawCircle(thumbX, trackH / 2f, thumbR, thumbPaint)
    }
}
