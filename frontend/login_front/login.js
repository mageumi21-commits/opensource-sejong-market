// ===========================
//  세종마켓 — 로그인 스크립트 (login.js)
// ===========================

const API_BASE_URL = 'http://localhost:8080';

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
function handleLogin() {
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

  // 실제 서비스에서는 서버에 로그인 요청을 보내요
  // 예: fetch('/api/login', { method: 'POST', body: JSON.stringify({ id, pw }) })

  // 데모: 성공 메시지 후 메인 페이지로 이동
  alert('로그인 성공! 환영합니다 :)');
  // window.location.href = 'index.html';
}

// ── 엔터 키로 로그인 ──
document.addEventListener('keydown', function (e) {
  if (e.key === 'Enter') {
    const activeTab = document.querySelector('.tab-content.active');
    const btn = activeTab ? activeTab.querySelector('.login-btn') : null;
    if (btn) btn.click();
  }
});
