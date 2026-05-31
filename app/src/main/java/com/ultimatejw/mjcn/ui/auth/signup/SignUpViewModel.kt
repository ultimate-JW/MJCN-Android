package com.ultimatejw.mjcn.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.ultimatejw.mjcn.data.local.TokenStore
import com.ultimatejw.mjcn.data.repository.AuthApiException
import com.ultimatejw.mjcn.domain.repository.AuthRepository
import com.ultimatejw.mjcn.domain.repository.CourseHistoryRepository
import com.ultimatejw.mjcn.domain.repository.CurrentCourseRepository
import com.ultimatejw.mjcn.domain.repository.InterestRepository
import com.ultimatejw.mjcn.domain.repository.ProfileRepository
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

/** 인증 직후 자동 로그인 결과. */
sealed class AutoLoginResult {
    data object Success : AutoLoginResult()
    data class Failure(val message: String) : AutoLoginResult()
}

/** 각 단계별 서버 저장 결과. */
sealed class StepSaveResult {
    data class Success(val step: Int) : StepSaveResult()
    data class Failure(val step: Int, val message: String) : StepSaveResult()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val interestRepository: InterestRepository,
    private val courseHistoryRepository: CourseHistoryRepository,
    private val currentCourseRepository: CurrentCourseRepository,
    private val tokenStore: TokenStore
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

    fun addSelectedCourse(name: String, meta: String = "") {
        if (findSelectedCourse(name) == null) {
            selectedCourses.add(SelectedCourse(name = name, meta = meta))
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

    fun addCurrentCourse(name: String, meta: String = "") {
        if (findCurrentCourse(name) == null) {
            selectedCurrentCourses.add(SelectedCourse(name = name, meta = meta))
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

    private val _autoLoginResult = Channel<AutoLoginResult>(Channel.BUFFERED)
    val autoLoginResult = _autoLoginResult.receiveAsFlow()

    private val _stepSaveResult = Channel<StepSaveResult>(Channel.BUFFERED)
    val stepSaveResult = _stepSaveResult.receiveAsFlow()

    private val _isStepSaveLoading = MutableStateFlow(false)
    val isStepSaveLoading: StateFlow<Boolean> = _isStepSaveLoading.asStateFlow()

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

    /**
     * 인증 직후 자동 로그인. 이메일/비밀번호는 회원가입 시 저장해둔 값을 사용.
     * 성공하면 TokenStore에 JWT가 저장되어 이후 API에 자동으로 첨부된다.
     */
    fun performAutoLogin() {
        val targetEmail = email
        val targetPassword = password
        if (targetEmail.isBlank() || targetPassword.isBlank()) {
            viewModelScope.launch {
                _autoLoginResult.send(AutoLoginResult.Failure("자동 로그인에 필요한 정보가 없습니다."))
            }
            return
        }
        viewModelScope.launch {
            authRepository.login(targetEmail, targetPassword)
                .onSuccess { _autoLoginResult.send(AutoLoginResult.Success) }
                .onFailure { throwable ->
                    val msg = (throwable as? AuthApiException)?.let { "자동 로그인 실패: HTTP ${it.code}" }
                        ?: "자동 로그인에 실패했습니다."
                    _autoLoginResult.send(AutoLoginResult.Failure(msg))
                }
        }
    }

    /**
     * Step1 ~ Step5 사용자 입력을 한 번에 서버에 저장.
     * 중간 단계에서 부분 저장을 하면 사용자가 뒤로 가기 → 다시 진행 시 중복 레코드/유니크 제약 위반으로
     * 500이 나는 문제가 있어, Step5 "다음" 클릭 시점에만 일괄 POST/PATCH를 수행한다.
     *
     * 순서:
     *   1) PATCH /profile/  (Step1 + Step2 정보)
     *   2) POST /interests/ × N  (Step3 관심분야)
     *   3) POST /course-history/ × N  (Step4 수강이력)
     *   4) POST /current-courses/ × N  (Step5 현재 수강 과목)
     *   5) PATCH /profile/ is_onboarding_completed=true
     *
     * 중간에 실패하면 즉시 중단하고 Failure 발행. 사용자는 Step1부터 다시 시작해야 한다.
     */
    fun saveAllAndCompleteOnboarding() {
        if (_isStepSaveLoading.value) return
        viewModelScope.launch {
            _isStepSaveLoading.value = true
            try {
                // 1) Step1 + Step2 → PATCH /profile/
                val (gradYear, gradMonth) = parseGraduationTerm(graduationTerm)
                val combinedMajor = listOf(college, department, major)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                val profileResult = profileRepository.patchProfile(
                    name = name.takeIf { it.isNotBlank() },
                    grade = grade.takeIf { it > 0 },
                    semester = semester.takeIf { it > 0 },
                    admissionYear = entranceYear.takeIf { it > 0 },
                    graduationYear = gradYear,
                    graduationMonth = gradMonth,
                    major = combinedMajor.takeIf { it.isNotBlank() }
                )
                if (profileResult.isFailure) {
                    emitFailure(profileResult.exceptionOrNull()!!, "기본 정보 저장에 실패했습니다.")
                    return@launch
                }

                // 2) Step3 → POST /interests/
                for (label in selectedInterests) {
                    val isOther = label == OTHER_INTEREST_LABEL
                    val category = if (isOther) "기타" else label
                    val customText = if (isOther) otherInterestText.trim() else ""
                    val r = interestRepository.createInterest(category, customText)
                    if (r.isFailure) {
                        emitFailure(r.exceptionOrNull()!!, "관심분야 저장에 실패했습니다.")
                        return@launch
                    }
                }

                // 3) Step4 (course-history) / 4) Step5 (current-courses) 는
                //    서버의 Course 마스터 테이블과 course_code FK 매칭이 필요해
                //    placeholder 코드로는 IntegrityError → 500 이 발생함을 확인.
                //    백엔드와 course_code 매핑 확정 전까지 저장은 보류.
                //    사용자가 선택한 selectedCourses / selectedCurrentCourses 는
                //    ViewModel 메모리에 남아 있으므로 추후 연결만 하면 됨.

                // 5) 온보딩 완료 마킹
                profileRepository.patchProfile(isOnboardingCompleted = true)
                    .onSuccess {
                        tokenStore.setOnboardingCompleted(true)
                        _stepSaveResult.send(StepSaveResult.Success(step = 5))
                    }
                    .onFailure { t ->
                        emitFailure(t, "온보딩 완료 처리에 실패했습니다.")
                    }
            } finally {
                _isStepSaveLoading.value = false
            }
        }
    }

    private suspend fun emitFailure(t: Throwable, fallback: String) {
        _stepSaveResult.send(StepSaveResult.Failure(5, errorMessage(t, fallback)))
    }

    /** "2026년 2월" → 2026, 2 / "선택 안 함" 또는 빈 문자열 → null, null */
    private fun parseGraduationTerm(term: String?): Pair<Int?, Int?> {
        if (term.isNullOrBlank() || term == "선택 안 함") return null to null
        val year = Regex("(\\d{4})년").find(term)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val month = Regex("(\\d{1,2})월").find(term)?.groupValues?.getOrNull(1)?.toIntOrNull()
        return year to month
    }

    private fun errorMessage(t: Throwable, fallback: String): String {
        val api = t as? AuthApiException ?: return t.message ?: fallback
        // 응답 본문은 Logcat에도 풀로 남겨 토스트가 잘릴 때 확인 가능하게 함.
        android.util.Log.e(
            "SignUpViewModel",
            "API error: HTTP ${api.code}, body=${api.errorBody}"
        )
        val detail = api.errorBody.takeIf { it.isNotBlank() }?.let { body ->
            " - ${body.take(200)}"
        }.orEmpty()
        return "$fallback (HTTP ${api.code})$detail"
    }
}
