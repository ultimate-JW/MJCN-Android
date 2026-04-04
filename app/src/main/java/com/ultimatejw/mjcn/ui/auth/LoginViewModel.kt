package com.ultimatejw.mjcn.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.repository.UserRepository
import com.ultimatejw.mjcn.utils.isValidEmail
import com.ultimatejw.mjcn.utils.isValidPassword
import kotlinx.coroutines.launch

class LoginViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _isFormValid = MutableLiveData(false)
    val isFormValid: LiveData<Boolean> = _isFormValid

    private val _emailError = MutableLiveData<String?>()
    val emailError: LiveData<String?> = _emailError

    private val _passwordError = MutableLiveData<String?>()
    val passwordError: LiveData<String?> = _passwordError

    private var email = ""
    private var password = ""

    fun onEmailChanged(input: String) {
        email = input
        _emailError.value = null
        validateForm()
    }

    fun onPasswordChanged(input: String) {
        password = input
        _passwordError.value = null
        validateForm()
    }

    private fun validateForm() {
        _isFormValid.value = email.isNotEmpty() && password.isNotEmpty()
    }

    fun login() {
        if (!email.isValidEmail()) {
            _emailError.value = "올바른 이메일 형식을 입력해주세요."
            return
        }
        if (!password.isValidPassword()) {
            _passwordError.value = "비밀번호는 8자 이상이어야 합니다."
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            // TODO: 실제 API 연동 시 교체
            try {
                userRepository.saveLoginState("dummy_token")
                _loginState.value = LoginState.Success
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("로그인에 실패했습니다.")
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}
