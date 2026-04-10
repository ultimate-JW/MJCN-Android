package com.ultimatejw.mjcn.ui.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {

    // Step 1 - 기본 정보
    var name: String = ""
    var grade: Int = 0
    var semester: Int = 0
    var graduationYear: Int? = null
    // [추가] 입학 연도, 졸업 희망 시기 필드
    var entranceYear: Int? = null
    var graduationDate: String? = null   // "2028년 2월" 형식

    // Step 2 - 전공 선택 (기존 이메일/비밀번호에서 변경)
    // [수정] Step2를 전공 선택 화면으로 변경
    var email: String = ""
    var password: String = ""
    var college: String = ""       // 대학
    var department: String = ""    // 학부/학과
    var major: String = ""         // 전공

    // Step 3
    val selectedInterests = mutableListOf<String>()

    private val _step1Valid = MutableLiveData(false)
    val step1Valid: LiveData<Boolean> = _step1Valid

    private val _step2Valid = MutableLiveData(false)
    val step2Valid: LiveData<Boolean> = _step2Valid

    // [추가] 아이디/비밀번호 화면 유효성 (SignUpIdPwFragment용)
    private val _idPwValid = MutableLiveData(false)
    val idPwValid: LiveData<Boolean> = _idPwValid

    private val _step3Valid = MutableLiveData(false)
    val step3Valid: LiveData<Boolean> = _step3Valid

    /**
     * [수정] Step1 유효성 검사 - 입학 연도 필수 추가
     * 이름(2~10자 한글/영문) + 학년 + 학기 + 입학 연도 모두 입력 시 유효
     */
    fun onStep1Changed(name: String, grade: Int, semester: Int, entranceYear: Int?) {
        this.name = name
        this.grade = grade
        this.semester = semester
        this.entranceYear = entranceYear
        val nameValid = name.matches(Regex("^[가-힣a-zA-Z]{2,10}$"))
        _step1Valid.value = nameValid && grade > 0 && semester > 0 && entranceYear != null
    }

    /**
     * [수정] Step2 유효성 검사 - 전공 선택 기준
     * 대학 + 학부/학과 + 전공 모두 선택 시 유효
     */
    fun onStep2Changed(college: String, department: String, major: String) {
        this.college = college
        this.department = department
        this.major = major
        _step2Valid.value = college.isNotEmpty() && department.isNotEmpty() && major.isNotEmpty()
    }

    // [기존 유지 + 수정] 이메일/비밀번호 검증 (SignUpIdPwFragment에서 사용)
    fun onIdPwChanged(email: String, password: String, passwordConfirm: String) {
        this.email = email
        this.password = password
        _idPwValid.value = email.isNotEmpty() && password.length >= 8 && password == passwordConfirm
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
