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
    getLoginEmail,
    readErrorMessage,
    escapeHtml,
    escapeAttribute,
    normalizeImageUrl,
    formatPrice,
  };
})();
