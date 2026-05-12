package com.ultimatejw.mjcn.ui.main.chat
import dagger.hilt.android.AndroidEntryPoint

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ultimatejw.mjcn.databinding.DialogDeleteConfirmBinding
import com.ultimatejw.mjcn.databinding.FragmentChatDetailBinding
import com.ultimatejw.mjcn.databinding.PopupCategoryChangeBinding
import com.ultimatejw.mjcn.databinding.PopupChatMenuBinding

@AndroidEntryPoint
class ChatDetailFragment : Fragment() {

    private var _binding: FragmentChatDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionId = arguments?.getString("sessionId") ?: ""
        binding.tvTitle.text = if (sessionId.isBlank()) "새 대화" else "AI 채팅"
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnMenu.setOnClickListener { anchor ->
            showChatMenuPopup(anchor)
        }
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString()
            if (message.isBlank()) return@setOnClickListener
            binding.etMessage.text.clear()
            // TODO: AI API 호출
        }
    }

    private fun showChatMenuPopup(anchor: View) {
        val popupBinding = PopupChatMenuBinding.inflate(layoutInflater)

        val popup = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 16f
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
            animationStyle = android.R.style.Animation_Dialog
        }

        popupBinding.btnChangeCategory.setOnClickListener {
            popup.dismiss()
            showCategoryChangePopup(anchor)
        }
        popupBinding.btnDelete.setOnClickListener {
            popup.dismiss()
            showDeleteConfirmDialog()
        }

        popupBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupBinding.root.measuredWidth

        val headerLocation = IntArray(2)
        binding.layoutHeader.getLocationOnScreen(headerLocation)
        val headerBottom = headerLocation[1] + binding.layoutHeader.height

        val density = resources.displayMetrics.density
        val margin24dp = (24 * density).toInt()
        val screenWidth = resources.displayMetrics.widthPixels
        val popupX = screenWidth - popupWidth - margin24dp

        popup.showAtLocation(binding.root, Gravity.NO_GRAVITY, popupX, headerBottom)
    }

    private fun showCategoryChangePopup(anchor: View) {
        val popupBinding = PopupCategoryChangeBinding.inflate(layoutInflater)

        val popup = PopupWindow(
            popupBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 16f
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            isOutsideTouchable = true
        }

        popupBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupWidth = popupBinding.root.measuredWidth

        val headerLocation = IntArray(2)
        binding.layoutHeader.getLocationOnScreen(headerLocation)
        val headerBottom = headerLocation[1] + binding.layoutHeader.height

        val density = resources.displayMetrics.density
        val margin24dp = (24 * density).toInt()
        val screenWidth = resources.displayMetrics.widthPixels
        val popupX = screenWidth - popupWidth - margin24dp

        listOf(
            popupBinding.chipAcademic,
            popupBinding.chipCourse,
            popupBinding.chipScholarship,
            popupBinding.chipContest,
            popupBinding.chipCareer
        ).forEach { chip ->
            chip.setOnClickListener {
                // TODO: 카테고리 변경 저장
                popup.dismiss()
                showToast("카테고리가 변경되었습니다.")
            }
        }

        popup.showAtLocation(binding.root, Gravity.NO_GRAVITY, popupX, headerBottom)
    }

    private fun showDeleteConfirmDialog() {
        val dialogBinding = DialogDeleteConfirmBinding.inflate(layoutInflater)

        val density = resources.displayMetrics.density
        val dialogWidth = resources.displayMetrics.widthPixels - (48 * density).toInt()

        val dialog = Dialog(requireContext()).apply {
            setContentView(dialogBinding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        dialogBinding.btnDeleteConfirm.setOnClickListener {
            dialog.dismiss()
            showToast("채팅 내용을 삭제했습니다.")
            findNavController().popBackStack()
        }
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showToast(message: String) {
        val snackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
        val snackbarView = snackbar.view

        snackbarView.background = GradientDrawable().apply {
            setColor(Color.parseColor("#CC000000"))
            cornerRadius = 100f
        }
        snackbarView.setPadding(48, 0, 48, 0)

        val tv = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        tv.setTextColor(Color.WHITE)
        tv.textAlignment = View.TEXT_ALIGNMENT_CENTER

        val params = snackbarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.width = FrameLayout.LayoutParams.WRAP_CONTENT
        params.bottomMargin = (80 * resources.displayMetrics.density).toInt()
        snackbarView.layoutParams = params

        snackbar.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
