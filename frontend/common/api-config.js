(function () {
  const DEFAULT_API_BASE_URL = 'http://localhost:8080';
  const API_BASE_URL_STORAGE_KEY = 'SEJONG_MARKET_API_BASE_URL';

  function readStoredApiBaseUrl() {
    try {
      return localStorage.getItem(API_BASE_URL_STORAGE_KEY);
    } catch (error) {
      console.warn('API_BASE_URL 저장값을 읽을 수 없습니다.', error);
      return '';
    }
  }

  function normalizeApiBaseUrl(value) {
    return String(value || DEFAULT_API_BASE_URL).replace(/\/$/, '');
  }

  // 실제 배포/원격 테스트 시 localStorage 값을 우선 사용합니다.
  // 예: localStorage.setItem('SEJONG_MARKET_API_BASE_URL', 'https://example.ngrok-free.dev');
  window.SEJONG_MARKET_API_BASE_URL = normalizeApiBaseUrl(
    readStoredApiBaseUrl() || window.SEJONG_MARKET_API_BASE_URL || DEFAULT_API_BASE_URL
  );
})();
