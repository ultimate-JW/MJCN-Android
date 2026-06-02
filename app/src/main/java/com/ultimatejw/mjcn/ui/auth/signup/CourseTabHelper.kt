package com.ultimatejw.mjcn.ui.auth.signup

import android.graphics.Typeface
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

private val MAJOR_CATEGORIES = setOf("전공필수", "전공선택")

fun List<Course>.filterByTab(isMajorTab: Boolean): List<Course> =
    if (isMajorTab) filter { it.category in MAJOR_CATEGORIES }
    else filter { it.category !in MAJOR_CATEGORIES }

fun applyTabStyle(
    majorTab: FrameLayout, majorText: TextView, majorIndicator: View,
    liberalTab: FrameLayout, liberalText: TextView, liberalIndicator: View,
    isMajorSelected: Boolean,
    onMajor: () -> Unit,
    onLiberal: () -> Unit
) {
    fun activate(text: TextView, indicator: View) {
        text.typeface = Typeface.DEFAULT_BOLD
        text.setTextColor(text.context.getColor(com.ultimatejw.mjcn.R.color.point_color1))
        indicator.visibility = View.VISIBLE
    }
    fun deactivate(text: TextView, indicator: View) {
        text.typeface = Typeface.DEFAULT
        text.setTextColor(text.context.getColor(com.ultimatejw.mjcn.R.color.font_color2))
        indicator.visibility = View.INVISIBLE
    }

    if (isMajorSelected) {
        activate(majorText, majorIndicator)
        deactivate(liberalText, liberalIndicator)
    } else {
        deactivate(majorText, majorIndicator)
        activate(liberalText, liberalIndicator)
    }

    majorTab.setOnClickListener { onMajor() }
    liberalTab.setOnClickListener { onLiberal() }
}
