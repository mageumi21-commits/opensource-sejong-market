# 실제 배포 설정 가이드

## 배포 시 반드시 맞춰야 하는 조건

세종마켓을 실제 배포 환경에서 여러 사용자가 함께 사용하려면 아래 3가지 조건을 만족해야 한다.

1. 모든 사용자가 접근할 수 있는 공개 백엔드 서버가 있어야 한다.
2. 공개 백엔드 서버는 하나의 공용 MySQL DB에 연결되어야 한다.
3. 프론트엔드는 해당 공개 백엔드 주소를 API 서버로 사용해야 한다.

이 조건이 맞아야 한 사용자가 등록한 상품을 다른 사용자가 상품 목록에서 볼 수 있고, 판매자와 구매자가 같은 채팅방 메시지를 공유할 수 있다.

## 백엔드 배포 환경변수

배포 서버에서는 다음 환경변수를 설정한다.

| 환경변수 | 설명 | 예시 |
|---|---|---|
| `PORT` | 백엔드 서버 포트 | `8080` |
| `SERVER_ADDRESS` | 외부 접속 바인딩 주소 | `0.0.0.0` |
| `DB_URL` | 공용 MySQL DB 주소 | `jdbc:mysql://db-host:3306/sejong_market?serverTimezone=Asia/Seoul&characterEncoding=UTF-8` |
| `DB_USERNAME` | 공용 DB 계정 | `sejong_user` |
| `DB_PASSWORD` | 공용 DB 비밀번호 | `비밀번호` |
| `MAIL_USERNAME` | 이메일 인증 발송 계정 | `example@gmail.com` |
| `MAIL_PASSWORD` | 이메일 앱 비밀번호 | `앱비밀번호` |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | 허용할 프론트 주소 | `https://frontend.example.com` |
| `PRODUCT_IMAGE_UPLOAD_DIR` | 상품 이미지 저장 폴더 | `/var/sejong-market/uploads/products` |
| `JPA_DDL_AUTO` | JPA 테이블 관리 방식 | `update` |
| `JPA_SHOW_SQL` | SQL 로그 출력 여부 | `false` |

로컬 개발에서는 기본값이 적용되므로 별도 설정 없이 기존처럼 실행할 수 있다.

## 백엔드 배포 확인

백엔드 배포 후 다음 주소로 접속한다.

```text
https://배포된-백엔드-주소/health
```

정상 응답 예시는 다음과 같다.

```json
{
  "status": "ok"
}
```

이 응답이 나오면 프론트엔드가 사용할 수 있는 공개 백엔드 주소가 준비된 것이다.

## 프론트엔드 배포 설정

프론트엔드 배포 전 `frontend/common/api-config.js`의 주소를 배포된 백엔드 주소로 변경한다.

```js
window.SEJONG_MARKET_API_BASE_URL =
  window.SEJONG_MARKET_API_BASE_URL || 'https://배포된-백엔드-주소';
```

이 파일을 수정하면 로그인, 회원가입, 상품 등록, 상품 목록, 상품 상세, 마이페이지, 채팅 화면이 모두 같은 백엔드 서버를 바라본다.

## 공용 DB 기준 확인

실제 배포에서는 팀원 각자의 로컬 MySQL이 아니라, 백엔드 서버가 연결한 하나의 공용 MySQL DB에 모든 데이터가 저장되어야 한다.

확인할 데이터는 다음과 같다.

- 회원가입한 사용자 정보
- 등록된 상품 정보
- 상품 이미지 경로
- 채팅방 정보
- 채팅 메시지 정보

## 배포 후 통합 테스트 순서

1. 배포된 백엔드 `/health` 응답 확인
2. 프론트엔드 `api-config.js`가 배포 백엔드 주소를 바라보는지 확인
3. 사용자 A 회원가입 및 로그인
4. 사용자 A 상품 등록
5. 사용자 B 로그인
6. 사용자 B 상품 목록에서 사용자 A 상품 확인
7. 사용자 B 상품 상세 진입
8. 사용자 B가 판매자와 채팅하기 클릭
9. 사용자 A와 사용자 B가 같은 채팅방에서 메시지 송수신 확인

## 현재 한계

- 채팅은 WebSocket이 아니라 polling 방식이다.
- 상품 이미지는 백엔드 서버의 파일 시스템에 저장된다.
- 배포 플랫폼이 파일 시스템을 유지하지 않는다면 이미지 저장소를 별도 스토리지로 분리해야 한다.
- JWT 인증은 아직 적용하지 않았으므로 로그인 사용자는 `localStorage` 기반으로 관리된다.
