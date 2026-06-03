package com.ultimatejw.mjcn.ui.main.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ultimatejw.mjcn.data.remote.dto.course.CourseListDto
import com.ultimatejw.mjcn.data.remote.dto.course.CourseOfferingDto
import com.ultimatejw.mjcn.data.remote.dto.course.CourseScheduleDto
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
import kotlinx.coroutines.async
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
            // 같은 과목의 다른 분반이 있으면 제거 후 새 분반 추가
            if (offeringId != null) {
                selectedCurrentCourses.removeAll { it.name == name && it.offeringId != offeringId }
                originalEnrolledOfferingIds.remove(offeringId)
            }
            selectedCurrentCourses.add(SelectedCourse(name = name, meta = meta, offeringId = offeringId))
        } else {
            if (offeringId != null) {
                selectedCurrentCourses.removeAll { it.offeringId == offeringId }
                originalEnrolledOfferingIds.remove(offeringId)
            } else {
                selectedCurrentCourses.removeAll { it.name == name }
            }
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
            val q = query.takeIf { it.isNotBlank() }
            val majorDeferred = async { coursesRepository.searchCourses(query = q, category = "전공필수", pageSize = 300) }
            val electiveDeferred = async { coursesRepository.searchCourses(query = q, category = "전공선택", pageSize = 300) }
            val liberalCategories = listOf("공통교양", "핵심교양", "학문기초교양", "일반교양", "자유선택")
            val liberalDeferreds = liberalCategories.map { cat ->
                async { coursesRepository.searchCourses(query = q, category = cat, pageSize = 300) }
            }
            val combined = buildList {
                majorDeferred.await().onSuccess { addAll(it.results.map { dto -> dto.toCourse() }) }
                electiveDeferred.await().onSuccess { addAll(it.results.map { dto -> dto.toCourse() }) }
                liberalDeferreds.forEach { it.await().onSuccess { res -> addAll(res.results.map { dto -> dto.toCourse() }) } }
            }
            if (combined.isNotEmpty()) _courseSearchResults.value = combined
        }
    }

    fun onOfferingQueryChanged(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isNotBlank()) delay(300)
            val q = query.takeIf { it.isNotBlank() }
            val allOfferings = mutableListOf<CourseOfferingDto>()
            var page = 1
            while (true) {
                val result = coursesRepository.searchOfferings(query = q, page = page, pageSize = 300)
                val data = result.getOrNull() ?: break
                allOfferings.addAll(data.results)
                if (data.next == null) break
                page++
            }
            if (allOfferings.isNotEmpty()) {
                _offeringSearchResults.value = allOfferings.map { it.toCourse() }
            }
        }
    }

    private fun CourseListDto.toCourse(): Course {
        val meta = listOfNotNull(college, department, major)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
            .ifEmpty { category }
        return Course(name = name, meta = meta, code = courseCode, category = category)
    }

    private val offeringScheduleMap = mutableMapOf<Int, List<CourseScheduleDto>>()

    private fun CourseOfferingDto.toCourse(): Course {
        offeringScheduleMap[id] = schedules
        val timePart = schedules.joinToString(" · ") {
            "${it.dayOfWeek} ${it.startTime.take(5)}~${it.endTime.take(5)}"
        }
        val meta = listOfNotNull(
            professor?.takeIf { it.isNotBlank() },
            timePart.takeIf { it.isNotBlank() }
        ).joinToString(" · ").ifEmpty { category }
        return Course(name = name, meta = meta, code = courseCode, offeringId = id, category = category)
    }

    fun hasTimeConflict(offeringId: Int): Boolean {
        val candidate = offeringScheduleMap[offeringId] ?: return false
        val selectedIds = selectedCurrentCourses.mapNotNull { it.offeringId }
            .filter { it != offeringId }
        return selectedIds.any { selectedId ->
            val selected = offeringScheduleMap[selectedId] ?: return@any false
            candidate.any { cs ->
                selected.any { ss ->
                    cs.dayOfWeek == ss.dayOfWeek && cs.startTime < ss.endTime && ss.startTime < cs.endTime
                }
            }
        }
    }

    // ── Course history prefill ───────────────────────────────────────────────
    private val originalCourseHistoryIds = mutableListOf<Int>()

    private val _courseHistoryLoaded = MutableStateFlow(false)
    val courseHistoryLoaded: StateFlow<Boolean> = _courseHistoryLoaded.asStateFlow()

    fun loadCourseHistory() {
        _courseHistoryLoaded.value = false
        viewModelScope.launch {
            profileRepository.getProfile().onSuccess { profile ->
                selectedCourseHistory.clear()
                originalCourseHistoryIds.clear()
                profile.courseHistories?.forEach { dto ->
                    originalCourseHistoryIds.add(dto.id)
                    selectedCourseHistory.add(
                        SelectedCourse(
                            name = dto.courseName,
                            meta = dto.category,
                            courseCode = dto.courseCode,
                            grade = dto.gradeReceived,
                            year = dto.year,
                            semester = dto.semester
                        )
                    )
                }
            }
            _courseHistoryLoaded.value = true
        }
    }

    // ── Current courses prefill ──────────────────────────────────────────────
    private val courseNameToEnrollmentId = mutableMapOf<String, Int>()
    private val originalEnrolledOfferingIds = mutableSetOf<Int>()

    fun isOriginalEnrollment(offeringId: Int): Boolean = offeringId in originalEnrolledOfferingIds

    private val _currentCoursesLoaded = MutableStateFlow(false)
    val currentCoursesLoaded: StateFlow<Boolean> = _currentCoursesLoaded.asStateFlow()

    fun loadCurrentCourses() {
        _currentCoursesLoaded.value = false
        viewModelScope.launch {
            profileRepository.getProfile().onSuccess { profile ->
                selectedCurrentCourses.clear()
                courseNameToEnrollmentId.clear()
                originalEnrolledOfferingIds.clear()
                profile.currentCourses?.forEach { dto ->
                    courseNameToEnrollmentId[dto.courseName] = dto.id
                    val offeringId = dto.offeringId ?: dto.id
                    originalEnrolledOfferingIds.add(offeringId)
                    // offerings 페이지네이션 완료 전에도 시간 충돌 판단이 되도록 미리 채움
                    // (offerings 로딩 완료 시 toCourse()에서 완전한 스케줄로 덮어씌워짐)
                    if (dto.dayOfWeek != null && dto.startTime != null && dto.endTime != null) {
                        offeringScheduleMap[offeringId] = listOf(
                            CourseScheduleDto(dayOfWeek = dto.dayOfWeek, startTime = dto.startTime, endTime = dto.endTime, room = null)
                        )
                    }
                    val timePart = dto.dayOfWeek?.takeIf { it.isNotBlank() }?.let { day ->
                        val start = dto.startTime?.take(5).orEmpty()
                        val end = dto.endTime?.take(5).orEmpty()
                        when {
                            start.isNotBlank() && end.isNotBlank() -> "$day $start~$end"
                            start.isNotBlank() -> "$day $start"
                            else -> day
                        }
                    }
                    val meta = listOfNotNull(
                        dto.professor?.takeIf { it.isNotBlank() },
                        timePart
                    ).joinToString(" · ")
                    selectedCurrentCourses.add(
                        SelectedCourse(name = dto.courseName, meta = meta, offeringId = offeringId)
                    )
                }
            }
            _currentCoursesLoaded.value = true
        }
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
                // 기존 관심분야 전체 삭제
                val existing = interestRepository.listInterests().getOrElse { emptyList() }
                for ((id, _) in existing) {
                    val result = interestRepository.deleteInterest(id)
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "기존 관심분야 삭제에 실패했습니다."))
                        return@launch
                    }
                }
                // 선택된 관심분야 저장
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
                for (id in originalCourseHistoryIds) {
                    val result = courseHistoryRepository.deleteCourseHistory(id)
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "수강이력 저장에 실패했습니다."))
                        return@launch
                    }
                }
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
                originalCourseHistoryIds.clear()
                refreshUserFromProfile()
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
                for (id in courseNameToEnrollmentId.values) {
                    val result = currentCourseRepository.deleteCurrentCourse(id)
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "수강 과목 저장에 실패했습니다."))
                        return@launch
                    }
                }
                for (course in selectedCurrentCourses) {
                    val offeringId = course.offeringId ?: continue
                    val result = currentCourseRepository.createCurrentCourse(offeringId)
                    if (result.isFailure) {
                        _saveResult.send(ProfileSaveResult.Failure(result.exceptionOrNull()?.message ?: "수강 과목 저장에 실패했습니다."))
                        return@launch
                    }
                }
                refreshUserFromProfile()
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
