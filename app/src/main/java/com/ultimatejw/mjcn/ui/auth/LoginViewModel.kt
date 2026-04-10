package com.ultimatejw.mjcn.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.repository.UserRepository
import com.ultimatejw.mjcn.utils.isValidEmail
import com.ultimatejw.mjcn.utils.isValidPassword
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginEvent {
    data object NavigateToMain : LoginEvent()
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _event = MutableSharedFlow<LoginEvent>()
    val event: SharedFlow<LoginEvent> = _event.asSharedFlow()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var email = ""
    private var password = ""

    fun onEmailChanged(input: String) {
        email = input
        _uiState.update { it.copy(emailError = null, isFormValid = email.isNotEmpty() && password.isNotEmpty()) }
    }

    fun onPasswordChanged(input: String) {
        password = input
        _uiState.update { it.copy(passwordError = null, isFormValid = email.isNotEmpty() && password.isNotEmpty()) }
    }

    fun login() {
        if (!email.isValidEmail()) {
            _uiState.update { it.copy(emailError = "올바른 이메일 형식을 입력해주세요.") }
            return
        }
        if (!password.isValidPassword()) {
            _uiState.update { it.copy(passwordError = "비밀번호는 8자 이상이어야 합니다.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                userRepository.saveLoginState("dummy_token") // TODO: 실제 API 연동
                _event.emit(LoginEvent.NavigateToMain)
            } catch (e: Exception) {
                _event.emit(LoginEvent.ShowError("로그인에 실패했습니다."))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
