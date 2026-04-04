package com.ultimatejw.mjcn.ui.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SignUpViewModel : ViewModel() {

    // Step 1
    var name: String = ""
    var grade: Int = 0
    var semester: Int = 0
    var graduationYear: Int? = null

    // Step 2
    var email: String = ""
    var password: String = ""

    // Step 3
    val selectedInterests = mutableListOf<String>()

    private val _step1Valid = MutableLiveData(false)
    val step1Valid: LiveData<Boolean> = _step1Valid

    private val _step2Valid = MutableLiveData(false)
    val step2Valid: LiveData<Boolean> = _step2Valid

    private val _step3Valid = MutableLiveData(false)
    val step3Valid: LiveData<Boolean> = _step3Valid

    fun onStep1Changed(name: String, grade: Int, semester: Int) {
        this.name = name
        this.grade = grade
        this.semester = semester
        _step1Valid.value = name.isNotEmpty() && grade > 0 && semester > 0
    }

    fun onStep2Changed(email: String, password: String, passwordConfirm: String) {
        this.email = email
        this.password = password
        _step2Valid.value = email.isNotEmpty() && password.length >= 8 && password == passwordConfirm
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
