// ===========================
//  세종마켓 — 로그인 스크립트 (login.js)
// ===========================

const { API_BASE_URL, toSejongEmail, readErrorMessage } = window.SejongMarketUtils;

// ── 탭 전환 ──
const tabs = document.querySelectorAll('.tab');
const tabContents = document.querySelectorAll('.tab-content');

tabs.forEach(function (tab) {
  tab.addEventListener('click', function () {
    // 모든 탭 비활성화
    tabs.forEach(function (t) { t.classList.remove('active'); });
    tabContents.forEach(function (c) { c.classList.remove('active'); });

    // 클릭한 탭 활성화
    tab.classList.add('active');
    const target = document.getElementById('tab-' + tab.dataset.tab);
    if (target) target.classList.add('active');
  });
});

// ── 로그인 처리 ──
async function handleLogin() {
  const id = document.getElementById('loginId').value.trim();
  const pw = document.getElementById('loginPw').value;

  if (!id) {
    alert('아이디 또는 이메일을 입력해 주세요.');
    document.getElementById('loginId').focus();
    return;
  }
  if (!pw) {
    alert('비밀번호를 입력해 주세요.');
    document.getElementById('loginPw').focus();
    return;
  }

  const email = toSejongEmail(id);
  const loginBtn = document.querySelector('#tab-id .login-btn');

  try {
    loginBtn.disabled = true;
    loginBtn.textContent = '로그인 중...';

    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        email,
        password: pw
      })
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    const loginUser = await response.json();
    saveLoginUser(loginUser, email);

    alert('로그인 성공!');
    window.location.href = window.SejongMarketUtils.pageUrl('products');
  } catch (error) {
    alert(error.message || '로그인에 실패했습니다.');
  } finally {
    loginBtn.disabled = false;
    loginBtn.textContent = '로그인';
  }
}

async function handleStudentLogin() {
  const studentId = document.getElementById('studentLoginId').value.trim();
  const pw = document.getElementById('studentLoginPw').value;

  if (!studentId) {
    alert('학번을 입력해 주세요.');
    document.getElementById('studentLoginId').focus();
    return;
  }
  if (!pw) {
    alert('비밀번호를 입력해 주세요.');
    document.getElementById('studentLoginPw').focus();
    return;
  }

  const loginBtn = document.querySelector('#tab-student .login-btn');

  try {
    loginBtn.disabled = true;
    loginBtn.textContent = '로그인 중...';

    const response = await fetch(`${API_BASE_URL}/users/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        studentId,
        password: pw
      })
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    const loginUser = await response.json();
    saveLoginUser(loginUser, loginUser.email);

    alert('로그인 성공!');
    window.location.href = window.SejongMarketUtils.pageUrl('products');
  } catch (error) {
    alert(error.message || '로그인에 실패했습니다.');
  } finally {
    loginBtn.disabled = false;
    loginBtn.textContent = '로그인';
  }
}

function saveLoginUser(loginUser, fallbackEmail) {
  const email = loginUser && loginUser.email ? loginUser.email : fallbackEmail;
  const user = {
    email,
    nickname: loginUser && loginUser.nickname ? loginUser.nickname : '',
    studentId: loginUser && loginUser.studentId ? loginUser.studentId : ''
  };

  localStorage.setItem('loginUser', JSON.stringify(user));
  localStorage.setItem('loginEmail', email);
}

// ── 엔터 키로 로그인 ──
document.addEventListener('keydown', function (e) {
  if (e.key === 'Enter') {
    const activeTab = document.querySelector('.tab-content.active');
    const btn = activeTab ? activeTab.querySelector('.login-btn') : null;
    if (btn) btn.click();
  }
});
