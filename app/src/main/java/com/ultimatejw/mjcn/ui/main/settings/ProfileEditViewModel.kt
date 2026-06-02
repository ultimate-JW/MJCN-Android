package com.ultimatejw.mjcn.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.remote.dto.course.CourseListDto
import com.ultimatejw.mjcn.data.remote.dto.course.CourseOfferingDto
import com.ultimatejw.mjcn.data.remote.dto.profile.ProfileResponse
import com.ultimatejw.mjcn.domain.repository.CourseHistoryRepository
import com.ultimatejw.mjcn.domain.repository.CoursesRepository
import com.ultimatejw.mjcn.domain.repository.CurrentCourseRepository
import com.ultimatejw.mjcn.domain.repository.InterestRepository
import com.ultimatejw.mjcn.domain.repository.ProfileRepository
import com.ultimatejw.mjcn.domain.repository.UserRepository
import com.ultimatejw.mjcn.ui.auth.signup.Course
import com.ultimatejw.mjcn.ui.auth.signup.SelectedCourse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    private val courseHistoryRepository: CourseHistoryRepository,
    private val currentCourseRepository: CurrentCourseRepository,
    private val coursesRepository: CoursesRepository
) : ViewModel() {

    companion object {
        private val NAME_REGEX = Regex("^[가-힣a-zA-Z]{2,10}$")
        const val OTHER_INTEREST_LABEL = "기타(직접 입력)"
        private const val OTHER_INTEREST_MIN_LENGTH = 2
        private const val OTHER_INTEREST_MAX_LENGTH = 100
        private val KNOWN_INTEREST_LABELS = setOf(
            "IT/개발", "디자인", "마케팅/광고", "금융/회계", "교육",
            "공기업/공공기관", "의료/바이오", "미디어/콘텐츠", "건축/공간",
            "스포츠/예술", "연구/R&D"
        )
    }

    // ── Profile ──────────────────────────────────────────────────────────────
    private val _profile = MutableStateFlow<ProfileResponse?>(null)
    val profile: StateFlow<ProfileResponse?> = _profile.asStateFlow()

    // ── Basic info ───────────────────────────────────────────────────────────
    var name: String = ""
    var grade: Int = 0
    var semester: Int = 0
    var entranceYear: Int = 0
    var graduationTerm: String? = null
    var selectedGradeText: String? = null
    var selectedSemesterText: String? = null
    var selectedEntranceYearText: String? = null

    // ── Major ────────────────────────────────────────────────────────────────
    var college: String = ""
    var department: String = ""
    var major: String = ""

    // ── Interests ────────────────────────────────────────────────────────────
    val selectedInterests = mutableListOf<String>()
    var otherInterestText: String = ""

    // ── Course history (수강이력) ──────────────────────────────────────────
    val selectedCourseHistory = mutableListOf<SelectedCourse>()

    fun findCourseHistory(name: String): SelectedCourse? =
        selectedCourseHistory.firstOrNull { it.name == name }

    fun toggleCourseHistory(name: String, meta: String, courseCode: String) {
        if (findCourseHistory(name) == null) {
            selectedCourseHistory.add(SelectedCourse(name = name, meta = meta, courseCode = courseCode))
        } else {
            selectedCourseHistory.removeAll { it.name == name }
        }
    }

    fun setCourseHistoryGrade(name: String, grade: String) {
        findCourseHistory(name)?.grade = grade
    }

    fun setCourseHistoryYear(name: String, year: Int) {
        findCourseHistory(name)?.year = year
    }

    fun setCourseHistorySemester(name: String, semester: Int) {
        findCourseHistory(name)?.semester = semester
    }

    // ── Current courses (현재 수강과목) ────────────────────────────────────
    val selectedCurrentCourses = mutableListOf<SelectedCourse>()

    fun findCurrentCourse(name: String): SelectedCourse? =
        selectedCurrentCourses.firstOrNull { it.name == name }

    fun findCurrentCourseByOfferingId(offeringId: Int): SelectedCourse? =
        selectedCurrentCourses.firstOrNull { it.offeringId == offeringId }

    fun toggleCurrentCourse(name: String, meta: String, offeringId: Int?) {
        val existing = if (offeringId != null) findCurrentCourseByOfferingId(offeringId)
                       else findCurrentCourse(name)
        if (existing == null) {
            selectedCurrentCourses.add(SelectedCourse(name = name, meta = meta, offeringId = offeringId))
        } else {
            if (offeringId != null) selectedCurrentCourses.removeAll { it.offeringId == offeringId }
            else selectedCurrentCourses.removeAll { it.name == name }
        }
    }

    // ── Course API search ────────────────────────────────────────────────────
    private val _courseSearchResults = MutableStateFlow<List<Course>>(emptyList())
    val courseSearchResults: StateFlow<List<Course>> = _courseSearchResults.asStateFlow()

    private val _offeringSearchResults = MutableStateFlow<List<Course>>(emptyList())
    val offeringSearchResults: StateFlow<List<Course>> = _offeringSearchResults.asStateFlow()

    private var searchJob: Job? = null

    fun onCourseQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotBlank()) delay(300)
            coursesRepository.searchCourses(query = query.takeIf { it.isNotBlank() }, pageSize = 300)
                .onSuccess { _courseSearchResults.value = it.results.map { dto -> dto.toCourse() } }
        }
    }

    fun onOfferingQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotBlank()) delay(300)
            coursesRepository.searchOfferings(query = query.takeIf { it.isNotBlank() }, pageSize = 300)
                .onSuccess { _offeringSearchResults.value = it.results.map { dto -> dto.toCourse() } }
        }
    }

    private fun CourseListDto.toCourse(): Course {
        val meta = listOfNotNull(college, department, major)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifEmpty { category }
        return Course(name = name, meta = meta, code = courseCode, category = category)
    }

    private fun CourseOfferingDto.toCourse(): Course {
        val schedule = schedules.firstOrNull()
        val timePart = schedule?.let { "${it.dayOfWeek} ${it.startTime.take(5)}" }.orEmpty()
        val meta = listOfNotNull(
            professor?.takeIf { it.isNotBlank() },
            timePart.takeIf { it.isNotBlank() }
        ).joinToString(" · ").ifEmpty { category }
        return Course(name = name, meta = meta, code = courseCode, offeringId = id, category = category)
    }

    // ── Interests prefill ────────────────────────────────────────────────────
    private val _interestsLoaded = MutableStateFlow(false)
    val interestsLoaded: StateFlow<Boolean> = _interestsLoaded.asStateFlow()

    fun loadInterests() {
        _interestsLoaded.value = false
        viewModelScope.launch {
            val user = userRepository.currentUser.first() ?: run {
                _interestsLoaded.value = true
                return@launch
            }
            selectedInterests.clear()
            otherInterestText = ""
            for (interest in user.interests) {
                when {
                    interest in KNOWN_INTEREST_LABELS -> selectedInterests.add(interest)
                    selectedInterests.none { it == OTHER_INTEREST_LABEL } -> {
                        selectedInterests.add(OTHER_INTEREST_LABEL)
                        otherInterestText = interest
                    }
                }
            }
            refreshInterestValid()
            _interestsLoaded.value = true
        }
    }

    // ── Validation ───────────────────────────────────────────────────────────
    private val _basicValid = MutableStateFlow(false)
    val basicValid: StateFlow<Boolean> = _basicValid.asStateFlow()

    private val _majorValid = MutableStateFlow(false)
    val majorValid: StateFlow<Boolean> = _majorValid.asStateFlow()

    private val _interestValid = MutableStateFlow(false)
    val interestValid: StateFlow<Boolean> = _interestValid.asStateFlow()

    // ── Save state ───────────────────────────────────────────────────────────
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveResult = Channel<ProfileSaveResult>(Channel.BUFFERED)
    val saveResult = _saveResult.receiveAsFlow()

    // ── Load ─────────────────────────────────────────────────────────────────
    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getProfile()
                .onSuccess { _profile.value = it }
                .onFailure { }
        }
    }

    // ── Validation helpers ───────────────────────────────────────────────────
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

    // ── Save actions ─────────────────────────────────────────────────────────
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
                    .onSuccess { refreshUserFromProfile(); _saveResult.send(ProfileSaveResult.Success) }
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
                profileRepository.patchProfile(major = "$college · $department · $major")
                    .onSuccess { refreshUserFromProfile(); _saveResult.send(ProfileSaveResult.Success) }
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
                    val result = interestRepository.createInterest(
                        category = if (isOther) "기타" else label,
                        customText = if (isOther) otherInterestText.trim() else ""
                    )
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

    fun saveCourseHistory() {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                for (course in selectedCourseHistory) {
                    val year = course.year ?: continue
                    val semester = course.semester ?: continue
                    val result = courseHistoryRepository.createCourseHistory(
                        courseCode = course.courseCode,
                        year = year,
                        semester = semester,
                        gradeReceived = course.grade ?: ""
                    )
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "수강이력 저장에 실패했습니다."))
                        return@launch
                    }
                }
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
                    val offeringId = course.offeringId ?: continue
                    val result = currentCourseRepository.createCurrentCourse(offeringId)
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

    // ── Private helpers ──────────────────────────────────────────────────────
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

    private fun parseGraduationTerm(term: String?): Pair<Int?, Int?> {
        if (term.isNullOrBlank() || term == "선택 안 함") return null to null
        val year = Regex("(\\d{4})년").find(term)?.groupValues?.getOrNull(1)?.toIntOrNull()
        val month = Regex("(\\d{1,2})월").find(term)?.groupValues?.getOrNull(1)?.toIntOrNull()
        return year to month
    }
}
