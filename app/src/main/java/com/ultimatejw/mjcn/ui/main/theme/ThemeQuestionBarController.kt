package com.ultimatejw.mjcn.ui.main.theme

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.RectF
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.AbsoluteCornerSize

/**
 * 테마 상세 화면들에 공통으로 들어가는 inline ↔ floating 질문바 토글 + chat_detail 스타일
 * 키보드 morph 애니메이션을 하나의 컨트롤러로 캡슐화. 각 fragment 가 자기 View 참조만 전달하면 됨.
 */
class ThemeQuestionBarController(
    private val root: View,
    private val inlineInput: View,
    private val floatingInput: ConstraintLayout,
    private val cardInput: MaterialCardView,
    private val etMessage: EditText,
) {
    private var isKeyboardVisible = false
    private var animator: ValueAnimator? = null

    fun setup() {
        inlineInput.setOnClickListener { showFloating() }
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            val bottomPadding = maxOf(imeBottom, navBottom)
            val imeVisible = imeBottom > navBottom

            root.setPadding(0, 0, 0, bottomPadding)

            if (imeVisible != isKeyboardVisible) {
                isKeyboardVisible = imeVisible
                animateForKeyboard(imeVisible)
                if (!imeVisible && floatingInput.visibility == View.VISIBLE) {
                    root.postDelayed({
                        if (!isKeyboardVisible) hideFloating()
                    }, 200L)
                }
            }
            insets
        }
    }

    fun cancel() {
        animator?.cancel()
    }

    private fun animateForKeyboard(keyboardVisible: Boolean) {
        val density = root.resources.displayMetrics.density

        floatingInput.setPadding(
            floatingInput.paddingLeft,
            floatingInput.paddingTop,
            floatingInput.paddingRight,
            if (keyboardVisible) 0 else (20 * density).toInt()
        )

        val startPaddingH = floatingInput.paddingLeft.toFloat()
        val endPaddingH = if (keyboardVisible) 0f else 18 * density
        val startElevation = cardInput.cardElevation
        val endElevation = 10 * density
        val bounds = RectF(0f, 0f, cardInput.width.toFloat(), cardInput.height.toFloat())
        val startRadius = cardInput.shapeAppearanceModel.topLeftCornerSize.getCornerSize(bounds)
        val endRadius = if (keyboardVisible) 0f else 32 * density

        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 200
            interpolator = DecelerateInterpolator()
            addUpdateListener { anim ->
                val f = anim.animatedFraction
                val h = lerp(startPaddingH, endPaddingH, f).toInt()
                floatingInput.setPadding(h, floatingInput.paddingTop, h, floatingInput.paddingBottom)
                cardInput.cardElevation = lerp(startElevation, endElevation, f)
                cardInput.shapeAppearanceModel = cardInput.shapeAppearanceModel.toBuilder()
                    .setAllCornerSizes(AbsoluteCornerSize(lerp(startRadius, endRadius, f)))
                    .build()
            }
            start()
        }
    }

    private fun lerp(start: Float, end: Float, fraction: Float) =
        start + (end - start) * fraction

    private fun showFloating() {
        inlineInput.visibility = View.INVISIBLE
        floatingInput.visibility = View.VISIBLE
        etMessage.requestFocus()
        val imm = root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(etMessage, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideFloating() {
        floatingInput.visibility = View.GONE
        inlineInput.visibility = View.VISIBLE
        etMessage.clearFocus()
        etMessage.text?.clear()
    }
}
