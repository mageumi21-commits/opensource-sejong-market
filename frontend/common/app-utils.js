window.SejongMarketUtils = (function () {
  const DEFAULT_API_BASE_URL = 'http://localhost:8080';
  const API_BASE_URL_STORAGE_KEY = 'SEJONG_MARKET_API_BASE_URL';
  const SERVICE_BASE_URL = 'https://sejong-market';
  const PAGE_FILES = {
    home: 'main_ui/index.html',
    products: 'main_ui/product-list.html',
    productNew: 'product_create_ui/product-create-ui.html',
    productDetail: 'product_detail_ui/product-detail.html',
    login: 'login_mypage/login.html',
    signup: 'login_mypage/signup.html',
    findId: 'login_mypage/find-id.html',
    findPassword: 'login_mypage/find-pw.html',
    terms: 'login_mypage/terms.html',
    privacy: 'login_mypage/privacy.html',
    mypage: 'login_mypage/mypage.html',
    chatRoom: 'chat_ui/chat.html',
  };
  const SERVICE_PATHS = {
    home: '/',
    products: '/products',
    productNew: '/products/new',
    productDetail: '/products/{id}',
    login: '/login',
    signup: '/signup',
    findId: '/find-id',
    findPassword: '/find-password',
    terms: '/terms',
    privacy: '/privacy',
    mypage: '/mypage',
    chatRoom: '/chatrooms/{roomId}',
  };
  const LEGACY_ROUTE_MAP = {
    'index.html': 'home',
    '../main_ui/index.html': 'home',
    'product-list.html': 'products',
    '../main_ui/product-list.html': 'products',
    '../product_create_ui/product-create-ui.html': 'productNew',
    'login.html': 'login',
    '../login_mypage/login.html': 'login',
    'signup.html': 'signup',
    '../login_mypage/signup.html': 'signup',
    'find-id.html': 'findId',
    '../login_mypage/find-id.html': 'findId',
    'find-pw.html': 'findPassword',
    '../login_mypage/find-pw.html': 'findPassword',
    'terms.html': 'terms',
    '../login_mypage/terms.html': 'terms',
    'privacy.html': 'privacy',
    '../login_mypage/privacy.html': 'privacy',
    'mypage.html': 'mypage',
    '../login_mypage/mypage.html': 'mypage',
    '../chat_ui/chat.html': 'chatRoom',
  };

  function resolveApiBaseUrl() {
    const params = new URLSearchParams(window.location.search);
    const queryApiBaseUrl = params.get('apiBaseUrl');

    if (queryApiBaseUrl) {
      localStorage.setItem(API_BASE_URL_STORAGE_KEY, queryApiBaseUrl);
      return queryApiBaseUrl.replace(/\/$/, '');
    }

    let storedApiBaseUrl = '';
    try {
      storedApiBaseUrl = localStorage.getItem(API_BASE_URL_STORAGE_KEY) || '';
    } catch (error) {
      console.warn('API_BASE_URL 저장값을 읽을 수 없습니다.', error);
    }

    const configuredApiBaseUrl =
      storedApiBaseUrl ||
      window.SEJONG_MARKET_API_BASE_URL ||
      DEFAULT_API_BASE_URL;

    return configuredApiBaseUrl.replace(/\/$/, '');
  }

  const API_BASE_URL = resolveApiBaseUrl();

  function frontendRootPrefix() {
    const path = window.location.pathname.replaceAll('\\', '/');
    if (
      path.includes('/frontend/') ||
      /\/(main_ui|login_mypage|product_create_ui|product_detail_ui|chat_ui)\//.test(path)
    ) {
      return '../';
    }

    return '';
  }

  function appendQueryString(url, params = {}) {
    const query = new URLSearchParams();
    Object.entries(params).forEach(function ([key, value]) {
      if (value !== undefined && value !== null && value !== '') {
        query.set(key, value);
      }
    });

    const queryString = query.toString();
    return queryString ? `${url}?${queryString}` : url;
  }

  function pageUrl(routeName, params = {}) {
    const file = PAGE_FILES[routeName];
    if (!file) {
      return '#';
    }

    return appendQueryString(frontendRootPrefix() + file, params);
  }

  function serviceUrl(routeName, params = {}) {
    let path = SERVICE_PATHS[routeName] || '/';
    Object.entries(params).forEach(function ([key, value]) {
      path = path.replace(`{${key}}`, encodeURIComponent(value));
    });

    return SERVICE_BASE_URL + path;
  }

  function productDetailUrl(productId) {
    return pageUrl('productDetail', { id: productId });
  }

  function chatRoomUrl(roomId) {
    return pageUrl('chatRoom', { roomId });
  }

  function normalizeInternalLinks() {
    document.querySelectorAll('a[href]').forEach(function (link) {
      const href = link.getAttribute('href');
      if (!href || href.startsWith('#') || href.startsWith('http') || href.startsWith('mailto:')) {
        return;
      }

      const [path, queryString] = href.split('?');
      const routeName = LEGACY_ROUTE_MAP[path];
      if (!routeName) {
        return;
      }

      const nextHref = queryString ? `${pageUrl(routeName)}?${queryString}` : pageUrl(routeName);
      link.setAttribute('href', nextHref);
    });
  }

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

    const loginUrl = options.loginUrl || pageUrl('login');
    const signupUrl = options.signupUrl || loginUrl;
    const mypageUrl = options.mypageUrl || pageUrl('mypage');
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

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', normalizeInternalLinks);
  } else {
    normalizeInternalLinks();
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
    SERVICE_BASE_URL,
    PAGE_FILES,
    SERVICE_PATHS,
    toSejongEmail,
    pageUrl,
    serviceUrl,
    productDetailUrl,
    chatRoomUrl,
    normalizeInternalLinks,
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
