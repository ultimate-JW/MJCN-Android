package com.ultimatejw.mjcn.ui.auth.signup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {

    // Step 1
    var name: String = ""
    var grade: Int = 0
    var semester: Int = 0
    var graduationYear: Int? = null
    var entranceYear: Int = 0
    var graduationTerm: String? = null

    // Step 2
    var email: String = ""
    var password: String = ""
    var college: String = ""
    var department: String = ""
    var major: String = ""

    // Step 3
    val selectedInterests = mutableListOf<String>()

    private val _step1Valid = MutableStateFlow(false)
    val step1Valid: StateFlow<Boolean> = _step1Valid.asStateFlow()

    private val _step2Valid = MutableStateFlow(false)
    val step2Valid: StateFlow<Boolean> = _step2Valid.asStateFlow()

    private val _step3Valid = MutableStateFlow(false)
    val step3Valid: StateFlow<Boolean> = _step3Valid.asStateFlow()

    private val _majorStepValid = MutableStateFlow(false)
    val majorStepValid: StateFlow<Boolean> = _majorStepValid.asStateFlow()

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z]{2,10}$")
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
        _step3Valid.value = selectedInterests.isNotEmpty()
    }
}
