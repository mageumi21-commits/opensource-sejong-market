# 세종마켓

세종마켓은 세종대학교 학생들을 위한 교내 중고거래 플랫폼입니다. 사용자는 학교 이메일 인증을 통해 회원가입하고, 상품을 등록하거나 조회할 수 있으며, 상품 상세 화면에서 판매자와 구매자 간 채팅을 진행할 수 있습니다.

본 프로젝트는 GitHub Flow 기반 협업 실습을 목표로 진행되었으며, 프론트엔드와 백엔드를 REST API로 연결하고 MySQL 데이터베이스에 사용자, 상품, 이미지 경로, 채팅 데이터를 저장하도록 구현했습니다.

## 1. 프로젝트 주요 기능

### 사용자 기능

- 학교 이메일 인증번호 발송
- 이메일 인증번호 확인
- 회원가입
- 로그인
- 아이디 찾기
- 비밀번호 재설정 인증
- 마이페이지 내 사용자 정보 조회
- 닉네임 수정
- 비밀번호 수정
- 로그인 상태에 따른 화면 우측 상단 UI 표시

### 상품 기능

- 상품 등록
- 상품 이미지 최대 10장 등록
- 상품 목록 조회
- 최신 상품 조회
- 추천 상품 조회
- 상품 상세 조회
- 상품 수정
- 상품 삭제
- 상품 판매완료 처리
- 상품 판매중/판매완료 상태 표시
- 찜하기 및 찜 해제
- 마이페이지 내 내가 등록한 상품 조회

### 채팅 기능

- 상품 상세 화면에서 판매자와 채팅방 생성
- 기존 채팅방이 있으면 기존 채팅방으로 이동
- 내 채팅방 목록 조회
- 채팅방 상세 조회
- 메시지 목록 조회
- 메시지 전송
- 읽지 않은 채팅 표시

### 원격 테스트 지원

- 프론트엔드 API 주소 공통 관리
- ngrok 주소를 통한 원격 백엔드 테스트 지원
- 공용 MySQL DB를 기준으로 여러 사용자의 상품과 채팅 데이터 공유 가능
- 원격 이미지 조회 보완

## 2. 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Frontend | HTML, CSS, JavaScript |
| Backend | Java 21, Spring Boot |
| Database | MySQL |
| ORM | Spring Data JPA |
| Mail | Spring Mail, Gmail SMTP |
| Test | JUnit 5, Mockito, Gradle Test |
| Collaboration | Git, GitHub, Pull Request |
| Remote Test | ngrok |

## 3. 프로젝트 구조

```text
5.20.test
├─ backend
│  ├─ src/main/java/com/market/backend
│  │  ├─ chat
│  │  ├─ common
│  │  ├─ config
│  │  ├─ product
│  │  └─ user
│  ├─ src/main/resources
│  │  └─ application.properties
│  └─ build.gradle
│
├─ frontend
│  ├─ chat_ui
│  ├─ common
│  ├─ login_mypage
│  ├─ main_ui
│  ├─ product_create_ui
│  └─ product_detail_ui
│
├─ docs
│  ├─ deployment-config.md
│  ├─ postman-test-checklist.md
│  ├─ remote-integration-test.md
│  └─ site-route-map.md
│
└─ README.md
```

## 4. 실행 전 준비 사항

아래 프로그램이 설치되어 있어야 합니다.

- Java 21
- MySQL 8.x
- Git
- 웹 브라우저
- 선택 사항: Postman
- 선택 사항: ngrok

## 5. 데이터베이스 준비

MySQL에서 프로젝트용 데이터베이스를 생성합니다.

```sql
CREATE DATABASE sejong_market
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

기본 로컬 실행 기준 DB 정보는 다음과 같습니다.

| 항목 | 기본값 |
|---|---|
| DB 이름 | sejong_market |
| DB 사용자 | root |
| DB 비밀번호 | 1234 |
| DB 포트 | 3306 |

DB 계정이나 비밀번호가 다르면 백엔드 실행 전 환경변수를 수정해야 합니다.

## 6. 백엔드 실행 방법

PowerShell에서 backend 폴더로 이동합니다.

```powershell
cd "프로젝트경로\backend"
```

Java 21 경로를 설정합니다.

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

DB 연결 정보를 설정합니다.

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/sejong_market?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="1234"
```

이메일 인증 기능을 사용하려면 Gmail 계정과 앱 비밀번호를 설정합니다.

```powershell
$env:MAIL_USERNAME="example@gmail.com"
$env:MAIL_PASSWORD="gmail-app-password"
```

서버를 실행합니다.

```powershell
.\gradlew.bat bootRun
```

정상 실행 후 아래 주소에서 서버 상태를 확인할 수 있습니다.

```text
http://localhost:8080/health
```

정상 응답 예시는 다음과 같습니다.

```json
{
  "status": "ok"
}
```

## 7. 프론트엔드 실행 방법

프론트엔드는 정적 HTML 파일로 구성되어 있으므로 별도 빌드 과정 없이 브라우저에서 실행할 수 있습니다.

메인 화면 파일:

```text
frontend/main_ui/index.html
```

브라우저에서 위 파일을 열면 세종마켓 메인 화면을 확인할 수 있습니다.

주요 화면 파일은 다음과 같습니다.

| 화면 | 파일 |
|---|---|
| 메인 화면 | frontend/main_ui/index.html |
| 상품 목록 화면 | frontend/main_ui/product-list.html |
| 상품 등록/수정 화면 | frontend/product_create_ui/product-create-ui.html |
| 상품 상세 화면 | frontend/product_detail_ui/product-detail.html |
| 로그인 화면 | frontend/login_mypage/login.html |
| 회원가입 화면 | frontend/login_mypage/signup.html |
| 아이디 찾기 화면 | frontend/login_mypage/find-id.html |
| 비밀번호 찾기 화면 | frontend/login_mypage/find-pw.html |
| 마이페이지 | frontend/login_mypage/mypage.html |
| 채팅 화면 | frontend/chat_ui/chat.html |

## 8. 원격 테스트 방법

팀원들이 서로 다른 컴퓨터에서 같은 백엔드와 같은 DB를 기준으로 테스트하려면 한 명의 컴퓨터에서 백엔드 서버와 MySQL을 실행하고, ngrok으로 백엔드 주소를 외부에 공개합니다.

### 8.1 백엔드 실행

서버 역할을 하는 컴퓨터에서 MySQL과 Spring Boot 백엔드를 실행합니다.

```powershell
cd "프로젝트경로\backend"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:DB_URL="jdbc:mysql://localhost:3306/sejong_market?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="1234"
$env:MAIL_USERNAME="example@gmail.com"
$env:MAIL_PASSWORD="gmail-app-password"
.\gradlew.bat bootRun
```

### 8.2 ngrok 실행

새 PowerShell 창에서 다음 명령어를 실행합니다.

```powershell
ngrok http 8080
```

ngrok이 표시하는 HTTPS 주소를 팀원에게 공유합니다.

예시:

```text
https://example.ngrok-free.dev
```

### 8.3 프론트엔드 API 주소 설정

각 팀원은 프론트엔드 HTML 파일을 연 뒤, 브라우저 개발자 도구 Console에서 아래 명령어를 실행합니다.

```js
localStorage.setItem("SEJONG_MARKET_API_BASE_URL", "https://example.ngrok-free.dev");
location.reload();
```

이후 로그인, 회원가입, 상품 등록, 상품 목록, 마이페이지, 채팅 기능이 모두 같은 백엔드 서버와 같은 MySQL DB를 기준으로 동작합니다.

원격 테스트 주소를 해제하려면 아래 명령어를 실행합니다.

```js
localStorage.removeItem("SEJONG_MARKET_API_BASE_URL");
location.reload();
```

## 9. 주요 API 목록

### 사용자 API

| Method | URL | 설명 |
|---|---|---|
| POST | /users/email/send | 학교 이메일 인증번호 발송 |
| POST | /users/email/verify | 이메일 인증번호 확인 |
| POST | /users/signup | 회원가입 |
| POST | /users/login | 로그인 |
| POST | /users/find-id | 아이디 찾기 |
| POST | /users/password/email/send | 비밀번호 재설정 인증번호 발송 |
| POST | /users/password/verify | 비밀번호 재설정 인증번호 확인 |
| GET | /users/me | 마이페이지 사용자 정보 조회 |
| PATCH | /users/me | 닉네임 수정 |
| PATCH | /users/me/password | 비밀번호 수정 |

### 상품 API

| Method | URL | 설명 |
|---|---|---|
| GET | /api/products | 전체 상품 목록 조회 |
| GET | /api/products/latest | 최신 상품 조회 |
| GET | /api/products/recommendations | 추천 상품 조회 |
| GET | /api/products/{productId} | 상품 상세 조회 |
| POST | /api/products | 상품 등록 |
| PATCH | /api/products/{productId} | 상품 수정 |
| DELETE | /api/products/{productId} | 상품 삭제 |
| PATCH | /api/products/{productId}/sold-out | 판매완료 처리 |
| POST | /api/products/{productId}/likes | 상품 찜하기 |
| DELETE | /api/products/{productId}/likes | 상품 찜 해제 |
| GET | /api/products/{productId}/likes | 상품 찜 여부 조회 |

### 채팅 API

| Method | URL | 설명 |
|---|---|---|
| GET | /api/chatrooms | 내 채팅방 목록 조회 |
| GET | /api/chatrooms/unread | 읽지 않은 채팅 여부 조회 |
| POST | /api/chatrooms | 채팅방 생성 또는 기존 채팅방 조회 |
| GET | /api/chatrooms/{chatRoomId} | 채팅방 상세 조회 |
| DELETE | /api/chatrooms/{chatRoomId} | 채팅방 나가기 |
| GET | /api/chatrooms/{chatRoomId}/messages | 메시지 목록 조회 |
| POST | /api/chatrooms/{chatRoomId}/messages | 메시지 전송 |

## 10. 테스트 방법

백엔드 자동 테스트는 Gradle로 실행합니다.

```powershell
cd "프로젝트경로\backend"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat test
```

프론트엔드와 백엔드 연동 테스트는 다음 순서로 진행합니다.

1. MySQL 실행
2. Spring Boot 백엔드 실행
3. `/health` 응답 확인
4. 프론트엔드 메인 화면 열기
5. 회원가입 진행
6. 로그인 진행
7. 상품 등록
8. 상품 목록에서 등록한 상품 확인
9. 상품 상세 화면 진입
10. 상품 수정, 삭제, 판매완료 처리 확인
11. 다른 계정으로 로그인
12. 판매자와 채팅하기 클릭
13. 채팅방 목록과 메시지 송수신 확인
14. 마이페이지에서 내 정보와 등록 상품 확인

Postman 테스트 항목은 `docs/postman-test-checklist.md` 파일을 참고합니다.

## 11. 사용 시 주의사항

- 현재 로그인 상태는 JWT가 아니라 `localStorage` 기반으로 관리됩니다.
- 따라서 실제 서비스 수준의 보안을 위해서는 JWT 또는 세션 기반 인증을 추가로 도입해야 합니다.
- 상품 이미지는 백엔드 서버의 `uploads/products` 폴더에 저장됩니다.
- 원격 테스트 시 모든 사용자가 같은 백엔드 주소를 사용해야 같은 DB 데이터를 공유할 수 있습니다.
- ngrok 무료 주소는 실행할 때마다 바뀔 수 있으므로, 주소가 바뀌면 프론트엔드의 `SEJONG_MARKET_API_BASE_URL`도 다시 설정해야 합니다.
- 채팅은 WebSocket 방식이 아니라 일정 주기로 메시지를 다시 조회하는 방식입니다.

## 12. 협업 방식

본 프로젝트는 GitHub Flow 전략을 사용하여 협업했습니다.

- `master` 브랜치에서 직접 작업하지 않음
- 기능별 브랜치 생성
- 작업 완료 후 Pull Request 생성
- 최소 1명 이상 리뷰 후 병합
- 충돌 발생 시 PR 작성자가 충돌 해결 후 재리뷰 요청

브랜치와 PR 제목에는 다음 키워드를 사용했습니다.

| 키워드 | 의미 |
|---|---|
| feat | 새로운 기능 추가 |
| fix | 버그 수정 또는 기능 보완 |
| docs | 문서 수정 |
| style | 코드 동작과 무관한 형식 수정 |
| refactor | 기능 변화 없는 구조 개선 |
| test | 테스트 코드 또는 테스트 문서 추가 |
| build | 빌드 설정 또는 외부 라이브러리 관련 변경 |
| chore | 기타 유지보수 작업 |

## 13. 참고 문서

- `docs/remote-integration-test.md`: 원격 통합 테스트 방법
- `docs/deployment-config.md`: 배포 설정 가이드
- `docs/postman-test-checklist.md`: Postman 테스트 체크리스트
- `docs/site-route-map.md`: 화면 경로 정리
