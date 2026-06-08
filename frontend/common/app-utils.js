window.SejongMarketUtils = (function () {
  const DEFAULT_API_BASE_URL = 'http://localhost:8080';
  const API_BASE_URL_STORAGE_KEY = 'SEJONG_MARKET_API_BASE_URL';
  // 배포 환경에서는 빈 문자열(절대경로)을 사용합니다.
  const SERVICE_BASE_URL = '';
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

  /**
   * 현재 실행 환경이 배포 환경인지 여부를 반환합니다.
   *
   * 배포 환경 판단 기준:
   * - file: 프로토콜 → 로컬 파일 실행 → false
   * - localhost 또는 127.0.0.1 → 로컬 개발 서버 → false
   * - 그 외 호스트명(Netlify, 실제 도메인 등) → true
   *
   * 강제 오버라이드:
   * - window.SEJONG_MARKET_DEPLOY_MODE = true  → 항상 배포 모드
   * - window.SEJONG_MARKET_DEPLOY_MODE = false → 항상 로컬 모드
   */
  function isDeployedEnv() {
    // 전역 플래그로 강제 지정 가능
    if (typeof window.SEJONG_MARKET_DEPLOY_MODE === 'boolean') {
      return window.SEJONG_MARKET_DEPLOY_MODE;
    }

    const protocol = window.location.protocol;
    const hostname = window.location.hostname;

    // file:// 프로토콜은 항상 로컬
    if (protocol === 'file:') {
      return false;
    }

    // localhost / 127.0.0.1 은 로컬 개발 서버
    if (hostname === 'localhost' || hostname === '127.0.0.1' || hostname === '') {
      return false;
    }

    return true;
  }

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

  /**
   * 페이지 URL을 반환합니다.
   *
   * - 로컬 환경(file://, localhost): HTML 파일 상대 경로 반환
   *   예) '../product_detail_ui/product-detail.html'
   * - 배포 환경(Netlify 등): 서비스 절대 경로 반환
   *   예) '/products'
   *
   * @param {string} routeName - PAGE_FILES/SERVICE_PATHS 키
   * @param {object} [params={}] - 쿼리 파라미터 (로컬) 또는 경로 파라미터 (배포)
   */
  function pageUrl(routeName, params = {}) {
    if (isDeployedEnv()) {
      // 배포 환경: 서비스 경로 사용
      let path = SERVICE_PATHS[routeName];
      if (!path) {
        return '#';
      }

      // 경로 파라미터 치환 (예: {id} → 1)
      Object.entries(params).forEach(function ([key, value]) {
        path = path.replace(`{${key}}`, encodeURIComponent(value));
      });

      // 남아있는 경로 파라미터가 없으면 그대로 반환
      // (쿼리스트링이 필요한 파라미터는 별도 처리)
      const remainingParams = {};
      Object.entries(params).forEach(function ([key, value]) {
        if (path.includes(`{${key}}`)) {
          remainingParams[key] = value;
        }
      });

      return appendQueryString(SERVICE_BASE_URL + path, remainingParams);
    }

    // 로컬 환경: HTML 파일 상대 경로 사용
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

  /**
   * 상품 상세 페이지 URL을 반환합니다.
   *
   * - 로컬: product-detail.html?id=1
   * - 배포: /products/1
   */
  function productDetailUrl(productId) {
    if (isDeployedEnv()) {
      return SERVICE_BASE_URL + '/products/' + encodeURIComponent(productId);
    }

    return pageUrl('productDetail', { id: productId });
  }

  /**
   * 채팅방 페이지 URL을 반환합니다.
   *
   * - 로컬: chat.html?roomId=1
   * - 배포: /chatrooms/1
   */
  function chatRoomUrl(roomId) {
    if (isDeployedEnv()) {
      return SERVICE_BASE_URL + '/chatrooms/' + encodeURIComponent(roomId);
    }

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

  const TRANSPARENT_IMAGE =
    'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///ywAAAAAAQABAAACAUwAOw==';
  const remoteImageCache = new Map();

  function isNgrokImageUrl(url) {
    try {
      return new URL(url, window.location.href).hostname.endsWith('.ngrok-free.dev');
    } catch (error) {
      return false;
    }
  }

  function imageSourceAttributes(path, alt, className = '') {
    const source = normalizeImageUrl(path);
    const classAttribute = className ? ` class="${escapeAttribute(className)}"` : '';
    const altAttribute = ` alt="${escapeAttribute(alt || '상품 이미지')}"`;

    if (!source) {
      return `src="${TRANSPARENT_IMAGE}"${altAttribute}${classAttribute}`;
    }

    if (!isNgrokImageUrl(source)) {
      return `src="${escapeAttribute(source)}"${altAttribute}${classAttribute}`;
    }

    return `src="${TRANSPARENT_IMAGE}" data-remote-image-src="${escapeAttribute(source)}"${altAttribute}${classAttribute}`;
  }

  async function resolveRemoteImageUrl(source) {
    const normalizedSource = normalizeImageUrl(source);
    if (!normalizedSource || !isNgrokImageUrl(normalizedSource)) {
      return normalizedSource;
    }

    if (!remoteImageCache.has(normalizedSource)) {
      remoteImageCache.set(
        normalizedSource,
        fetch(normalizedSource)
          .then(function (response) {
            if (!response.ok) {
              throw new Error('이미지를 불러오지 못했습니다.');
            }

            return response.blob();
          })
          .then(function (blob) {
            return URL.createObjectURL(blob);
          })
      );
    }

    return remoteImageCache.get(normalizedSource);
  }

  function hydrateRemoteImages(root = document) {
    root.querySelectorAll('img[data-remote-image-src]').forEach(function (image) {
      const source = image.dataset.remoteImageSrc;
      if (!source || image.dataset.remoteImageLoading === 'true') {
        return;
      }

      image.dataset.remoteImageLoading = 'true';
      resolveRemoteImageUrl(source)
        .then(function (resolvedSource) {
          if (resolvedSource) {
            image.src = resolvedSource;
            image.dataset.resolvedImageSrc = resolvedSource;
          }
        })
        .catch(function (error) {
          console.warn('원격 이미지를 불러오지 못했습니다.', error);
        })
        .finally(function () {
          delete image.dataset.remoteImageLoading;
        });
    });
  }

  function setImageElementSource(image, path) {
    const source = normalizeImageUrl(path);
    if (!image || !source) {
      return;
    }

    if (!isNgrokImageUrl(source)) {
      image.src = source;
      return;
    }

    image.src = TRANSPARENT_IMAGE;
    image.dataset.remoteImageSrc = source;
    hydrateRemoteImages(image.parentElement || document);
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
    isDeployedEnv,
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
    imageSourceAttributes,
    resolveRemoteImageUrl,
    hydrateRemoteImages,
    setImageElementSource,
    formatPrice,
  };
})();
