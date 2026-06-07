# 원격 통합 테스트 가이드

## 목적

팀원들이 서로 다른 장소에 있어도 같은 백엔드 서버와 같은 MySQL DB를 기준으로 상품 등록, 상품 목록 조회, 상품 상세 조회, 채팅 기능을 테스트하기 위한 절차를 정리한다.

원격 테스트의 핵심은 모든 프론트엔드가 같은 백엔드 주소를 바라보고, 그 백엔드가 하나의 MySQL DB에 연결되는 것이다.

## 테스트 구조

- 팀장 또는 테스트 담당자 컴퓨터에서 MySQL 실행
- 팀장 또는 테스트 담당자 컴퓨터에서 Spring Boot 백엔드 실행
- ngrok 등 터널링 도구로 `localhost:8080`을 외부 주소로 공개
- 팀원들은 프론트엔드의 `API_BASE_URL`을 ngrok 주소로 맞춰 접속
- 상품, 채팅방, 메시지 데이터는 백엔드가 연결한 하나의 MySQL DB에 저장

## 백엔드 실행 준비

PowerShell에서 백엔드 폴더로 이동한다.

```powershell
cd "C:\Users\akrma\OneDrive\Documents\5.20.test\backend"
```

Java 21 경로를 설정한다.

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

메일 환경변수를 설정한다.

```powershell
$env:MAIL_USERNAME="메일계정"
$env:MAIL_PASSWORD="앱비밀번호"
```

기본 로컬 DB를 그대로 사용할 경우 추가 DB 설정은 필요 없다.

다른 DB를 연결해야 한다면 다음 환경변수를 설정한다.

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/sejong_market?serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="1234"
```

외부 접속을 받을 수 있도록 서버 주소를 설정한다.

```powershell
$env:SERVER_ADDRESS="0.0.0.0"
```

백엔드를 실행한다.

```powershell
.\gradlew.bat bootRun
```

## ngrok으로 백엔드 공개

백엔드가 실행 중인 상태에서 새 PowerShell 창을 열고 다음 명령을 실행한다.

```powershell
ngrok http 8080
```

ngrok이 생성한 HTTPS 주소를 확인한다.

예시:

```text
https://abcd-1234.ngrok-free.app
```

이 주소가 원격 테스트용 백엔드 주소가 된다.

## 프론트엔드 API 주소 변경

프론트엔드는 `frontend/common/app-utils.js`의 `API_BASE_URL`을 기준으로 백엔드 API를 호출한다.

원격 테스트 주소는 두 가지 방법으로 설정할 수 있다.

### 방법 1. URL 파라미터로 설정

상품 목록 화면을 열 때 `apiBaseUrl` 파라미터를 붙인다.

```text
product-list.html?apiBaseUrl=https://abcd-1234.ngrok-free.app
```

이 값은 `localStorage`에 저장되므로 이후 다른 화면에서도 같은 백엔드 주소를 사용한다.

### 방법 2. 브라우저 콘솔에서 설정

브라우저 개발자 도구 Console에서 다음 명령을 실행한다.

```js
localStorage.setItem("SEJONG_MARKET_API_BASE_URL", "https://abcd-1234.ngrok-free.app");
```

다시 로컬 백엔드로 테스트하려면 다음 명령을 실행한다.

```js
localStorage.removeItem("SEJONG_MARKET_API_BASE_URL");
```

## 원격 상품 등록/조회 테스트

1. 팀원 A가 원격 백엔드 주소가 설정된 프론트엔드를 연다.
2. 팀원 A가 회원가입 또는 로그인한다.
3. 팀원 A가 상품을 등록한다.
4. 상품 데이터가 테스트 담당자 컴퓨터의 MySQL DB에 저장되는지 확인한다.
5. 팀원 B가 다른 컴퓨터에서 같은 원격 백엔드 주소를 설정한다.
6. 팀원 B가 상품 목록 화면을 연다.
7. 팀원 A가 등록한 상품이 팀원 B의 상품 목록에 보이는지 확인한다.
8. 상품 카드를 클릭해 상품 상세 화면으로 이동되는지 확인한다.

## 원격 채팅 테스트

1. 판매자 계정으로 상품을 등록한다.
2. 구매자 계정으로 같은 상품 상세 화면에 들어간다.
3. 구매자가 `판매자와 채팅하기` 버튼을 누른다.
4. 채팅방이 생성되거나 기존 채팅방으로 이동되는지 확인한다.
5. 구매자가 메시지를 보낸다.
6. 판매자도 같은 채팅방에 접속한다.
7. 판매자 화면에 구매자 메시지가 표시되는지 확인한다.
8. 판매자가 답장을 보낸다.
9. 구매자 화면에 판매자 메시지가 표시되는지 확인한다.

현재 채팅은 WebSocket이 아니라 polling 방식이므로, 메시지는 약 2~3초 뒤 갱신될 수 있다.

## 주의사항

- 모든 팀원이 같은 ngrok 백엔드 주소를 사용해야 한다.
- 백엔드가 연결한 MySQL DB가 하나여야 상품과 채팅 데이터가 공유된다.
- ngrok 주소가 바뀌면 프론트엔드의 `SEJONG_MARKET_API_BASE_URL`도 다시 설정해야 한다.
- 같은 브라우저 일반 탭 여러 개는 `localStorage`를 공유하므로 서로 다른 사용자 테스트는 일반 창과 시크릿 창, 또는 Chrome과 Edge를 나눠 사용하는 것이 좋다.
- ngrok 무료 주소는 실행할 때마다 바뀔 수 있다.
