# Postman 수동 테스트 체크리스트

이 문서는 세종마켓 백엔드 API와 프론트엔드 연결 흐름을 수동으로 확인하기 위한 테스트 체크리스트이다.
Pull Request 생성 전 기능별 테스트 결과를 정리하거나, 최종 보고서의 테스트 과정 설명 자료로 활용한다.

## 1. 테스트 전 준비

### 백엔드 서버 실행

- [ ] Java 21이 설정되어 있는지 확인한다.
- [ ] MySQL 서버가 실행 중인지 확인한다.
- [ ] `sejong_market` 데이터베이스가 존재하는지 확인한다.
- [ ] 이메일 인증 테스트가 필요한 경우 SMTP 환경변수를 설정한다.
- [ ] 백엔드 폴더에서 서버를 실행한다.

```powershell
cd "C:\Users\akrma\OneDrive\Documents\5.20.test\backend"
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.11"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:MAIL_USERNAME="GMAIL_ACCOUNT"
$env:MAIL_PASSWORD="GMAIL_APP_PASSWORD"
.\gradlew.bat bootRun
```

### 공통 확인 사항

- [ ] 서버가 `http://localhost:8080`에서 정상 실행되는지 확인한다.
- [ ] Postman에서 요청 URL을 `http://localhost:8080` 기준으로 작성한다.
- [ ] JSON 요청은 `Content-Type: application/json` 헤더를 사용한다.
- [ ] 상품 등록 요청은 `multipart/form-data` 형식을 사용한다.
- [ ] 테스트 후 MySQL에서 실제 데이터가 저장/수정/삭제되었는지 확인한다.

## 2. 회원 기능 테스트

### 이메일 인증번호 발송

```http
POST /users/email/send
```

요청 예시:

```json
{
  "email": "student@sju.ac.kr"
}
```

확인 항목:

- [ ] 정상 이메일 입력 시 인증번호 발송 성공 응답이 반환된다.
- [ ] 세종대 이메일이 아닌 경우 오류가 반환된다.
- [ ] SMTP 환경변수가 없거나 잘못된 경우 메일 발송 오류가 반환된다.

### 이메일 인증번호 확인

```http
POST /users/email/verify
```

요청 예시:

```json
{
  "email": "student@sju.ac.kr",
  "code": "123456"
}
```

확인 항목:

- [ ] 올바른 인증번호 입력 시 이메일 인증 성공 응답이 반환된다.
- [ ] 잘못된 인증번호 입력 시 오류가 반환된다.
- [ ] 만료된 인증번호 입력 시 오류가 반환된다.

### 회원가입

```http
POST /users/signup
```

요청 예시:

```json
{
  "email": "student@sju.ac.kr",
  "password": "test1234!",
  "nickname": "테스트유저",
  "studentId": "23011234"
}
```

확인 항목:

- [ ] 이메일 인증 완료 후 회원가입 성공 응답이 반환된다.
- [ ] 인증되지 않은 이메일로 회원가입 시 오류가 반환된다.
- [ ] MySQL `user` 테이블에 사용자 정보가 저장된다.

### 로그인

```http
POST /users/login
```

요청 예시:

```json
{
  "email": "student@sju.ac.kr",
  "password": "test1234!"
}
```

확인 항목:

- [ ] 올바른 이메일과 비밀번호 입력 시 로그인 성공 응답이 반환된다.
- [ ] 잘못된 비밀번호 입력 시 오류가 반환된다.
- [ ] 존재하지 않는 이메일 입력 시 오류가 반환된다.

### 아이디 찾기

```http
POST /users/find-id
```

이름과 학번으로 찾기:

```json
{
  "nickname": "테스트유저",
  "studentId": "23011234"
}
```

이름과 이메일로 찾기:

```json
{
  "nickname": "테스트유저",
  "email": "student@sju.ac.kr"
}
```

확인 항목:

- [ ] 일치하는 회원 정보가 있으면 이메일 또는 마스킹된 이메일이 반환된다.
- [ ] 일치하는 회원 정보가 없으면 오류가 반환된다.

### 비밀번호 찾기 인증번호 발송

```http
POST /users/password/email/send
```

요청 예시:

```json
{
  "id": "student@sju.ac.kr",
  "nickname": "테스트유저",
  "email": "student@sju.ac.kr"
}
```

확인 항목:

- [ ] 아이디, 이름, 이메일이 일치하면 인증번호 발송 성공 응답이 반환된다.
- [ ] 아이디와 이메일이 다르면 오류가 반환된다.
- [ ] 존재하지 않는 사용자 정보 입력 시 오류가 반환된다.

### 비밀번호 찾기 인증번호 확인

```http
POST /users/password/verify
```

요청 예시:

```json
{
  "id": "student@sju.ac.kr",
  "nickname": "테스트유저",
  "email": "student@sju.ac.kr",
  "code": "123456"
}
```

확인 항목:

- [ ] 올바른 인증번호 입력 시 비밀번호 확인 응답이 반환된다.
- [ ] 잘못된 인증번호 입력 시 오류가 반환된다.

## 3. 상품 기능 테스트

### 상품 등록

```http
POST /api/products
```

요청 형식:

```text
multipart/form-data
```

Form Data 예시:

```text
sellerEmail: student@sju.ac.kr
productName: 전공책 팝니다
category: 도서
price: 12000
description: 깨끗하게 사용한 전공책입니다.
tradeMethod: 직거래
locationNumber: 3
locationName: 학생회관
images: 이미지 파일
```

확인 항목:

- [ ] 필수값 입력 시 상품 등록 성공 응답이 반환된다.
- [ ] 상품 ID가 응답에 포함된다.
- [ ] 이미지 파일이 `uploads/products` 폴더에 저장된다.
- [ ] DB에 상품 정보와 이미지 경로가 저장된다.
- [ ] 이미지가 10장을 초과하면 오류가 반환된다.
- [ ] 가격이 숫자가 아니거나 0보다 작으면 오류가 반환된다.

### 상품 목록 조회

```http
GET /api/products
```

확인 항목:

- [ ] 등록된 상품 목록이 배열 형태로 반환된다.
- [ ] 상품이 없으면 빈 배열이 반환된다.
- [ ] 최신 등록순으로 반환되는지 확인한다.
- [ ] 상품명, 가격, 설명, 대표 이미지, 판매자 정보가 포함된다.

### 상품 검색 및 카테고리 조회

```http
GET /api/products?keyword=전공책
GET /api/products?category=도서
GET /api/products?keyword=전공책&category=도서
```

확인 항목:

- [ ] 검색어와 일치하는 상품만 반환된다.
- [ ] 카테고리와 일치하는 상품만 반환된다.
- [ ] 검색어와 카테고리를 함께 적용해도 정상 조회된다.

### 최신 상품 조회

```http
GET /api/products/latest?limit=5
```

확인 항목:

- [ ] 지정한 개수 이하의 최신 상품이 반환된다.
- [ ] 상품이 없으면 빈 배열이 반환된다.

### 추천 상품 조회

```http
GET /api/products/recommendations?limit=5
```

확인 항목:

- [ ] 지정한 개수 이하의 추천 상품이 반환된다.
- [ ] 상품이 없으면 빈 배열이 반환된다.

### 상품 상세 조회

```http
GET /api/products/{productId}
```

확인 항목:

- [ ] 존재하는 상품 ID로 요청하면 상품 상세 정보가 반환된다.
- [ ] 상품명, 가격, 설명, 카테고리, 거래 방식, 위치, 판매자 정보, 이미지 경로가 포함된다.
- [ ] 존재하지 않는 상품 ID로 요청하면 오류가 반환된다.

### 상품 수정

```http
PATCH /api/products/{productId}
```

요청 예시:

```json
{
  "sellerEmail": "student@sju.ac.kr",
  "productName": "수정된 전공책",
  "category": "도서",
  "price": 10000,
  "description": "수정된 설명입니다.",
  "tradeMethod": "직거래",
  "locationNumber": 5,
  "locationName": "도서관"
}
```

확인 항목:

- [ ] 판매자 본인이 요청하면 상품 정보가 수정된다.
- [ ] 수정된 상품 정보가 응답으로 반환된다.
- [ ] DB의 `updatedAt` 값이 갱신된다.
- [ ] 판매자가 아닌 사용자가 요청하면 오류가 반환된다.

### 상품 삭제

```http
DELETE /api/products/{productId}?sellerEmail=student@sju.ac.kr
```

확인 항목:

- [ ] 판매자 본인이 요청하면 상품이 삭제된다.
- [ ] DB에서 해당 상품이 삭제되었는지 확인한다.
- [ ] 판매자가 아닌 사용자가 요청하면 오류가 반환된다.
- [ ] 존재하지 않는 상품 ID로 요청하면 오류가 반환된다.

## 4. 마이페이지 기능 테스트

### 마이페이지 조회

```http
GET /users/me?email=student@sju.ac.kr
```

확인 항목:

- [ ] 사용자 이메일, 닉네임, 학번이 반환된다.
- [ ] 사용자가 등록한 상품 목록이 반환된다.
- [ ] 등록한 상품이 없으면 상품 목록이 빈 배열로 반환된다.
- [ ] 존재하지 않는 사용자 이메일로 요청하면 오류가 반환된다.

### 마이페이지 프론트 연결 확인

확인 항목:

- [ ] 로그인 후 `localStorage`에 `loginUser`, `loginEmail`이 저장된다.
- [ ] 마이페이지 접속 시 로그인 이메일로 `/users/me`를 호출한다.
- [ ] 사용자 정보가 화면에 표시된다.
- [ ] 내가 등록한 상품 목록이 화면에 표시된다.
- [ ] 상품 카드 클릭 시 상품 상세 화면으로 이동한다.
- [ ] 수정 버튼 클릭 시 상품 수정 API가 호출된다.
- [ ] 삭제 버튼 클릭 시 상품 삭제 API가 호출된다.

## 5. 채팅 기능 테스트

### 채팅방 생성 또는 기존 채팅방 조회

```http
POST /api/chatrooms
```

요청 예시:

```json
{
  "productId": 1,
  "buyerEmail": "buyer@sju.ac.kr"
}
```

확인 항목:

- [ ] 구매자가 판매자와 다른 사용자이면 채팅방이 생성된다.
- [ ] 같은 상품/구매자/판매자 조합의 채팅방이 이미 있으면 기존 채팅방이 반환된다.
- [ ] 구매자와 판매자가 같은 사용자이면 오류가 반환된다.
- [ ] 판매자 정보가 없는 상품이면 오류가 반환된다.

### 채팅방 정보 조회

```http
GET /api/chatrooms/{chatRoomId}
```

확인 항목:

- [ ] 채팅방 ID로 채팅방 정보가 반환된다.
- [ ] 상품명, 구매자 정보, 판매자 정보가 포함된다.
- [ ] 존재하지 않는 채팅방 ID로 요청하면 오류가 반환된다.

### 메시지 목록 조회

```http
GET /api/chatrooms/{chatRoomId}/messages
```

확인 항목:

- [ ] 채팅방의 메시지 목록이 시간순으로 반환된다.
- [ ] 메시지가 없으면 빈 배열이 반환된다.

### 메시지 전송

```http
POST /api/chatrooms/{chatRoomId}/messages
```

요청 예시:

```json
{
  "senderEmail": "buyer@sju.ac.kr",
  "content": "안녕하세요. 아직 판매 중인가요?"
}
```

확인 항목:

- [ ] 채팅방 참여자가 메시지를 전송하면 메시지가 저장된다.
- [ ] 저장된 메시지가 응답으로 반환된다.
- [ ] 빈 메시지를 전송하면 오류가 반환된다.
- [ ] 채팅방 참여자가 아닌 사용자가 전송하면 오류가 반환된다.

## 6. 프론트엔드 통합 흐름 테스트

### 기본 거래 흐름

- [ ] 회원가입 화면에서 이메일 인증을 완료한다.
- [ ] 회원가입을 완료한다.
- [ ] 로그인 화면에서 로그인한다.
- [ ] 로그인 후 마이페이지로 이동한다.
- [ ] 상품 등록 화면으로 이동한다.
- [ ] 상품명, 카테고리, 가격, 설명, 거래 방식, 위치, 이미지를 입력하고 상품을 등록한다.
- [ ] 상품 등록 성공 후 상품 상세 화면으로 이동한다.
- [ ] 상품 목록 화면에서 등록한 상품이 표시되는지 확인한다.
- [ ] 상품 목록 카드 클릭 시 상품 상세 화면으로 이동한다.
- [ ] 상품 상세 화면에서 상품 정보와 이미지가 표시되는지 확인한다.

### 채팅 흐름

- [ ] 판매자와 다른 구매자 계정으로 로그인한다.
- [ ] 상품 상세 화면에서 판매자와 채팅하기 버튼을 클릭한다.
- [ ] 채팅방으로 이동하는지 확인한다.
- [ ] 메시지를 전송한다.
- [ ] 전송한 메시지가 목록에 표시되는지 확인한다.

### 마이페이지 상품 관리 흐름

- [ ] 판매자 계정으로 로그인한다.
- [ ] 마이페이지에 접속한다.
- [ ] 내가 등록한 상품 목록이 표시되는지 확인한다.
- [ ] 상품 수정 버튼을 눌러 상품 정보를 수정한다.
- [ ] 수정 후 마이페이지 목록과 상품 상세 화면에 변경 내용이 반영되는지 확인한다.
- [ ] 상품 삭제 버튼을 눌러 상품을 삭제한다.
- [ ] 삭제 후 마이페이지 목록과 상품 목록에서 해당 상품이 사라졌는지 확인한다.

## 7. 테스트 결과 기록 양식

| 테스트 항목 | 요청 방식 | URL | 결과 | 비고 |
|---|---|---|---|---|
| 이메일 인증번호 발송 | POST | `/users/email/send` |  |  |
| 회원가입 | POST | `/users/signup` |  |  |
| 로그인 | POST | `/users/login` |  |  |
| 상품 등록 | POST | `/api/products` |  |  |
| 상품 목록 조회 | GET | `/api/products` |  |  |
| 상품 상세 조회 | GET | `/api/products/{productId}` |  |  |
| 상품 수정 | PATCH | `/api/products/{productId}` |  |  |
| 상품 삭제 | DELETE | `/api/products/{productId}` |  |  |
| 마이페이지 조회 | GET | `/users/me` |  |  |
| 채팅방 생성 | POST | `/api/chatrooms` |  |  |
| 메시지 전송 | POST | `/api/chatrooms/{chatRoomId}/messages` |  |  |

## 8. 참고 사항

- 현재 프로젝트는 JWT 인증을 적용하지 않았으므로 사용자 식별은 이메일 값을 요청에 포함하는 방식으로 테스트한다.
- 비밀번호는 현재 초기 구현 구조에 맞춰 테스트하며, 추후 암호화 및 재설정 방식으로 개선할 수 있다.
- 실제 메일 인증 테스트를 위해서는 Gmail 앱 비밀번호 등 SMTP 설정이 필요하다.
- 이미지 업로드 테스트 시 파일 크기와 전체 요청 크기가 설정값을 넘지 않도록 확인한다.
