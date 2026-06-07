# 세종마켓 화면 주소 체계

## 목적

정적 HTML 파일 경로 중심으로 작성된 화면 이동을 서비스 기능 기준의 의미 있는 주소 체계로 정리한다.

현재 프로젝트는 정적 HTML 구조이므로 실제 파일 이동은 `*.html` 경로를 사용한다. 다만 서비스 관점에서는 아래와 같은 주소 체계를 기준으로 설명하고, 추후 배포 서버 라우팅을 적용할 때 이 매핑을 사용할 수 있다.

## 서비스 주소 기준

기준 도메인:

```text
https://sejong-market
```

| 기능 | 서비스 기준 주소 | 현재 파일 경로 |
|---|---|---|
| 홈 | `/` | `frontend/main_ui/index.html` |
| 상품 목록 | `/products` | `frontend/main_ui/product-list.html` |
| 상품 상세 | `/products/{id}` | `frontend/product_detail_ui/product-detail.html?id={id}` |
| 상품 등록 | `/products/new` | `frontend/product_create_ui/product-create-ui.html` |
| 로그인 | `/login` | `frontend/login_mypage/login.html` |
| 회원가입 | `/signup` | `frontend/login_mypage/signup.html` |
| 아이디 찾기 | `/find-id` | `frontend/login_mypage/find-id.html` |
| 비밀번호 찾기 | `/find-password` | `frontend/login_mypage/find-pw.html` |
| 마이페이지 | `/mypage` | `frontend/login_mypage/mypage.html` |
| 채팅방 | `/chatrooms/{roomId}` | `frontend/chat_ui/chat.html?roomId={roomId}` |
| 서비스 이용약관 | `/terms` | `frontend/login_mypage/terms.html` |
| 개인정보 처리방침 | `/privacy` | `frontend/login_mypage/privacy.html` |

## 구현 방식

`frontend/common/app-utils.js`에서 화면별 파일 경로와 서비스 주소 기준을 함께 관리한다.

- `pageUrl(routeName, params)`: 현재 정적 HTML 구조에서 실제 이동할 파일 주소 생성
- `serviceUrl(routeName, params)`: 보고서 및 배포 설명에 사용할 서비스 기준 주소 생성
- `productDetailUrl(productId)`: 상품 상세 화면 이동 주소 생성
- `chatRoomUrl(roomId)`: 채팅방 화면 이동 주소 생성
- `normalizeInternalLinks()`: 기존 상대 경로 링크를 공통 주소 체계 기준으로 정규화

## 기대 효과

- 화면 이동 경로가 한 곳에서 관리된다.
- 상품 상세, 채팅방처럼 파라미터가 필요한 주소를 일관되게 생성할 수 있다.
- `#`로 남아 있던 임시 링크를 실제 페이지 경로로 정리할 수 있다.
- 추후 `/products`, `/login` 같은 실제 라우팅을 적용할 때 현재 매핑을 기준으로 확장할 수 있다.
