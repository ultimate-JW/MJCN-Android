# MJCN 프로젝트 규칙

## 커밋
- **절대로 사용자의 명시적 요청 없이 커밋하지 말 것**
- 작업이 끝나면 변경 내용을 요약하고 커밋 여부를 물어볼 것

## 프로필 편집 화면
- `ProfileEditBasicFragment` ~ `ProfileEditCurrentCourseFragment` 는 회원가입용 레이아웃(`fragment_signup_step1~5.xml`)을 **그대로 재사용**한다.
- XML을 직접 수정하면 회원가입 플로우에도 영향을 미치므로, 편집 전용 레이아웃 변경은 Fragment에서 `ConstraintSet`으로 처리할 것.
- 프로필 저장 API 호출 후 반드시 `userRepository.saveUser()`로 로컬 Room DB를 갱신해야 한다. 이렇게 해야 `ObserveCurrentUserUseCase` Flow를 구독하는 `HomeFragment`·`SettingsFragment`가 자동으로 재렌더링된다.
- 로딩 표시는 `LoadingDialog.show/hide(childFragmentManager)` 를 사용한다.
- `CurrentCourseRepository.createCurrentCourse()` 는 백엔드 course_code FK 매핑이 확정되지 않아 현재 빈 값으로 호출 중 — 추후 백엔드와 협의 후 수정 필요.
