# MJCN 프로젝트 규칙

## 커밋
- **절대로 사용자의 명시적 요청 없이 커밋하지 말 것**
- 작업이 끝나면 변경 내용을 요약하고 커밋 여부를 물어볼 것

## 프로필 편집 화면
- `ProfileEditBasicFragment` ~ `ProfileEditCurrentCourseFragment` 는 회원가입용 레이아웃(`fragment_signup_step1~5.xml`)을 **그대로 재사용**한다.
- XML을 직접 수정하면 회원가입 플로우에도 영향을 미치므로, 편집 전용 레이아웃 변경은 Fragment에서 `ConstraintSet`으로 처리할 것.
- 프로필 저장 API 호출 후 반드시 `userRepository.saveUser()`로 로컬 Room DB를 갱신해야 한다. 이렇게 해야 `ObserveCurrentUserUseCase` Flow를 구독하는 `HomeFragment`·`SettingsFragment`가 자동으로 재렌더링된다.
- 로딩 표시는 `LoadingDialog.show/hide(childFragmentManager)` 를 사용한다.

## 수강이력 / 현재수강과목 (Course API)
- 과목 목록: `GET /api/v1/courses/` — 전체 228개, pageSize=300으로 한 번에 로드
- 분반(현재수강): `GET /api/v1/courses/offerings/` — offering_id 기반으로 선택
- **수강이력 저장**: `POST /api/v1/accounts/course-history/` 에 `course_code + year + semester + grade_received` 전송. `course_name/category/credits`는 서버가 자동 hydrate.
- **현재수강 저장**: `POST /api/v1/accounts/current-courses/` 에 `offering_id` 하나만 전송. 나머지 7개 필드는 서버 자동 hydrate.
- 과목 선택 UI: `CourseAdapter` — `selectionProvider: (Course) -> SelectedCourse?` 로 Course 객체 기반 조회. offerings는 반드시 `offeringId` 기반으로 선택 상태 판단 (이름 기반 fallback 금지 — 같은 이름 다른 분반이 전부 선택돼 보이는 버그 발생).
- 전공/교양 탭: `CourseTabHelper.kt`의 `filterByTab()` / `applyTabStyle()` 사용. 전공 = category ∈ {"전공필수","전공선택"}, 나머지는 교양.
- `SelectedCourse`에 `courseCode`, `offeringId`, `year`, `semester` 필드 있음.

## 관심분야 (Interest)
- 프로필 편집 시 `ProfileEditViewModel.loadInterests()`로 `userRepository.currentUser`에서 기존 관심분야를 prefill.
- "기타(직접 입력)" 칩은 API 저장 시 category="기타" + custom_text로 분리되어 전송됨.
- `InterestAreaCategoryEnum` 값과 칩 텍스트가 일치해야 함 (예: "IT/개발", "공기업/공공기관" 등).

## API 엔드포인트 주의사항
- `/api/v1/notices/` 와 `/api/v1/information/` URL이 서버에서 반대로 동작 — 의도적 교차 매핑 유지할 것.
- `GET /api/v1/accounts/profile/` 응답에 `interests`, `course_histories`, `current_courses` 포함됨 (`ProfileResponse` DTO에 반영됨).
