package com.ultimatejw.mjcn.ui.auth.signup

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.ultimatejw.mjcn.R

class SignupEmailSentDialog : DialogFragment() {

    companion object {
        const val REQUEST_KEY = "SignupEmailSentDialog.REQUEST_KEY"
        const val RESULT_CONFIRMED = "confirmed"
        const val TAG = "SignupEmailSentDialog"

        private const val HORIZONTAL_MARGIN_DP = 18f
        // 박스가 187dp일 때 중앙 위치 기준으로, 203dp(16dp 늘림)로 바뀐 후에도
        // 박스 위쪽이 원래 위치 그대로 유지되도록 박스를 8dp 아래로 내림.
        private const val VERTICAL_OFFSET_DP = 8f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_signup_email_sent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_dialog_confirm).setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_CONFIRMED to true))
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // 뒷배경 어둡게 (스크림)
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            val params = attributes
            params.dimAmount = 0.5f
            params.gravity = Gravity.CENTER
            params.y = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                VERTICAL_OFFSET_DP,
                resources.displayMetrics
            ).toInt()
            attributes = params

            val marginPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                HORIZONTAL_MARGIN_DP,
                resources.displayMetrics
            ).toInt()
            val screenWidth = resources.displayMetrics.widthPixels
            val width = screenWidth - 2 * marginPx
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }
}
