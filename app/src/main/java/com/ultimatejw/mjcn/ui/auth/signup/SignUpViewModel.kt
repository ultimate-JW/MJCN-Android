package com.ultimatejw.mjcn.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ultimatejw.mjcn.data.repository.AuthApiException
import com.ultimatejw.mjcn.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignupResult {
    data object Success : SignupResult()
    data class EmailError(val message: String, val rejectedEmail: String) : SignupResult()
    data class PasswordError(val message: String) : SignupResult()
    data class PasswordConfirmError(val message: String) : SignupResult()
    data class GeneralError(val message: String) : SignupResult()
}

sealed class VerifyEmailResult {
    data object Success : VerifyEmailResult()
    data class Failure(val message: String) : VerifyEmailResult()
}

sealed class ResendResult {
    data object Success : ResendResult()
    data class Failure(val message: String) : ResendResult()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // Step 1
    var name: String = ""
    var grade: Int = 0
    var semester: Int = 0
    var graduationYear: Int? = null
    var entranceYear: Int = 0
    var graduationTerm: String? = null
    var selectedGradeText: String? = null
    var selectedSemesterText: String? = null
    var selectedEntranceYearText: String? = null

    // Step 2
    var email: String = ""
    var password: String = ""
    var college: String = ""
    var department: String = ""
    var major: String = ""

    // Step 3
    val selectedInterests = mutableListOf<String>()
    var otherInterestText: String = ""

    // Step 4 - 수강 이력
    val selectedCourses = mutableListOf<SelectedCourse>()

    fun findSelectedCourse(name: String): SelectedCourse? =
        selectedCourses.firstOrNull { it.name == name }

    fun addSelectedCourse(name: String) {
        if (findSelectedCourse(name) == null) {
            selectedCourses.add(SelectedCourse(name))
        }
    }

    fun removeSelectedCourse(name: String) {
        selectedCourses.removeAll { it.name == name }
    }

    fun setCourseGrade(name: String, grade: String) {
        findSelectedCourse(name)?.grade = grade
    }

    // Step 5 - 현재 수강 과목
    val selectedCurrentCourses = mutableListOf<SelectedCourse>()

    fun findCurrentCourse(name: String): SelectedCourse? =
        selectedCurrentCourses.firstOrNull { it.name == name }

    fun addCurrentCourse(name: String) {
        if (findCurrentCourse(name) == null) {
            selectedCurrentCourses.add(SelectedCourse(name))
        }
    }

    fun removeCurrentCourse(name: String) {
        selectedCurrentCourses.removeAll { it.name == name }
    }

    private val _step1Valid = MutableStateFlow(false)
    val step1Valid: StateFlow<Boolean> = _step1Valid.asStateFlow()

    private val _step2Valid = MutableStateFlow(false)
    val step2Valid: StateFlow<Boolean> = _step2Valid.asStateFlow()

    private val _step3Valid = MutableStateFlow(false)
    val step3Valid: StateFlow<Boolean> = _step3Valid.asStateFlow()

    private val _majorStepValid = MutableStateFlow(false)
    val majorStepValid: StateFlow<Boolean> = _majorStepValid.asStateFlow()

    private val _isSignupLoading = MutableStateFlow(false)
    val isSignupLoading: StateFlow<Boolean> = _isSignupLoading.asStateFlow()

    private val _signupResult = Channel<SignupResult>(Channel.BUFFERED)
    val signupResult = _signupResult.receiveAsFlow()

    private val _isVerifyLoading = MutableStateFlow(false)
    val isVerifyLoading: StateFlow<Boolean> = _isVerifyLoading.asStateFlow()

    private val _verifyResult = Channel<VerifyEmailResult>(Channel.BUFFERED)
    val verifyResult = _verifyResult.receiveAsFlow()

    private val _resendResult = Channel<ResendResult>(Channel.BUFFERED)
    val resendResult = _resendResult.receiveAsFlow()

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z]{2,10}$")
        const val OTHER_INTEREST_LABEL = "기타(직접 입력)"
        const val OTHER_INTEREST_MIN_LENGTH = 2
        const val OTHER_INTEREST_MAX_LENGTH = 100
    }

    fun onStep1Changed(name: String, grade: Int, semester: Int, entranceYear: Int = 0) {
        this.name = name
        this.grade = grade
        this.semester = semester
        this.entranceYear = entranceYear
        // 이름 정규식 검증, 입학 연도 필수 조건 추가
        _step1Valid.value = NAME_REGEX.matches(name) && grade > 0 && semester > 0 && entranceYear > 0
    }

    // 이름 유효성 검사 메서드 (Fragment에서 에러 표시용)
    fun isNameValid(name: String): Boolean = name.isEmpty() || NAME_REGEX.matches(name)

    fun onStep2Changed(email: String, password: String, passwordConfirm: String) {
        this.email = email
        this.password = password
        _step2Valid.value = email.isNotEmpty() && password.length >= 8 && password == passwordConfirm
    }

    fun onMajorStepChanged(college: String, department: String, major: String) {
        this.college = college
        this.department = department
        this.major = major
        _majorStepValid.value = college.isNotEmpty() && department.isNotEmpty() && major.isNotEmpty()
    }

    fun onInterestToggled(interest: String, selected: Boolean) {
        if (selected && selectedInterests.size < 3) {
            selectedInterests.add(interest)
        } else {
            selectedInterests.remove(interest)
        }
        refreshStep3Valid()
    }

    /** 기타 직접 입력 텍스트 변경 시 유효성 재평가 */
    fun onOtherInterestTextChanged(text: String) {
        otherInterestText = text
        refreshStep3Valid()
    }

    private fun refreshStep3Valid() {
        val hasSelection = selectedInterests.isNotEmpty()
        val otherSelected = selectedInterests.contains(OTHER_INTEREST_LABEL)
        val otherTextLen = otherInterestText.trim().length
        val otherTextValid = otherTextLen in OTHER_INTEREST_MIN_LENGTH..OTHER_INTEREST_MAX_LENGTH
        // 기타가 선택된 경우 다른 칩 동반 여부와 무관하게 입력 텍스트가 2~100자여야 함
        _step3Valid.value = hasSelection && (!otherSelected || otherTextValid)
    }

    fun requestSignup(email: String, password: String, passwordConfirm: String) {
        if (_isSignupLoading.value) return
        viewModelScope.launch {
            _isSignupLoading.value = true
            val result = authRepository.signup(email, password, passwordConfirm)
            _isSignupLoading.value = false
            result
                .onSuccess {
                    this@SignUpViewModel.email = email
                    this@SignUpViewModel.password = password
                    _signupResult.send(SignupResult.Success)
                }
                .onFailure { throwable ->
                    val errorBody = (throwable as? AuthApiException)?.errorBody.orEmpty()
                    val fallback = throwable.message ?: "회원가입에 실패했습니다."
                    _signupResult.send(parseSignupErrorBody(errorBody, email, fallback))
                }
        }
    }

    private fun parseSignupErrorBody(body: String, email: String, fallback: String): SignupResult {
        if (body.isBlank()) return SignupResult.GeneralError(fallback)
        return try {
            val json = JsonParser.parseString(body).asJsonObject
            val emailMsg = json.firstStringIn("email")
            val passwordMsg = json.firstStringIn("password")
            val passwordConfirmMsg = json.firstStringIn("password_confirm")
            val nonFieldMsg = json.firstStringIn("non_field_errors")
            val detailMsg = json.get("detail")?.takeIf { !it.isJsonNull }?.asString
            when {
                emailMsg != null -> SignupResult.EmailError(emailMsg, email)
                passwordMsg != null -> SignupResult.PasswordError(passwordMsg)
                passwordConfirmMsg != null -> SignupResult.PasswordConfirmError(passwordConfirmMsg)
                nonFieldMsg != null -> SignupResult.GeneralError(nonFieldMsg)
                detailMsg != null -> SignupResult.GeneralError(detailMsg)
                else -> SignupResult.GeneralError(fallback)
            }
        } catch (_: Exception) {
            SignupResult.GeneralError(fallback)
        }
    }

    private fun JsonObject.firstStringIn(key: String): String? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return when {
            element.isJsonArray -> (element as JsonArray)
                .takeIf { it.size() > 0 }
                ?.get(0)
                ?.takeIf { !it.isJsonNull }
                ?.asString
            element.isJsonPrimitive -> element.asString
            else -> null
        }
    }

    fun verifyEmail(code: String) {
        if (_isVerifyLoading.value) return
        val targetEmail = email
        if (targetEmail.isBlank()) {
            viewModelScope.launch {
                _verifyResult.send(VerifyEmailResult.Failure("이메일 정보가 없습니다. 이전 단계부터 다시 진행해주세요."))
            }
            return
        }
        viewModelScope.launch {
            _isVerifyLoading.value = true
            val result = authRepository.verifyEmail(targetEmail, code)
            _isVerifyLoading.value = false
            result
                .onSuccess { _verifyResult.send(VerifyEmailResult.Success) }
                .onFailure {
                    _verifyResult.send(VerifyEmailResult.Failure("인증 코드가 일치하지 않습니다."))
                }
        }
    }

    fun resendVerification() {
        val targetEmail = email
        if (targetEmail.isBlank()) {
            viewModelScope.launch {
                _resendResult.send(ResendResult.Failure("이메일 정보가 없습니다. 이전 단계부터 다시 진행해주세요."))
            }
            return
        }
        viewModelScope.launch {
            val result = authRepository.resendVerification(targetEmail)
            result
                .onSuccess { _resendResult.send(ResendResult.Success) }
                .onFailure {
                    _resendResult.send(ResendResult.Failure("인증 코드 재전송에 실패했습니다."))
                }
        }
    }
}
