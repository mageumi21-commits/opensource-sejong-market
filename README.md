# Pull Request 가이드라인
<br><br>
## 1. 브랜치 규칙 (Branch Rule)

본 프로젝트는 **GitHub Flow 전략**을 사용한다.

모든 팀원은 먼저 원본 저장소(Upstream Repository)를 **Clone** 한 뒤,

**기능별 브랜치**를 생성하여 작업을 진행한다.

작업이 완료되면 원본 저장소의 `master` 브랜치로 Pull Request를 생성한다.

직접 `master` 브랜치에서 작업하거나 Push하는 것은 금지한다.
<br><br>
### 브랜치 이름 규칙

브랜치 이름은 작업 내용을 명확히 알 수 있도록 작성한다.

예시:

```bash
feat/login
feat/product-create
feat/product-search
fix/product-delete-error
docs/update-readme
```
구체적인 이름 규칙은 아래의 **Pull Request 제목 규칙** 항목의 **키워드 규칙**을 참고 할 것.

---
<br><br>
## 2. Pull Request 제목 규칙 (Pull Request Title)

모든 Pull Request 제목은 반드시 아래 키워드 중 하나로 시작해야 한다.

### 키워드 규칙

- `feat:` 새로운 기능 추가
- `fix:` 버그 수정
- `docs:` 문서 수정
- `style:` 코드 동작과 무관한 형식 수정 (공백, 들여쓰기, 세미콜론 등)
- `refactor:` 기능 변화 없이 코드 구조 개선
- `test:` 테스트 코드 추가 또는 수정
- `build:` 빌드 설정 또는 외부 라이브러리 관련 변경
- `chore:` 기타 설정 및 유지보수 작업
- `revert:` 이전 작업 되돌리기

### 작성 예시

```text
feat: 상품 등록 기능 추가
fix: 로그인 오류 수정
docs: README 실행 방법 추가
refactor: 상품 검색 코드 개선
```

---
<br><br>
## 3. Pull Request 설명 작성 규칙 (Pull Request Description)

모든 Pull Request에는 아래 내용을 반드시 작성해야 한다.

### 작성 형식

```text
## 작업 내용
구현하거나 수정한 내용을 작성

## 변경 파일
수정한 주요 파일들을 작성

## 테스트 방법
어떤 방식으로 테스트했는지 작성

## 참고 사항
리뷰어가 확인해야 할 내용이 있다면 작성
```

### 작성 예시

```text
## 작업 내용
상품 등록 API를 구현했습니다.

## 변경 파일
- ProductController.java
- ProductService.java
- ProductRepository.java

## 테스트 방법
- 서버 실행 후 Postman으로 상품 등록 요청 테스트
- 정상적으로 DB에 저장되는지 확인

## 참고 사항
상품 이미지 업로드 기능은 아직 구현하지 않았습니다.
```

---
<br><br>
## 4. 코드 리뷰 규칙 (Review Rule)

모든 Pull Request는 최소 **1명 이상의 팀원 리뷰**를 받은 후 Merge할 수 있다.

### 리뷰 체크 항목

리뷰어는 아래 내용을 확인해야 한다.

- 코드가 정상적으로 실행되는가?
- 구현한 기능이 정상 동작하는가?
- 불필요한 파일이 포함되어 있지 않은가?
- Commit 메시지가 의미 있게 작성되었는가?
- 기존 기능에 영향을 주지 않는가?

문제가 발견되면 리뷰 코멘트를 남긴다.

작성자는 수정 후 다시 Push하여 리뷰를 요청한다.

---
<br><br>
## 5. Merge 규칙 (Merge Rule)

Pull Request 승인 후 **조장(팀장)이 Merge를 진행한다.**

Merge 전 반드시 아래 내용을 확인해야 한다.
<br>
### Merge 전 체크 항목

- 충돌 사항이 없는가?
- 리뷰에서 요청된 수정사항이 반영되었는가?
- 기능이 정상적으로 동작하는가?

### 충돌 발생 시

병합 시 충돌이 발생한 경우:

- Pull Request 작성자가 직접 충돌을 해결.
- 충돌 해결 후 다시 리뷰를 요청.

---
<br><br>
## 6. 테스트 규칙 (Testing Rule)

본 프로젝트는 자동 테스트(CI)를 사용하지 않으므로, 모든 팀원은 Pull Request 생성 전 반드시 **직접 테스트**를 진행해야 한다.
<br>
### 테스트 체크 항목

PR 생성 전 아래 항목을 확인한다.

- 프로젝트가 정상적으로 실행되는가?
- 내가 구현한 기능이 정상 동작하는가?
- 기존 기능이 깨지지 않았는가?
- 실행 중 오류가 발생하지 않는가?

필요한 경우 Pull Request 설명에 테스트 결과를 함께 작성한다.
<br>
### 작성 예시

```text
테스트 결과:

- 로그인 기능 정상 작동 확인
- 상품 등록 기능 정상 작동 확인
- 상품 검색 기능 정상 작동 확인
```