// ===========================
//  세종마켓 — 회원가입 스크립트 (signup.js)
// ===========================

const API_BASE_URL = 'http://localhost:8080';

function getSignupEmail() {
  return emailIdInput.value.trim() + '@sju.ac.kr';
}

async function readErrorMessage(response) {
  const message = await response.text();
  return message || '요청 처리 중 오류가 발생했습니다.';
}

// ── 단과대학별 학과 목록 ──
const deptMap = {
  '공과대학': ['기계공학과', '우주항공시스템공학부', '건설환경공학과', '건축학과', '건축공학과', '에너지자원공학과', '양자원자력공학과', '국방AI융합시스템공학과', '환경융합공학과', '나노신소재공학과'],
  '인공지능융합대학': ['AI융합전자공학과', '컴퓨터공학과', '양자지능정보학과', '소프트웨어학과', '콘텐츠소프트웨어학과', '반도체시스템공학과', '전자정보통신공학과', '정보보호학과', '사이버국방학과', '인공지능데이터사이언스학과', '국방AI로봇융합공학과', 'AI로봇학과', '지능정보융합학과', '지능IoT학과', '디자인이노베이션', '만화애니메이션텍'],
  '경영경제대학': ['경영학부', '경제학과'],
  '인문과학대학': ['교육학과', '국어국문학과', '국제학부', '글로벌인재학부', '역사학과'],
  '자연과학대학': ['수학통계학과', '물리천문학과', '화학과'],
  '사회과학대학': ['행정학과', '미디어커뮤니케이션학과', '법학과'],
  '생명과학대학': ['식품생명공학', '바이오융합공학', '바이오산업자원공학', '스마트생명산업융합학과'],
  '호텔관광대학': ['호텔관광외식경영학부', '호텔외식관광프랜차이즈경영학과', '조리서비스경영학과', '호텔외식비즈니스학과'],
  '예체능대학': ['회화과', '음악과', '무용과', '패션디자인학과', '체육학과', '영화예술학과']
};

// ── 상태 변수 ──
let verified = false;
let timerInterval = null;
let currentStep = 1;

// ── DOM 요소 ──
const emailIdInput   = document.getElementById('emailId');
const verifyBtn      = document.getElementById('verifyBtn');
const verifyHint     = document.getElementById('verifyHint');
const codeField      = document.getElementById('codeField');
const codeInput      = document.getElementById('codeInput');
const timerEl        = document.getElementById('timer');
const verifiedBadge  = document.getElementById('verifiedBadge');
const nextStep1Btn   = document.getElementById('nextStep1');
const collegeSelect  = document.getElementById('college');
const deptSelect     = document.getElementById('dept');
const pwInput        = document.getElementById('pw');
const pwConfirmInput = document.getElementById('pwConfirm');
const termAllCheck   = document.getElementById('termAll');
const termChecks     = document.querySelectorAll('.term');

// ===========================
//  단계 이동
// ===========================

function goStep(n) {
  // 현재 단계 숨기기
  document.getElementById('step' + currentStep).classList.remove('active');

  // 이전 단계 표시 점 업데이트
  const prevDot = document.getElementById('step' + currentStep + '-dot');
  if (prevDot) {
    prevDot.classList.remove('active');
    if (n > currentStep) prevDot.classList.add('done'); // 완료 표시
  }

  currentStep = n;

  // 새 단계 활성화
  document.getElementById('step' + currentStep).classList.add('active');
  const newDot = document.getElementById('step' + currentStep + '-dot');
  if (newDot) {
    newDot.classList.add('active');
    newDot.classList.remove('done');
  }
}

// ===========================
//  STEP 1 — 이메일 인증
// ===========================

// 이메일 아이디 입력 시 인증 버튼 활성화
emailIdInput.addEventListener('input', function () {
  const val = this.value.trim();
  verifyBtn.disabled = val.length < 2;
  verifyHint.style.display = val.length >= 2 ? 'block' : 'none';
});

// 인증 버튼 클릭
verifyBtn.addEventListener('click', async function () {
  const emailId = emailIdInput.value.trim();
  if (!emailId) return;

  const email = getSignupEmail();

  try {
    verifyBtn.disabled = true;
    verifyBtn.textContent = '발송 중';

    const response = await fetch(`${API_BASE_URL}/users/email/send`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: email
      })
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    codeField.style.display = 'block';
    verifyBtn.textContent = '재발송';

    startTimer();

    // 30초 뒤 재발송 활성화
    setTimeout(function () {
      verifyBtn.disabled = false;
    }, 30000);
  } catch (error) {
    alert(error.message || '인증번호 발송 중 오류가 발생했습니다.');
    verifyBtn.disabled = false;
    verifyBtn.textContent = '인증';
  }
});

// 3분 타이머
function startTimer() {
  clearInterval(timerInterval);
  let seconds = 180;

  timerInterval = setInterval(function () {
    seconds--;
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    timerEl.textContent = m + ':' + String(s).padStart(2, '0');

    if (seconds <= 0) {
      clearInterval(timerInterval);
      timerEl.textContent = '만료됨';
    }
  }, 1000);
}

function completeEmailVerification() {
  verified = true;
  clearInterval(timerInterval);

  codeField.style.display = 'none';
  verifyHint.style.display = 'none';
  verifyBtn.style.display = 'none';
  verifiedBadge.classList.add('show');
  nextStep1Btn.disabled = false; // 다음 버튼 활성화
}

// 인증 코드 6자리 입력 완료
codeInput.addEventListener('input', async function () {
  if (this.value.length === 6) {
    const email = getSignupEmail();
    const code = this.value;

    try {
      codeInput.disabled = true;

      const response = await fetch(`${API_BASE_URL}/users/email/verify`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          email: email,
          code: code
        })
      });

      if (!response.ok) {
        throw new Error(await readErrorMessage(response));
      }

      completeEmailVerification();
    } catch (error) {
      alert(error.message || '인증번호 확인 중 오류가 발생했습니다.');
      codeInput.value = '';
      codeInput.focus();
    } finally {
      codeInput.disabled = false;
    }
  }
});

// ===========================
//  STEP 2 — 기본 정보
// ===========================

// 단과대학 선택 시 학과 자동 변경
collegeSelect.addEventListener('change', function () {
  const depts = deptMap[this.value] || [];
  deptSelect.innerHTML = '<option value="">선택</option>';
  depts.forEach(function (d) {
    const opt = document.createElement('option');
    opt.textContent = d;
    deptSelect.appendChild(opt);
  });
});

// 이름 유효성
document.getElementById('name').addEventListener('blur', function () {
  const err = document.getElementById('nameErr');
  if (!this.value.trim()) {
    err.classList.add('show');
    this.classList.add('is-error');
  } else {
    err.classList.remove('show');
    this.classList.remove('is-error');
  }
});

// 학번 유효성 (숫자만, 8자리)
document.getElementById('studentId').addEventListener('input', function () {
  this.value = this.value.replace(/[^0-9]/g, '');
  const err = document.getElementById('studentIdErr');
  if (this.value.length > 0 && !/^\d{8}$/.test(this.value)) {
    err.classList.add('show');
    this.classList.add('is-error');
  } else {
    err.classList.remove('show');
    this.classList.remove('is-error');
  }
});

// ===========================
//  STEP 3 — 비밀번호
// ===========================

// 비밀번호 강도 표시
pwInput.addEventListener('input', function () {
  const pw = this.value;
  const fill = document.getElementById('strengthFill');
  const label = document.getElementById('strengthLabel');

  let score = 0;
  if (pw.length >= 8)            score++;
  if (/[a-zA-Z]/.test(pw))      score++;
  if (/[0-9]/.test(pw))         score++;
  if (/[^a-zA-Z0-9]/.test(pw)) score++;

  const widths  = ['0%', '25%', '50%', '75%', '100%'];
  const colors  = ['', '#e74c3c', '#e67e22', '#f1c40f', '#27ae60'];
  const labels  = ['', '매우 약함', '약함', '보통', '강함'];

  fill.style.width = widths[score];
  fill.style.backgroundColor = colors[score] || '#eee';
  label.textContent = labels[score] || '';
});

// 비밀번호 확인 일치
pwConfirmInput.addEventListener('input', function () {
  const err = document.getElementById('pwConfirmErr');
  if (this.value && this.value !== pwInput.value) {
    err.classList.add('show');
    this.classList.add('is-error');
  } else {
    err.classList.remove('show');
    this.classList.remove('is-error');
  }
});

// ── 약관 전체 동의 ──
termAllCheck.addEventListener('change', function () {
  termChecks.forEach(function (c) { c.checked = termAllCheck.checked; });
});

termChecks.forEach(function (check) {
  check.addEventListener('change', function () {
    termAllCheck.checked = Array.from(termChecks).every(function (c) { return c.checked; });
  });
});

// ===========================
//  최종 회원가입 제출
// ===========================

async function handleSignup() {
  const name      = document.getElementById('name').value.trim();
  const studentId = document.getElementById('studentId').value;
  const pw        = pwInput.value;
  const pwConfirm = pwConfirmInput.value;
  const term1     = document.getElementById('term1').checked;
  const term2     = document.getElementById('term2').checked;
  const submitBtn = document.querySelector('.submit-btn');

  if (!verified) { alert('이메일 인증을 완료해 주세요.'); return; }
  if (!name) { alert('이름을 입력해 주세요.'); return; }
  if (!/^\d{8}$/.test(studentId)) { alert('학번 8자리를 올바르게 입력해 주세요.'); return; }
  if (pw.length < 8) { alert('비밀번호를 8자 이상 입력해 주세요.'); return; }
  if (pw !== pwConfirm) { alert('비밀번호가 일치하지 않아요.'); return; }
  if (!term1 || !term2) { alert('필수 약관에 동의해 주세요.'); return; }

  try {
    if (submitBtn) {
      submitBtn.disabled = true;
      submitBtn.textContent = '가입 중...';
    }

    const response = await fetch(`${API_BASE_URL}/users/signup`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email: getSignupEmail(),
        password: pw,
        nickname: name,
        studentId: studentId
      })
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    alert('회원가입이 완료됐어요! 환영합니다 :)');
    window.location.href = 'login.html';
  } catch (error) {
    alert(error.message || '회원가입 중 오류가 발생했습니다.');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.innerHTML = '<i class="ti ti-user-plus"></i> 회원가입 완료';
    }
  }
}
