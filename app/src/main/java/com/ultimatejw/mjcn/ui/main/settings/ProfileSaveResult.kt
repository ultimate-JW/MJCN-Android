package com.ultimatejw.mjcn.ui.main.settings

sealed class ProfileSaveResult {
    object Success : ProfileSaveResult()
    data class Failure(val message: String) : ProfileSaveResult()
}
