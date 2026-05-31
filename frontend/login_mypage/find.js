// ===========================
//  세종마켓 - 아이디/비밀번호 찾기 스크립트
// ===========================

const API_BASE_URL = 'http://localhost:8080';

const isFindId = !!document.getElementById('tab-student');
const isFindPw = !!document.getElementById('pw-step1');

let pwTimerInterval = null;
let passwordFindEmail = '';
let REAL_PW = { value: '' };
let pwVisible = false;

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

async function findIdByStudent() {
  const name = document.getElementById('findName1').value.trim();
  const studentId = document.getElementById('findStudentId').value.trim();

  if (!name) { alert('이름을 입력해 주세요.'); return; }
  if (!/^\d{8}$/.test(studentId)) { alert('학번 8자리를 올바르게 입력해 주세요.'); return; }

  try {
    const response = await fetch(`${API_BASE_URL}/users/find-id`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nickname: name, studentId }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    const result = await response.json();
    showIdResult(result.maskedEmail || result.email, '가입된 세종대학교 이메일입니다.');
  } catch (error) {
    showFail(error.message || '일치하는 회원 정보를 찾을 수 없습니다.');
  }
}

async function findIdByEmail() {
  const name = document.getElementById('findName2').value.trim();
  const email = document.getElementById('findEmail').value.trim();

  if (!name) { alert('이름을 입력해 주세요.'); return; }
  if (!email) { alert('이메일을 입력해 주세요.'); return; }

  try {
    const response = await fetch(`${API_BASE_URL}/users/find-id`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nickname: name, email: toSejongEmail(email) }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    const result = await response.json();
    showIdResult(result.maskedEmail || result.email, '가입된 세종대학교 이메일입니다.');
  } catch (error) {
    showFail(error.message || '일치하는 회원 정보를 찾을 수 없습니다.');
  }
}

function showIdResult(id, date) {
  hideResults();
  const resultBox = document.getElementById('resultBox');
  if (!resultBox) return;

  document.getElementById('resultId').textContent = id;
  document.getElementById('resultDate').textContent = date;
  resultBox.style.display = 'block';
}

function showFail(message) {
  hideResults();
  const failBox = document.getElementById('failBox');
  if (!failBox) return;

  failBox.textContent = message;
  failBox.style.display = 'block';
}

function hideResults() {
  const resultBox = document.getElementById('resultBox');
  const failBox = document.getElementById('failBox');
  if (resultBox) resultBox.style.display = 'none';
  if (failBox) failBox.style.display = 'none';
}

async function pwGoStep2() {
  const id = document.getElementById('pwFindId').value.trim();
  const name = document.getElementById('pwFindName').value.trim();
  const email = document.getElementById('pwFindEmail').value.trim();

  if (!id) { alert('아이디를 입력해 주세요.'); return; }
  if (!name) { alert('이름을 입력해 주세요.'); return; }
  if (!email) { alert('이메일을 입력해 주세요.'); return; }

  passwordFindEmail = toSejongEmail(email);

  try {
    const response = await fetch(`${API_BASE_URL}/users/password/email/send`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id: toSejongEmail(id),
        nickname: name,
        email: passwordFindEmail,
      }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    document.getElementById('sentEmailText').textContent =
      `${passwordFindEmail} 으로 인증 코드를 발송했어요.`;

    pwChangeStep(2);
    startPwTimer();
  } catch (error) {
    alert(error.message || '인증 코드 발송에 실패했습니다.');
  }
}

async function pwGoStep3() {
  const code = document.getElementById('pwCode').value.trim();
  const id = document.getElementById('pwFindId').value.trim();
  const name = document.getElementById('pwFindName').value.trim();

  if (code.length !== 6) { alert('6자리 인증 코드를 입력해 주세요.'); return; }

  try {
    const response = await fetch(`${API_BASE_URL}/users/password/verify`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id: toSejongEmail(id),
        nickname: name,
        email: passwordFindEmail,
        code,
      }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    const result = await response.json();
    clearInterval(pwTimerInterval);
    pwChangeStep(3);
    showFoundPw(result.password);
  } catch (error) {
    alert(error.message || '인증번호 확인에 실패했습니다.');
  }
}

function showFoundPw(password) {
  REAL_PW.value = password;
  pwVisible = false;

  document.getElementById('foundPw').textContent = '●'.repeat(password.length);
  document.getElementById('pwToggleIcon').className = 'ti ti-eye';
}

function togglePwVisible() {
  const pwEl = document.getElementById('foundPw');
  const iconEl = document.getElementById('pwToggleIcon');

  pwVisible = !pwVisible;

  if (pwVisible) {
    pwEl.textContent = REAL_PW.value;
    iconEl.className = 'ti ti-eye-off';
  } else {
    pwEl.textContent = '●'.repeat(REAL_PW.value.length);
    iconEl.className = 'ti ti-eye';
  }
}

function pwChangeStep(n) {
  document.querySelectorAll('.step-content').forEach(function (el) {
    el.classList.remove('active');
  });
  const target = document.getElementById('pw-step' + n);
  if (target) target.classList.add('active');
}

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

async function resendCode() {
  const id = document.getElementById('pwFindId').value.trim();
  const name = document.getElementById('pwFindName').value.trim();

  if (!passwordFindEmail) {
    alert('먼저 본인 확인을 진행해 주세요.');
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/users/password/email/send`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id: toSejongEmail(id),
        nickname: name,
        email: passwordFindEmail,
      }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    startPwTimer();
    alert('인증 코드를 재발송했어요.');
  } catch (error) {
    alert(error.message || '인증 코드 재발송에 실패했습니다.');
  }
}

function toSejongEmail(value) {
  const trimmed = value.trim().toLowerCase();
  if (trimmed.includes('@')) {
    return trimmed;
  }
  return `${trimmed}@sju.ac.kr`;
}

async function readErrorMessage(response) {
  const text = await response.text();
  if (!text) {
    return '요청 처리에 실패했습니다.';
  }

  try {
    const data = JSON.parse(text);
    return data.message || data.error || text;
  } catch (error) {
    return text;
  }
}

const findStudentIdInput = document.getElementById('findStudentId');
if (findStudentIdInput) {
  findStudentIdInput.addEventListener('input', function () {
    this.value = this.value.replace(/[^0-9]/g, '');
  });
}
