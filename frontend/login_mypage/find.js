// ===========================
//  세종마켓 — 아이디/비밀번호 찾기 스크립트 (find.js)
// ===========================

// ── 현재 페이지 감지 ──
const isFindId = !!document.getElementById('tab-student');
const isFindPw = !!document.getElementById('pw-step1');

// ===========================
//  공통 — 탭 전환 (아이디 찾기)
// ===========================

if (isFindId) {
  const tabs = document.querySelectorAll('.tab');
  const tabContents = document.querySelectorAll('.tab-content');

  tabs.forEach(function (tab) {
    tab.addEventListener('click', function () {
      tabs.forEach(function (t) { t.classList.remove('active'); });
      tabContents.forEach(function (c) { c.classList.remove('active'); });

      tab.classList.add('active');
      const target = document.getElementById('tab-' + tab.dataset.tab);
      if (target) target.classList.add('active');

      hideResults();
    });
  });
}

// ===========================
//  아이디 찾기 — 학번으로
// ===========================

function findIdByStudent() {
  const name      = document.getElementById('findName1').value.trim();
  const studentId = document.getElementById('findStudentId').value.trim();

  if (!name)                        { alert('이름을 입력해 주세요.'); return; }
  if (!/^\d{8}$/.test(studentId))  { alert('학번 8자리를 올바르게 입력해 주세요.'); return; }

  // 실제 서비스: fetch('/api/find-id', { method: 'POST', body: JSON.stringify({ name, studentId }) })
  // 응답 예시: { id: 'hong****', createdAt: '2023-03-01' }

  // 데모용 결과
  showIdResult('hong****', '2023년 03월 01일 가입');
}

// ===========================
//  아이디 찾기 — 이메일로
// ===========================

function findIdByEmail() {
  const name  = document.getElementById('findName2').value.trim();
  const email = document.getElementById('findEmail').value.trim();

  if (!name)  { alert('이름을 입력해 주세요.'); return; }
  if (!email) { alert('이메일을 입력해 주세요.'); return; }

  // 실제 서비스: fetch('/api/find-id', { method: 'POST', body: JSON.stringify({ name, email: email + '@sju.ac.kr' }) })

  // 데모용 결과
  showIdResult('hong****', '2023년 03월 01일 가입');
}

// ── 아이디 결과 표시 ──
function showIdResult(id, date) {
  hideResults();
  const resultBox = document.getElementById('resultBox');
  if (!resultBox) return;
  document.getElementById('resultId').textContent   = id;
  document.getElementById('resultDate').textContent = date;
  resultBox.style.display = 'block';
}

function hideResults() {
  const resultBox = document.getElementById('resultBox');
  const failBox   = document.getElementById('failBox');
  if (resultBox) resultBox.style.display = 'none';
  if (failBox)   failBox.style.display   = 'none';
}

// ===========================
//  비밀번호 찾기 — 단계 이동
// ===========================

let pwTimerInterval = null;

// STEP 1 → STEP 2: 본인 확인 후 인증 코드 발송
function pwGoStep2() {
  const id    = document.getElementById('pwFindId').value.trim();
  const name  = document.getElementById('pwFindName').value.trim();
  const email = document.getElementById('pwFindEmail').value.trim();

  if (!id)    { alert('아이디를 입력해 주세요.'); return; }
  if (!name)  { alert('이름을 입력해 주세요.'); return; }
  if (!email) { alert('이메일을 입력해 주세요.'); return; }

  // 실제 서비스: fetch('/api/send-pw-code', { method: 'POST', body: JSON.stringify({ id, name, email: email + '@sju.ac.kr' }) })

  document.getElementById('sentEmailText').textContent =
    email + '@sju.ac.kr 으로 인증 코드를 발송했어요.';

  pwChangeStep(2);
  startPwTimer();
}

// STEP 2 → STEP 3: 인증 코드 확인 후 비밀번호 표시
function pwGoStep3() {
  const code = document.getElementById('pwCode').value.trim();

  if (code.length !== 6) { alert('6자리 인증 코드를 입력해 주세요.'); return; }

  // 실제 서비스: fetch('/api/verify-pw-code', { method: 'POST', body: JSON.stringify({ code }) })
  // 응답 예시: { password: 'abc1234!' }
  // 실제로는 응답받은 비밀번호를 아래 showFoundPw()에 넣어요

  clearInterval(pwTimerInterval);
  pwChangeStep(3);

  // 데모용: 서버에서 받아온 비밀번호라고 가정
  showFoundPw('abc1234!');
}

// ── 비밀번호 표시 ──
const REAL_PW   = { value: '' };  // 실제 비밀번호 저장 (토글용)
let   pwVisible = false;           // 현재 보기 상태

function showFoundPw(password) {
  REAL_PW.value = password;
  pwVisible = false;

  // 처음엔 ●으로 마스킹해서 표시
  document.getElementById('foundPw').textContent = '●'.repeat(password.length);
  document.getElementById('pwToggleIcon').className = 'ti ti-eye';
}

// 비밀번호 보기/숨기기 토글
function togglePwVisible() {
  const pwEl   = document.getElementById('foundPw');
  const iconEl = document.getElementById('pwToggleIcon');

  pwVisible = !pwVisible;

  if (pwVisible) {
    pwEl.textContent    = REAL_PW.value;           // 실제 비밀번호 표시
    iconEl.className    = 'ti ti-eye-off';         // 아이콘 → 숨기기
  } else {
    pwEl.textContent    = '●'.repeat(REAL_PW.value.length); // 다시 마스킹
    iconEl.className    = 'ti ti-eye';             // 아이콘 → 보기
  }
}

// ── 단계 전환 ──
function pwChangeStep(n) {
  document.querySelectorAll('.step-content').forEach(function (el) {
    el.classList.remove('active');
  });
  const target = document.getElementById('pw-step' + n);
  if (target) target.classList.add('active');
}

// ── 3분 타이머 ──
function startPwTimer() {
  clearInterval(pwTimerInterval);
  let seconds = 180;
  const el = document.getElementById('pwTimer');
  if (!el) return;

  pwTimerInterval = setInterval(function () {
    seconds--;
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    el.textContent = m + ':' + String(s).padStart(2, '0');

    if (seconds <= 0) {
      clearInterval(pwTimerInterval);
      el.textContent = '만료됨';
    }
  }, 1000);
}

// ── 재발송 ──
function resendCode() {
  startPwTimer();
  alert('인증 코드를 재발송했어요.');
}

// ── 학번 숫자만 입력 ──
const findStudentIdInput = document.getElementById('findStudentId');
if (findStudentIdInput) {
  findStudentIdInput.addEventListener('input', function () {
    this.value = this.value.replace(/[^0-9]/g, '');
  });
}