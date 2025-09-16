test

### 서비스 코드 준비 방법
프로젝트 루트 디렉토리에서 아래 명령어로 Gradle Task를 실행합니다.
> ```bash
> ./graldew setupRepos
> ```

### 서비스 실행 및 종료 방법
프로젝트 루트 디렉토리에서 아래 명령어로 Gradle Task를 실행합니다.
- 서비스 실행
> ```bash
> ./gradlew composeUp
> ```
- 서비스 상태 확인
> ```bash
> ./gradlew composePs
> ```
- 서비스 로그 확인
> ```bash
> ./gradlew composeLogs
> ```
- 서비스 종료
> ```bash
> ./gradlew composeDown
> ```