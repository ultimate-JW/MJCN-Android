package com.ultimatejw.mjcn.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun View.visible() { visibility = View.VISIBLE }
fun View.gone() { visibility = View.GONE }
fun View.invisible() { visibility = View.INVISIBLE }

fun Fragment.showToast(message: String) {
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.hideKeyboard() {
    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(requireView().windowToken, 0)
}

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassword(): Boolean = length >= 8

fun String.isValidName(): Boolean =
    matches(Regex("^[가-힣a-zA-Z]{2,10}$"))

fun String.toRelativeTime(): String {
    return try {
        val normalized = this.replace("Z", "").split(".")[0]
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val date = sdf.parse(normalized) ?: return this
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < 60_000L -> "방금 전"
            diff < 3_600_000L -> "${diff / 60_000}분 전"
            diff < 86_400_000L -> "${diff / 3_600_000}시간 전"
            diff < 2 * 86_400_000L -> "어제"
            else -> "${diff / 86_400_000}일 전"
        }
    } catch (e: Exception) {
        this
    }
}
