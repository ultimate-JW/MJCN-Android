package com.ultimatejw.mjcn.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.local.TokenStore
import com.ultimatejw.mjcn.data.repository.AuthApiException
import com.ultimatejw.mjcn.domain.repository.AuthRepository
import com.ultimatejw.mjcn.domain.repository.ProfileRepository
import com.ultimatejw.mjcn.utils.isValidEmail
import com.ultimatejw.mjcn.utils.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginEvent {
    /** 온보딩 완료된 사용자 — 메인으로 이동. */
    data object NavigateToMain : LoginEvent()
    /** 온보딩 재개 — 멈춘 지점부터 시작. */
    data class NavigateToStep(val step: Int) : LoginEvent()
    data class ShowError(val message: String) : LoginEvent()
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val isFormValid: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event.asSharedFlow()

    private val _uiState = MutableLiveData(LoginUiState())
    val uiState: LiveData<LoginUiState> = _uiState

    private var email = ""
    private var password = ""

    fun onEmailChanged(input: String) {
        email = input
        _uiState.value = _uiState.value!!.copy(emailError = null, isFormValid = email.isNotEmpty() && password.isNotEmpty())
    }

    fun onPasswordChanged(input: String) {
        password = input
        _uiState.value = _uiState.value!!.copy(passwordError = null, isFormValid = email.isNotEmpty() && password.isNotEmpty())
    }

    fun login() {
        if (!email.isValidEmail()) {
            _uiState.value = _uiState.value!!.copy(emailError = "올바른 이메일 형식을 입력해주세요.")
            return
        }
        if (!password.isValidPassword()) {
            _uiState.value = _uiState.value!!.copy(passwordError = "비밀번호는 8자 이상이어야 합니다.")
            return
        }

        _uiState.value = _uiState.value!!.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val loginResult = authRepository.login(email, password)
                if (loginResult.isFailure) {
                    val msg = friendlyMessage(loginResult.exceptionOrNull()!!)
                    _event.emit(LoginEvent.ShowError(msg))
                    return@launch
                }
                // 토큰/isLoggedIn 마킹은 AuthRepository 가 처리.
                // 여기서는 온보딩 진행 상태만 판단.
                val event = determineResumeTarget()
                _event.emit(event)
            } catch (e: Exception) {
                _event.emit(LoginEvent.ShowError("로그인에 실패했습니다."))
            } finally {
                _uiState.postValue(_uiState.value!!.copy(isLoading = false))
            }
        }
    }

    /**
     * 로그인 후 어디로 보낼지 결정.
     * Step1~5 저장은 Step5 "다음" 클릭 시 일괄로만 이뤄지기 때문에 중간 단계 부분 저장이 없다.
     * 따라서 분기는 단순히 onboarding 완료 여부만 본다.
     * - is_onboarding_completed=true → Main
     * - 그 외 모든 경우(부분 진행/이탈/실패) → Step1 부터 다시 입력
     */
    private suspend fun determineResumeTarget(): LoginEvent {
        val profile = profileRepository.getProfile().getOrNull()
        if (profile?.isOnboardingCompleted == true) {
            tokenStore.setOnboardingCompleted(true)
            return LoginEvent.NavigateToMain
        }
        return LoginEvent.NavigateToStep(1)
    }

    private fun friendlyMessage(t: Throwable): String {
        val api = t as? AuthApiException ?: return t.message ?: "로그인에 실패했습니다."
        return when (api.code) {
            400, 401 -> "이메일 또는 비밀번호가 올바르지 않습니다."
            403 -> "이메일 인증 후 다시 시도해주세요."
            else -> "로그인에 실패했습니다. (HTTP ${api.code})"
        }
    }
}
