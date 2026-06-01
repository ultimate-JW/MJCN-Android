package com.ultimatejw.mjcn.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileResponse
import com.ultimatejw.mjcn.domain.repository.CurrentCourseRepository
import com.ultimatejw.mjcn.domain.repository.InterestRepository
import com.ultimatejw.mjcn.domain.repository.ProfileRepository
import com.ultimatejw.mjcn.domain.repository.UserRepository
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val interestRepository: InterestRepository,
    private val userRepository: UserRepository,
    private val currentCourseRepository: CurrentCourseRepository
) : ViewModel() {

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z]{2,10}$")
        const val OTHER_INTEREST_LABEL = "기타(직접 입력)"
        private const val OTHER_INTEREST_MIN_LENGTH = 2
        private const val OTHER_INTEREST_MAX_LENGTH = 100
    }

    // Profile data
    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile.asStateFlow()

    // Basic info fields
    var name: String = ""
    var grade: Int = 0
    var semester: Int = 0
    var entranceYear: Int = 0
    var graduationTerm: String? = null
    var selectedGradeText: String? = null
    var selectedSemesterText: String? = null
    var selectedEntranceYearText: String? = null

    // Major fields
    var college: String = ""
    var department: String = ""
    var major: String = ""

    // Interest fields
    val selectedInterests = mutableListOf<String>()
    var otherInterestText: String = ""

    // Current course fields
    val selectedCurrentCourses = mutableListOf<SelectedCourse>()

    fun findCurrentCourse(name: String): SelectedCourse? =
        selectedCurrentCourses.firstOrNull { it.name == name }

    fun toggleCurrentCourse(name: String, meta: String) {
        if (findCurrentCourse(name) == null) {
            selectedCurrentCourses.add(SelectedCourse(name = name, meta = meta))
        } else {
            selectedCurrentCourses.removeAll { it.name == name }
        }
    }

    // Validation StateFlows
    private val _basicValid = MutableStateFlow(false)
    val basicValid: StateFlow<Boolean> = _basicValid.asStateFlow()

    private val _majorValid = MutableStateFlow(false)
    val majorValid: StateFlow<Boolean> = _majorValid.asStateFlow()

    private val _interestValid = MutableStateFlow(false)
    val interestValid: StateFlow<Boolean> = _interestValid.asStateFlow()

    // Save state
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveResult = Channel<ProfileSaveResult>(Channel.BUFFERED)
    val saveResult = _saveResult.receiveAsFlow()

    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getProfile()
                .onSuccess { _profile.value = it }
                .onFailure { /* Silently ignore; user can still edit manually */ }
        }
    }

    fun isNameValid(name: String): Boolean = name.isEmpty() || NAME_REGEX.matches(name)

    fun onBasicChanged(name: String, grade: Int, semester: Int, entranceYear: Int) {
        this.name = name
        this.grade = grade
        this.semester = semester
        this.entranceYear = entranceYear
        _basicValid.value = NAME_REGEX.matches(name) && grade > 0 && semester > 0 && entranceYear > 0
    }

    fun onMajorChanged(college: String, department: String, major: String) {
        this.college = college
        this.department = department
        this.major = major
        _majorValid.value = college.isNotEmpty() && department.isNotEmpty() && major.isNotEmpty()
    }

    fun onInterestToggled(interest: String, selected: Boolean) {
        if (selected && selectedInterests.size < 3) {
            selectedInterests.add(interest)
        } else {
            selectedInterests.remove(interest)
        }
        refreshInterestValid()
    }

    fun onOtherInterestTextChanged(text: String) {
        otherInterestText = text
        refreshInterestValid()
    }

    private fun refreshInterestValid() {
        val hasSelection = selectedInterests.isNotEmpty()
        val otherSelected = selectedInterests.contains(OTHER_INTEREST_LABEL)
        val otherTextLen = otherInterestText.trim().length
        val otherTextValid = otherTextLen in OTHER_INTEREST_MIN_LENGTH..OTHER_INTEREST_MAX_LENGTH
        _interestValid.value = hasSelection && (!otherSelected || otherTextValid)
    }

    fun saveBasic() {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val (gradYear, gradMonth) = parseGraduationTerm(graduationTerm)
                profileRepository.patchProfile(
                    name = name.takeIf { it.isNotBlank() },
                    grade = grade.takeIf { it > 0 },
                    semester = semester.takeIf { it > 0 },
                    admissionYear = entranceYear.takeIf { it > 0 },
                    graduationYear = gradYear,
                    graduationMonth = gradMonth
                )
                    .onSuccess {
                        refreshUserFromProfile()
                        _saveResult.send(ProfileSaveResult.Success)
                    }
                    .onFailure { t -> _saveResult.send(ProfileSaveResult.Failure(t.message ?: "저장에 실패했습니다.")) }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun saveMajor() {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val combinedMajor = "$college · $department · $major"
                profileRepository.patchProfile(major = combinedMajor)
                    .onSuccess {
                        refreshUserFromProfile()
                        _saveResult.send(ProfileSaveResult.Success)
                    }
                    .onFailure { t -> _saveResult.send(ProfileSaveResult.Failure(t.message ?: "저장에 실패했습니다.")) }
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun saveInterests() {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                for (label in selectedInterests) {
                    val isOther = label == OTHER_INTEREST_LABEL
                    val category = if (isOther) "기타" else label
                    val customText = if (isOther) otherInterestText.trim() else ""
                    val result = interestRepository.createInterest(category, customText)
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "관심분야 저장에 실패했습니다."))
                        return@launch
                    }
                }
                refreshLocalInterests()
                _saveResult.send(ProfileSaveResult.Success)
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun saveCurrentCourses() {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                for (course in selectedCurrentCourses) {
                    val result = currentCourseRepository.createCurrentCourse(
                        courseName = course.name,
                        courseCode = "",
                        dayOfWeek = "",
                        startTime = "",
                        endTime = "",
                        professor = "",
                        room = "",
                        building = ""
                    )
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "수강 과목 저장에 실패했습니다."))
                        return@launch
                    }
                }
                _saveResult.send(ProfileSaveResult.Success)
            } finally {
                _isSaving.value = false
            }
        }
    }

    private suspend fun refreshUserFromProfile() {
        profileRepository.getProfile().onSuccess { profile ->
            val currentUser = userRepository.currentUser.first() ?: return@onSuccess
            val gradDate = if (profile.graduationYear != null && profile.graduationMonth != null)
                "${profile.graduationYear}년 ${profile.graduationMonth}월"
            else currentUser.graduationDate
            val parts = profile.major?.split(" · ")
            userRepository.saveUser(
                currentUser.copy(
                    name = profile.name ?: currentUser.name,
                    grade = profile.grade ?: currentUser.grade,
                    semester = profile.semester ?: currentUser.semester,
                    entranceYear = profile.admissionYear ?: currentUser.entranceYear,
                    graduationYear = profile.graduationYear ?: currentUser.graduationYear,
                    graduationDate = gradDate,
                    college = parts?.getOrNull(0) ?: currentUser.college,
                    department = parts?.getOrNull(1) ?: currentUser.department,
                    major = parts?.getOrNull(2) ?: currentUser.major,
                )
            )
        }
    }

    private suspend fun refreshLocalInterests() {
        val currentUser = userRepository.currentUser.first() ?: return
        val labels = selectedInterests.map { if (it == OTHER_INTEREST_LABEL) otherInterestText.trim() else it }
        userRepository.saveUser(currentUser.copy(interests = labels))
    }

    /** "2026년 2월" → 2026, 2 / "선택 안 함" 또는 빈 문자열 → null, null */
    private fun parseGraduationTerm(term: String?): Pair<Int?, Int?> {
        if (term.isNullOrBlank() || term == "선택 안 함") return null to null
        val year = Regex("(\\d{4})년").find(term)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val month = Regex("(\\d{1,2})월").find(term)?.groupValues?.getOrNull(1)?.toIntOrNull()
        return year to month
    }
}
