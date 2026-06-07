window.SejongMarketUtils = (function () {
  const API_BASE_URL = 'http://localhost:8080';

  function toSejongEmail(value) {
    const trimmed = String(value || '').trim().toLowerCase();
    if (trimmed.includes('@')) {
      return trimmed;
    }
    return `${trimmed}@sju.ac.kr`;
  }

  function getLoginEmail() {
    try {
      const loginUser = JSON.parse(localStorage.getItem('loginUser'));
      if (loginUser && loginUser.email) {
        return loginUser.email;
      }
    } catch (error) {
      console.warn('loginUser 값을 읽을 수 없습니다.', error);
    }

    return localStorage.getItem('loginEmail') || localStorage.getItem('userEmail') || '';
  }

  function getLoginUser() {
    try {
      const loginUser = JSON.parse(localStorage.getItem('loginUser'));
      return loginUser && typeof loginUser === 'object' ? loginUser : null;
    } catch (error) {
      console.warn('loginUser 값을 읽을 수 없습니다.', error);
      return null;
    }
  }

  function getLoginDisplayName() {
    const loginUser = getLoginUser();
    if (loginUser && loginUser.nickname) {
      return loginUser.nickname;
    }

    const email = getLoginEmail();
    if (email) {
      return email.split('@')[0];
    }

    return '';
  }

  function clearLoginStorage() {
    localStorage.removeItem('loginUser');
    localStorage.removeItem('loginEmail');
    localStorage.removeItem('userEmail');
  }

  function renderAuthArea(target, options = {}) {
    const element = typeof target === 'string' ? document.getElementById(target) : target;
    if (!element) {
      return;
    }

    const loginUrl = options.loginUrl || '../login_mypage/login.html';
    const signupUrl = options.signupUrl || loginUrl;
    const mypageUrl = options.mypageUrl || '../login_mypage/mypage.html';
    const afterLogoutUrl = options.afterLogoutUrl || loginUrl;
    const displayName = getLoginDisplayName();

    if (displayName) {
      element.innerHTML = `
        <span class="auth-user-name">${escapeHtml(displayName)}님</span>
        <button class="auth-action-btn" type="button" data-auth-action="mypage">마이페이지</button>
        <button class="auth-action-btn auth-logout-btn" type="button" data-auth-action="logout">로그아웃</button>
      `;

      element.querySelector('[data-auth-action="mypage"]').addEventListener('click', function () {
        window.location.href = mypageUrl;
      });
      element.querySelector('[data-auth-action="logout"]').addEventListener('click', function () {
        clearLoginStorage();
        window.location.href = afterLogoutUrl;
      });
      return;
    }

    element.innerHTML = `
      <button class="auth-login-btn" type="button" data-auth-action="login">로그인/회원가입</button>
    `;

    element.querySelector('[data-auth-action="login"]').addEventListener('click', function () {
      window.location.href = signupUrl || loginUrl;
    });
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

  function escapeHtml(value) {
    return String(value)
      .replaceAll('&', '&amp;')
      .replaceAll('<', '&lt;')
      .replaceAll('>', '&gt;')
      .replaceAll('"', '&quot;')
      .replaceAll("'", '&#039;');
  }

  function escapeAttribute(value) {
    return escapeHtml(value).replaceAll('`', '&#096;');
  }

  function normalizeImageUrl(path) {
    if (!path) {
      return '';
    }

    if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('data:')) {
      return path;
    }

    return API_BASE_URL + path;
  }

  function formatPrice(price) {
    const value = Number(price);
    if (!Number.isFinite(value)) {
      return '가격 미정';
    }

    return value.toLocaleString('ko-KR') + '원';
  }

  return {
    API_BASE_URL,
    toSejongEmail,
    getLoginUser,
    getLoginEmail,
    getLoginDisplayName,
    clearLoginStorage,
    renderAuthArea,
    readErrorMessage,
    escapeHtml,
    escapeAttribute,
    normalizeImageUrl,
    formatPrice,
  };
})();
