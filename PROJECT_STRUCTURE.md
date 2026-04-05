# MJCN 프로젝트 구조

> Myong Ji Campus Navigator — 명지대학교 캠퍼스 네비게이터  
> Android MVVM | minSdk 26 | targetSdk 36 | AGP 9.1.0 | Kotlin 2.0.21

---

## 디렉토리 구조

```
MJCN/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/ultimatejw/mjcn/
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── MjcnDatabase.kt         # Room 싱글턴 DB
│       │   │   │   ├── Converters.kt           # List<String> ↔ String 변환
│       │   │   │   └── dao/
│       │   │   │       ├── UserDao.kt
│       │   │   │       ├── NoticeDao.kt
│       │   │   │       └── ChatDao.kt
│       │   │   ├── model/
│       │   │   │   ├── User.kt                 # Room Entity (프로필)
│       │   │   │   ├── Notice.kt               # Room Entity + NoticeCategory enum
│       │   │   │   ├── ChatSession.kt          # Room Entity + ChatMessage + ChatCategory enum
│       │   │   │   └── Theme.kt                # data class (비 Room)
│       │   │   └── repository/
│       │   │       ├── UserRepository.kt       # DataStore(로그인) + Room(유저)
│       │   │       ├── NoticeRepository.kt
│       │   │       └── ChatRepository.kt
│       │   ├── ui/
│       │   │   ├── splash/
│       │   │   │   └── SplashActivity.kt       # 1.5초 딜레이 → 로그인 상태 분기
│       │   │   ├── auth/
│       │   │   │   ├── AuthActivity.kt         # nav_auth 호스트
│       │   │   │   ├── LoginFragment.kt
│       │   │   │   ├── LoginViewModel.kt
│       │   │   │   ├── LoginViewModelFactory.kt
│       │   │   │   └── signup/
│       │   │   │       ├── SignUpViewModel.kt  # activityViewModels (3단계 공유)
│       │   │   │       ├── SignUpStep1Fragment.kt  # 이름, 학년, 학기, 졸업연도
│       │   │   │       ├── SignUpStep2Fragment.kt  # 이메일, 비밀번호
│       │   │   │       └── SignUpStep3Fragment.kt  # 관심사 ChipGroup
│       │   │   ├── onboarding/
│       │   │   │   └── OnboardingFragment.kt   # 가입 후 최초 1회 표시
│       │   │   ├── main/
│       │   │   │   ├── MainActivity.kt         # nav_main 호스트 + BottomNav
│       │   │   │   ├── home/
│       │   │   │   │   ├── HomeFragment.kt
│       │   │   │   │   ├── HomeViewModel.kt
│       │   │   │   │   └── HomeViewModelFactory.kt
│       │   │   │   ├── theme/
│       │   │   │   │   ├── ThemeFragment.kt
│       │   │   │   │   ├── ThemeViewModel.kt
│       │   │   │   │   ├── ThemeAdapter.kt
│       │   │   │   │   └── ThemeDetailFragment.kt
│       │   │   │   ├── chat/
│       │   │   │   │   ├── ChatFragment.kt
│       │   │   │   │   ├── ChatViewModel.kt
│       │   │   │   │   ├── ChatViewModelFactory.kt
│       │   │   │   │   └── ChatDetailFragment.kt
│       │   │   │   ├── notice/
│       │   │   │   │   ├── NoticeFragment.kt
│       │   │   │   │   ├── NoticeViewModel.kt
│       │   │   │   │   ├── NoticeViewModelFactory.kt
│       │   │   │   │   └── NoticeDetailFragment.kt
│       │   │   │   └── info/
│       │   │   │       └── InfoFragment.kt
│       │   │   └── utils/
│       │   │       └── Extensions.kt           # visible/gone, showToast, isValidEmail 등
│       └── res/
│           ├── layout/
│           │   ├── activity_splash.xml
│           │   ├── activity_auth.xml
│           │   ├── activity_main.xml
│           │   ├── fragment_login.xml
│           │   ├── fragment_signup_step1.xml
│           │   ├── fragment_signup_step2.xml
│           │   ├── fragment_signup_step3.xml
│           │   ├── fragment_onboarding.xml
│           │   ├── fragment_home.xml
│           │   ├── fragment_theme.xml
│           │   ├── fragment_theme_detail.xml
│           │   ├── fragment_chat.xml
│           │   ├── fragment_chat_detail.xml
│           │   ├── fragment_notice.xml
│           │   ├── fragment_notice_detail.xml
│           │   ├── fragment_info.xml
│           │   ├── item_theme.xml
│           │   └── item_onboarding_feature.xml
│           ├── navigation/
│           │   ├── nav_auth.xml                # Login → SignUp 1~3 → Onboarding
│           │   └── nav_main.xml                # Home, Theme, Chat, Notice, Info
│           ├── drawable/
│           │   ├── bg_btn_primary.xml
│           │   ├── bg_btn_kakao.xml
│           │   ├── bg_input_field.xml
│           │   ├── bg_card_rounded.xml
│           │   ├── bg_category_chip.xml
│           │   ├── bg_chip_interest.xml
│           │   ├── bg_splash.xml
│           │   ├── ic_nav_home/chat/notice/theme/info.xml
│           │   ├── ic_visibility.xml
│           │   ├── ic_visibility_off.xml
│           │   ├── ic_send.xml
│           │   └── ic_arrow_right.xml
│           ├── menu/
│           │   └── bottom_nav_menu.xml         # 하단 탭 5개
│           ├── color/
│           │   └── nav_selector.xml
│           └── values/
│               ├── colors.xml
│               ├── strings.xml
│               └── themes.xml
├── gradle/
│   └── libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 화면 흐름

```
SplashActivity
    ├── isLoggedIn = true  →  MainActivity
    └── isLoggedIn = false →  AuthActivity
                                ├── LoginFragment
                                └── SignUpStep1 → Step2 → Step3 → OnboardingFragment
                                                                        └── MainActivity

MainActivity (BottomNavigationView)
    ├── 홈          HomeFragment
    ├── 주제        ThemeFragment → ThemeDetailFragment
    ├── AI채팅      ChatFragment  → ChatDetailFragment
    ├── 공지        NoticeFragment → NoticeDetailFragment
    └── 정보        InfoFragment
```

---

## 주요 라이브러리

| 라이브러리 | 버전 | 용도 |
|-----------|------|------|
| AGP | 9.1.0 | 빌드 (내장 Kotlin) |
| Kotlin | 2.0.21 | 언어 |
| KSP | 2.0.21-1.0.28 | 어노테이션 처리 |
| Room | 2.7.0 | 로컬 DB |
| DataStore Preferences | 1.1.1 | 로그인 상태 저장 |
| Navigation Component | 2.8.9 | 화면 전환 |
| Lifecycle (ViewModel/LiveData) | 2.8.7 | MVVM |
| Retrofit | 2.11.0 | REST API |
| OkHttp Logging Interceptor | 4.12.0 | 네트워크 디버깅 |
| Glide | 4.16.0 | 이미지 로딩 |
| Coroutines | 1.9.0 | 비동기 처리 |
| Material Components | 1.12.0 | UI |

---

## 아키텍처 패턴

```
View (Fragment/Activity)
    ↕  observe LiveData / call methods
ViewModel
    ↕  suspend functions / Flow
Repository
    ↕                    ↕
Room (로컬 DB)     DataStore (설정/세션)
                         ↕  (추후)
                    Retrofit (서버 API)
```

---

## 특이사항

- AGP 9.1.0은 Kotlin을 내장 → `kotlin-android` 플러그인 별도 추가 불필요
- KSP + AGP 9.x 호환을 위해 `gradle.properties`에 `android.disallowKotlinSourceSets=false` 설정
- Single Activity 구조: `SplashActivity` → `AuthActivity` → `MainActivity` 각각 독립
- Pretendard 폰트: TTF 파일 미포함 상태 (추후 `res/font/`에 추가 필요)
