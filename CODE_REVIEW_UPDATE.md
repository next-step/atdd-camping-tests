# 코드 리뷰 반영 내역: Gradle 태스크 정의 분리 및 구조 개선

본 문서는 build.gradle.kts 내에 존재하던 커스텀 태스크 정의를 별도 스크립트 파일로 분리하여 유지보수성과 가독성을 높인 작업을 설명합니다. 추가로, 이전에 적용한 cloneKioskRepo 태스크 개선 내용도 함께 정리했습니다.

---

## 1) 태스크 정의 분리 (요구사항 반영)

### 배경
코드 리뷰에서 다음과 같은 제안을 받았습니다.

> "build.gradle.kts 에서 태스크 정의 부분을 별도로 분리할 수 도 있을 것 같네요 :)"

기존에는 Docker Compose 관련 Exec 태스크들과 저장소 클론 태스크가 루트 build.gradle.kts에 직접 정의되어 있어, 빌드 설정(플러그인/의존성/테스트 설정)과 운영 스크립트(도커/레포 관리)가 한 파일에 혼재되어 가독성이 떨어지고 변경 영향 범위를 파악하기 어려웠습니다.

### 변경 사항 요약
- 새 파일 생성: `gradle/custom-tasks.gradle.kts`
  - 이동 대상:
    - `defaultComposeFile` 변수
    - `kioskAppUp`, `kioskAppDown`, `ps`, `logs` 태스크 (Docker Compose 관련)
    - `cloneKioskRepo` 태스크 (레포 클론/업데이트)
- 루트 스크립트 정리: `build.gradle.kts`
  - 위 커스텀 태스크 정의를 제거하고, 아래 한 줄로 외부 스크립트를 적용하도록 변경
  - `apply(from = "gradle/custom-tasks.gradle.kts")`

### 변경 전/후 구조
- 변경 전: 모든 태스크가 `build.gradle.kts`에 직접 정의
- 변경 후:
  - `build.gradle.kts`: 플러그인, 의존성, 테스트 설정 + 외부 스크립트 apply 라인만 유지
  - `gradle/custom-tasks.gradle.kts`: 도커 및 레포 관련 커스텀 태스크 정의 모음

### 사용 방법 (동일)
- Docker 관련
  - 띄우기: `./gradlew kioskAppUp`
  - 내리기: `./gradlew kioskAppDown`
  - 상태: `./gradlew ps`
  - 로그: `./gradlew logs`
- 레포 관리
  - 클론/업데이트: `./gradlew cloneKioskRepo`

태스크의 이름과 동작은 변경되지 않았습니다. 파일 위치만 바뀌었고, 사용 방법은 그대로입니다.

### 기대 효과
- 관심사 분리: 빌드 정의와 운영 스크립트가 분리되어 각 파일의 책임이 명확해집니다.
- 가독성 향상: 루트 스크립트 축소로 핵심 설정 파악이 쉬워지고, 태스크 추가/수정 시 변경 범위가 명확해집니다.
- 확장 용이성: 도커 관련 태스크를 더 추가하거나, 다른 도메인(예: CI, 릴리스) 태스크 파일을 별도로 두는 등 구조적 확장이 간편해집니다.

---

## 2) cloneKioskRepo 태스크 개선 (이전 변경 요약)

### 배경
저장소 디렉토리가 존재하는 경우 태스크가 실행되지 않아 최신 코드로 갱신되지 않는 문제가 있었습니다. 또한 실제 프로젝트 구조와 경로가 일치하도록 정비가 필요했습니다.

### 주요 변경
1. 존재 여부에 따른 동작 분기
   - 존재하면: 해당 디렉토리에서 `git pull`
   - 없으면: 상위 디렉토리에서 `git clone`
2. 경로 일치화: `repo/atdd-camping-kiosk` 사용
3. 동적 명령 설정: `doFirst { ... }`에서 `workingDir`, `commandLine` 지정

### 변경된 태스크 요약 코드
```kotlin
tasks.register<Exec>("cloneKioskRepo") {
    description = "Clone or update https://github.com/next-step/atdd-camping-kiosk under repo/ at project root."
    group = "setup"

    val repoDir = project.file("repo/atdd-camping-kiosk")

    doFirst {
        repoDir.parentFile.mkdirs()
        if (repoDir.exists()) {
            workingDir(repoDir)
            commandLine("git", "pull")
        } else {
            workingDir(repoDir.parentFile)
            commandLine(
                "git", "clone",
                "--branch", "main",
                "https://github.com/next-step/atdd-camping-kiosk"
            )
        }
    }
}
```

### 사용 방법
- macOS/Linux: `./gradlew cloneKioskRepo`
- Windows: `gradlew.bat cloneKioskRepo`

실행 결과
- 디렉토리 없음 → 클론 수행
- 디렉토리 존재 → `git pull` 수행으로 최신화

### 주의 사항
- 로컬 변경이 있는 경우 `git pull` 시 충돌이 발생할 수 있으니, 해당 디렉토리에서 수동으로 해결 후 재실행하세요.

---

## 변경 파일 목록
- `build.gradle.kts`: 커스텀 태스크 제거 및 `apply(from = "gradle/custom-tasks.gradle.kts")` 추가
- `gradle/custom-tasks.gradle.kts`: 커스텀 태스크 정의를 이 파일로 이동(신규)

## 호환성
- 기존 Gradle 태스크 명칭/행동은 그대로 유지되므로, 기존 사용 스크립트나 CI 파이프라인에 영향이 없습니다.
