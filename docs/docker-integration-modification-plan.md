# Docker 통합 구성 수정 계획

## 1. 목적

React와 Spring Boot를 각각 멀티스테이지로 빌드하고, React 실행 이미지의 Nginx를 단일 진입점으로 사용하는 Docker Compose 구성을 완성한다.

이 문서는 현재 구성 검증 결과를 다음 코드 수정 단계에서 바로 실행할 수 있도록 작업 순서, 변경 대상, 검증 기준으로 정리한다.

## 2. 변경 범위 원칙

- `src/**`는 절대 수정하지 않는다.
- Spring Java 코드와 `src/main/resources/**` 설정은 수정하지 않는다.
- 기존 사용자 변경사항을 보존한다.
- 실제 비밀번호, JWT 키 등 비밀값은 Git에 저장하지 않는다.
- 운영 환경은 현재 `.env.prod`의 설정에 맞춰 외부 MySQL RDS를 사용하는 구성을 기본안으로 한다.
- 로컬 MySQL 컨테이너가 필요하면 운영 구성과 섞지 않고 별도 Compose override로 분리한다.

## 3. 목표 구성

```text
브라우저
  -> Nginx (:80 또는 :443)
       -> React 정적 파일
       -> /api/*     -> Spring Boot backend:8080/*
       -> /public/*  -> Spring Boot backend:8080/public/*
       -> /ws-chat   -> Spring Boot WebSocket

Spring Boot
  -> 외부 MySQL RDS
  -> uploads named volume
```

Nginx가 외부에 노출되는 유일한 서비스이며 Spring Boot는 Compose 내부 네트워크에만 노출한다.

## 4. 수정 대상 파일

### 현재 Spring 저장소

- `Dockerfile`
- `.dockerignore`
- `docker-compose.yaml`
- `.gitignore`
- `.env.example` 신규 생성
- `.env.prod`는 로컬에서만 유지하고 Git 추적 해제

### React 저장소 (`../community-ktb`)

- `Dockerfile`
- `.dockerignore`
- `nginx.conf`

React 저장소는 현재 작업공간의 쓰기 허용 범위 밖에 있으므로, 다음 수정 단계 전에 해당 폴더에 대한 쓰기 권한을 확보하거나 별도 승인을 받아야 한다.

## 5. 단계별 수정 계획

### 5.1 비밀정보 관리 정리

1. Git 인덱스에서 `.env.prod` 추적을 해제한다.
2. `.gitignore`의 `.env`, `.env.*` 제외 규칙을 유지한다.
3. 값 없이 변수 이름과 안전한 예시만 담은 `.env.example`을 추가한다.
4. `.env.prod`가 과거 원격 저장소에 올라간 적이 있는지 확인한다.
5. 원격 노출 이력이 있으면 DB 비밀번호와 JWT secret을 교체한다.

예정 환경변수 목록:

```dotenv
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://<host>:3306/<database>
DB_USERNAME=
DB_PASSWORD=
DB_POOL_MAX_SIZE=10
DB_POOL_MIN_IDLE=2
JWT_SECRET=
JWT_ACCESS_TOKEN_EXPIRATION=180
JWT_REFRESH_TOKEN_EXPIRATION=12096000
UPLOAD_PATH=/var/lib/hobbyloop/uploads
CORS_ALLOWED_ORIGINS=https://example.com
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0
VITE_API_BASE_URL=/api
```

필수 수정값은 `SERVER_ADDRESS=0.0.0.0`이다. `127.0.0.1`을 사용하면 다른 컨테이너의 Nginx가 Spring Boot에 접속할 수 없다.

### 5.2 Spring Dockerfile 보완

현재 JDK 빌더와 JRE 런타임을 나눈 멀티스테이지 구조는 유지한다.

수정 항목:

1. 의존성 다운로드 단계의 `|| true`를 제거해 실제 오류가 숨겨지지 않도록 한다.
2. Gradle wrapper와 빌드 설정을 먼저 복사해 레이어 캐시를 유지한다.
3. `bootJar` 결과를 명확한 경로로 복사한다.
4. 런타임은 기존 비루트 `spring` 사용자로 실행한다.
5. 업로드 디렉터리를 이미지에서 미리 만들고 `spring` 사용자에게 쓰기 권한을 부여한다.
6. JVM이 컨테이너 종료 신호를 정상적으로 받도록 exec 형식 `ENTRYPOINT`를 유지한다.
7. 필요하면 임시 파일 경로와 JVM 메모리 옵션을 환경변수로 주입할 수 있게 한다.

완료 기준:

- 이미지에 JDK와 Gradle 캐시가 포함되지 않는다.
- 컨테이너 프로세스가 root가 아니다.
- 생성된 실행 JAR가 정상적으로 시작된다.
- 업로드 볼륨에 파일을 작성할 수 있다.

### 5.3 React Dockerfile 보완

현재 Node 빌더와 Nginx 런타임을 나눈 멀티스테이지 구조는 유지한다.

수정 항목:

1. `package-lock.json` 기반 `npm ci`를 유지한다.
2. `VITE_API_BASE_URL`의 기본값과 빌드 인자를 `/api`로 통일한다.
3. 빌드 결과인 `dist`만 Nginx 이미지에 복사한다.
4. Nginx 설정 검증 단계를 추가하거나 실행 전 `nginx -t`로 검증한다.
5. `.dockerignore`에서 모든 로컬 환경변수 파일과 테스트 산출물을 제외한다.

완료 기준:

- 최종 이미지에 `node_modules`, 소스 코드, Node 런타임이 포함되지 않는다.
- React Router 경로를 새로고침해도 `index.html`이 반환된다.
- 브라우저의 API 요청 URL이 동일 출처의 `/api`를 사용한다.

### 5.4 Nginx 리버스 프록시 보완

다음 라우팅을 명시한다.

| 요청 경로 | 전달 대상 | 비고 |
|---|---|---|
| `/api/*` | `http://backend:8080/*` | `/api` 접두사를 제거해 현재 Spring 경로와 일치시킴 |
| `/public/*` | `http://backend:8080/public/*` | 업로드 이미지 제공 |
| `/ws-chat` | `http://backend:8080/ws-chat` | WebSocket Upgrade 헤더 필요 |
| 그 외 | React 정적 파일 | 파일이 없으면 `/index.html` |

추가 항목:

1. `Host`, `X-Real-IP`, `X-Forwarded-For`, `X-Forwarded-Proto` 헤더를 전달한다.
2. WebSocket에 `proxy_http_version 1.1`, `Upgrade`, `Connection` 헤더를 설정한다.
3. 업로드 응답 크기와 요청 크기를 고려해 `client_max_body_size`를 Spring의 100MB 제한과 맞춘다.
4. 해시가 포함된 정적 자산에만 장기 캐시를 적용하고 `index.html`은 캐시하지 않는다.
5. 기본적인 보안 응답 헤더를 추가한다.

`src`를 수정하지 않는 제약으로 인해 WebSocket의 운영 Origin은 주의가 필요하다. 현재 Spring 설정은 localhost Origin만 허용한다. 다음 수정 단계에서는 Nginx에서 허용된 외부 Origin만 통과시키고, Spring으로 전달할 WebSocket Origin 헤더를 현재 허용값에 맞추는 인프라 우회안을 적용할지 검토한다. 이 우회는 허용 도메인을 Nginx에서 엄격히 제한하는 경우에만 사용한다.

REST API는 브라우저가 Nginx의 동일 출처 `/api`를 사용하므로 운영 CORS에 의존하지 않는다.

### 5.5 Docker Compose 재구성

Compose 파일은 현재 Spring 저장소에 유지하며 다음 빌드 경로를 사용한다.

```yaml
backend:
  build:
    context: .

frontend:
  build:
    context: ../community-ktb
```

수정 항목:

1. 존재하지 않는 `./backend`, `./frontend` 경로를 실제 경로로 변경한다.
2. 운영 기본안에서는 사용되지 않는 PostgreSQL `database` 서비스를 제거한다.
3. 백엔드에 `.env.prod`를 `env_file`로 전달한다.
4. 프론트 빌드 인자로 `VITE_API_BASE_URL=/api`를 전달한다.
5. 백엔드는 `expose: 8080`만 사용하고 호스트 포트는 열지 않는다.
6. 프론트/Nginx만 호스트의 `80:80`을 공개한다.
7. 업로드 파일용 `uploads-data` named volume을 추가한다.
8. 백엔드와 프론트를 전용 bridge 네트워크에 연결한다.
9. 재시작 정책과 서비스 상태 확인 방법을 명시한다.
10. 가능한 경우 백엔드 healthcheck 이후 프론트가 준비되도록 의존 조건을 설정한다.

외부 RDS 연결정보는 이미지 빌드 인자에 넣지 않고 런타임 환경변수로만 전달한다.

### 5.6 선택 작업: 로컬 MySQL Compose 분리

로컬 DB까지 Compose로 띄워야 한다면 PostgreSQL을 유지하지 않고 별도 `docker-compose.local.yaml`에 MySQL 서비스를 추가한다.

- 이미지: 프로젝트와 호환되는 MySQL 8 계열
- Compose 서비스명: `database`
- 컨테이너 내부 DB URL: `jdbc:mysql://database:3306/<database>`
- MySQL healthcheck 성공 후 backend 시작
- DB 데이터용 named volume 사용
- 운영 RDS 구성과 로컬 DB 구성을 한 파일에서 혼용하지 않음

## 6. 검증 계획

### 6.1 정적 검증

- `docker compose --env-file .env.prod config --quiet`
- 환경변수 미설정 경고가 없는지 확인
- 빌드 context와 Dockerfile 경로가 모두 존재하는지 확인
- Nginx 설정 문법 검사
- `.env.prod`가 Git 추적 대상이 아닌지 확인
- Docker build context에 비밀정보가 포함되지 않는지 확인

### 6.2 이미지 빌드 검증

- Spring `bootJar` 빌드 성공
- Spring Docker 이미지 빌드 성공
- React `npm ci`와 production build 성공
- React/Nginx Docker 이미지 빌드 성공
- 최종 이미지에서 비루트 실행 여부와 불필요한 빌드 도구 부재 확인

### 6.3 통합 실행 검증

1. `docker compose --env-file .env.prod up --build`로 전체 실행
2. Nginx 첫 화면 응답 확인
3. React Router 하위 경로 직접 접근 및 새로고침 확인
4. `/api`를 통한 로그인, 게시글 조회 등 API 요청 확인
5. `/public` 업로드 이미지 응답 확인
6. WebSocket 연결과 메시지 송수신 확인
7. Nginx에서 backend 서비스명 해석 확인
8. backend 재시작 후 업로드 파일 유지 확인
9. backend 포트가 호스트에 직접 공개되지 않았는지 확인
10. 컨테이너 종료 시 Spring graceful shutdown 확인

## 7. 완료 조건

- Spring과 React가 모두 멀티스테이지 Dockerfile로 빌드된다.
- `docker compose --env-file .env.prod up --build` 한 번으로 통합 실행된다.
- 외부 접근은 Nginx 포트 하나로 제한된다.
- REST API, 업로드 파일, React Router, WebSocket이 의도한 경로로 동작한다.
- Spring은 외부 MySQL RDS에 정상 연결된다.
- 업로드 파일은 컨테이너 재생성 후에도 유지된다.
- 비밀 환경변수 파일이 Git 또는 이미지에 포함되지 않는다.
- `src/**`에는 변경사항이 없다.

## 8. 다음 수정 단계의 권장 순서

1. `.env.prod` Git 추적 해제 및 `.env.example` 추가
2. Spring Dockerfile과 `.dockerignore` 수정
3. React Dockerfile과 `.dockerignore` 수정
4. Nginx API·업로드·WebSocket 프록시 수정
5. Compose를 외부 RDS 기준으로 재구성
6. 정적 검증 및 두 이미지 개별 빌드
7. Compose 통합 실행과 엔드포인트 검증
8. `src/**` 무변경 여부와 Git diff 최종 확인
