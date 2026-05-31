package com.ultimatejw.mjcn.ui.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.ultimatejw.mjcn.R

class LoadingDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.dialog_loading, container, false)

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    companion object {
        private const val TAG = "LoadingDialog"

        fun show(manager: FragmentManager) {
            if (manager.findFragmentByTag(TAG) == null) {
                LoadingDialog().show(manager, TAG)
            }
        }

        fun hide(manager: FragmentManager) {
            (manager.findFragmentByTag(TAG) as? LoadingDialog)?.dismissAllowingStateLoss()
        }
    }
}
