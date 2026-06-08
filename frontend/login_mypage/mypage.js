const {
  API_BASE_URL,
  getLoginEmail,
  pageUrl,
  productDetailUrl,
  normalizeImageUrl,
  formatPrice,
  escapeHtml,
  escapeAttribute,
  readErrorMessage,
  renderAuthArea,
} = window.SejongMarketUtils;
const LOGIN_URL = pageUrl('login');
const profileEditPanel = document.getElementById('profileEditPanel');
const toggleEditProfileBtn = document.getElementById('toggleEditProfileBtn');
const nicknameEditForm = document.getElementById('nicknameEditForm');
const passwordEditForm = document.getElementById('passwordEditForm');
const nicknameInput = document.getElementById('nicknameInput');
const currentPasswordInput = document.getElementById('currentPasswordInput');
const newPasswordInput = document.getElementById('newPasswordInput');
const newPasswordConfirmInput = document.getElementById('newPasswordConfirmInput');

let currentUser = null;

function renderUserInfo(user) {
  currentUser = user;
  document.getElementById('profileNickname').textContent = user.nickname || '이름 없음';
  document.getElementById('profileEmail').textContent = user.email || '-';
  document.getElementById('profileStudentId').textContent = user.studentId || '-';
  document.getElementById('profileDept').textContent = '세종대학교 중고거래';

  if (nicknameInput) {
    nicknameInput.value = user.nickname || '';
  }
}

function renderProducts(products) {
  const list = document.getElementById('productList');
  const emptyState = document.getElementById('emptyState');
  const emptyStateMessage = document.getElementById('emptyStateMessage');
  const statEl = document.getElementById('statProducts');

  statEl.textContent = products.length;
  list.innerHTML = '';

  if (products.length === 0) {
    list.style.display = 'none';
    emptyState.style.display = 'flex';
    emptyStateMessage.textContent = '아직 등록한 상품이 없습니다.';
    return;
  }

  list.style.display = 'flex';
  emptyState.style.display = 'none';

  products.forEach(function (product) {
    list.appendChild(createProductCard(product, { showActions: true }));
  });
}

function renderLikedProducts(products) {
  const list = document.getElementById('likedProductList');
  const emptyState = document.getElementById('likedEmptyState');
  const statEl = document.getElementById('statWishlist');

  statEl.textContent = products.length;
  list.innerHTML = '';

  if (products.length === 0) {
    list.style.display = 'none';
    emptyState.style.display = 'flex';
    return;
  }

  list.style.display = 'flex';
  emptyState.style.display = 'none';

  products.forEach(function (product) {
    list.appendChild(createProductCard(product, { showActions: false }));
  });
}

function getProductTitle(product) {
  return product.title || product.productName || product.name || '';
}

function createProductCard(product, options = {}) {
  const card = document.createElement('div');
  const productId = product.id;
  const status = getProductStatus(product);
  const statusText = getProductStatusText(product);
  const showActions = options.showActions !== false;

  card.className = `product-card${showActions ? '' : ' readonly'}`;
  if (productId) {
    card.tabIndex = 0;
    card.setAttribute('role', 'link');
    card.setAttribute('aria-label', `${getProductTitle(product) || '상품'} 상세 페이지로 이동`);
    card.addEventListener('click', function () {
      window.location.href = productDetailUrl(productId);
    });
    card.addEventListener('keydown', function (event) {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        window.location.href = productDetailUrl(productId);
      }
    });
  }

  card.innerHTML = `
    <div class="product-img-wrap">
      <div class="product-img-placeholder">${renderProductImage(product)}</div>
      <span class="product-status ${escapeAttribute(status)}">${escapeHtml(statusText)}</span>
    </div>
    <div class="product-info">
      <p class="product-title">${escapeHtml(getProductTitle(product) || '제목 없음')}</p>
      <p class="product-price">${formatPrice(product.price)}</p>
      <p class="product-desc">${escapeHtml(product.description || '')}</p>
      <div class="product-meta">
        <span><i class="ti ti-map-pin"></i> ${escapeHtml(product.locationName || '장소 미정')}</span>
        <span><i class="ti ti-clock"></i> ${escapeHtml(product.createdAtText || formatCreatedAt(product.createdAt))}</span>
      </div>
    </div>
    ${showActions ? `
      <div class="product-actions">
        <button class="action-icon-btn" type="button" title="수정" data-action="edit">
          <i class="ti ti-pencil"></i>
        </button>
        <button class="action-icon-btn danger" type="button" title="삭제" data-action="delete">
          <i class="ti ti-trash"></i>
        </button>
      </div>
    ` : ''}
  `;

  if (showActions) {
    card.querySelector('[data-action="edit"]').addEventListener('click', function (event) {
      event.stopPropagation();
      editProduct(product);
    });

    card.querySelector('[data-action="delete"]').addEventListener('click', function (event) {
      event.stopPropagation();
      deleteProduct(productId);
    });
  }

  return card;
}

function getProductStatus(product) {
  if (product.status) {
    return product.status;
  }

  if (product.saleStatus === 'SOLD_OUT') {
    return 'sold-out';
  }

  return 'on-sale';
}

function getProductStatusText(product) {
  if (product.saleStatusText) {
    return product.saleStatusText;
  }

  return getProductStatus(product) === 'sold-out' ? '판매완료' : '판매중';
}

function renderProductImage(product) {
  const imageUrl = normalizeImageUrl(product.imageUrl || product.image);
  if (!imageUrl) {
    return '<i class="ti ti-photo"></i>';
  }

  return `<img src="${escapeAttribute(imageUrl)}" alt="상품 이미지">`;
}

function formatCreatedAt(createdAt) {
  if (!createdAt) {
    return '등록일 미정';
  }

  const date = new Date(createdAt);
  if (Number.isNaN(date.getTime())) {
    return '등록일 미정';
  }

  return date.toLocaleDateString('ko-KR');
}

function showMessage(message) {
  const list = document.getElementById('productList');
  const emptyState = document.getElementById('emptyState');
  const emptyStateMessage = document.getElementById('emptyStateMessage');

  list.style.display = 'none';
  emptyState.style.display = 'flex';
  emptyStateMessage.textContent = message;
}

async function editProduct(product) {
  if (!product || !product.id) {
    alert('상품 정보를 확인할 수 없습니다.');
    return;
  }

  const sellerEmail = getLoginEmail();
  if (!sellerEmail) {
    alert('로그인 정보가 없습니다. 로그인 후 이용해 주세요.');
    window.location.href = LOGIN_URL;
    return;
  }

  const productName = prompt('상품명을 입력해 주세요.', getProductTitle(product));
  if (productName === null) {
    return;
  }

  const category = prompt('카테고리를 입력해 주세요.', product.category || '기타');
  if (category === null) {
    return;
  }

  const priceInput = prompt('가격을 입력해 주세요.', product.price || '');
  if (priceInput === null) {
    return;
  }

  const price = Number(priceInput);
  if (!Number.isInteger(price) || price < 0) {
    alert('가격은 0 이상의 숫자로 입력해 주세요.');
    return;
  }

  const description = prompt('상품 설명을 입력해 주세요.', product.description || '');
  if (description === null) {
    return;
  }

  const tradeMethod = prompt('거래 방식을 입력해 주세요. 예: 직거래, 택배', product.tradeMethod || '직거래');
  if (tradeMethod === null) {
    return;
  }

  const locationNumberInput = prompt('교내 거래 위치 번호를 입력해 주세요. 없으면 비워두세요.', product.locationNumber || '');
  if (locationNumberInput === null) {
    return;
  }

  const locationNumber = locationNumberInput.trim() ? Number(locationNumberInput) : null;
  if (locationNumber !== null && (!Number.isInteger(locationNumber) || locationNumber < 0)) {
    alert('거래 위치 번호는 0 이상의 숫자로 입력해 주세요.');
    return;
  }

  const locationName = prompt('교내 거래 위치명을 입력해 주세요. 없으면 비워두세요.', product.locationName || '');
  if (locationName === null) {
    return;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/products/${encodeURIComponent(product.id)}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        sellerEmail,
        productName: productName.trim(),
        category: category.trim(),
        price,
        description: description.trim(),
        tradeMethod: tradeMethod.trim(),
        locationNumber,
        locationName: locationName.trim(),
      }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    alert('상품 정보가 수정되었습니다.');
    loadMyPage();
  } catch (error) {
    alert(error.message || '상품 수정에 실패했습니다.');
  }
}

async function deleteProduct(id) {
  if (!id) {
    alert('상품 정보를 확인할 수 없습니다.');
    return;
  }

  const sellerEmail = getLoginEmail();
  if (!sellerEmail) {
    alert('로그인 정보가 없습니다. 로그인 후 이용해 주세요.');
    window.location.href = LOGIN_URL;
    return;
  }

  if (!confirm('정말 이 상품을 삭제하시겠습니까?')) {
    return;
  }

  try {
    const response = await fetch(
      `${API_BASE_URL}/api/products/${encodeURIComponent(id)}?sellerEmail=${encodeURIComponent(sellerEmail)}`,
      { method: 'DELETE' }
    );

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    alert('상품이 삭제되었습니다.');
    loadMyPage();
  } catch (error) {
    alert(error.message || '상품 삭제에 실패했습니다.');
  }
}

function updateLoginUserStorage(user) {
  try {
    const previousLoginUser = JSON.parse(localStorage.getItem('loginUser')) || {};
    const nextLoginUser = {
      ...previousLoginUser,
      email: user.email || previousLoginUser.email || '',
      nickname: user.nickname || '',
      studentId: user.studentId || previousLoginUser.studentId || ''
    };

    localStorage.setItem('loginUser', JSON.stringify(nextLoginUser));
    if (nextLoginUser.email) {
      localStorage.setItem('loginEmail', nextLoginUser.email);
    }
  } catch (error) {
    console.warn('로그인 사용자 정보를 갱신하지 못했습니다.', error);
  }
}

function refreshAuthArea() {
  renderAuthArea('authArea', {
    loginUrl: 'login.html',
    signupUrl: 'login.html',
    mypageUrl: 'mypage.html',
    afterLogoutUrl: '../main_ui/index.html'
  });
}

async function updateNickname(event) {
  event.preventDefault();

  const email = getLoginEmail();
  const nickname = nicknameInput.value.trim();
  const submitButton = nicknameEditForm.querySelector('button[type="submit"]');

  if (!email) {
    alert('로그인 정보가 없습니다. 로그인 후 이용해 주세요.');
    window.location.href = LOGIN_URL;
    return;
  }

  if (!nickname) {
    alert('닉네임을 입력해 주세요.');
    nicknameInput.focus();
    return;
  }

  try {
    submitButton.disabled = true;
    submitButton.textContent = '저장 중';

    const response = await fetch(`${API_BASE_URL}/users/me`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        nickname,
      }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    const data = await response.json();
    renderUserInfo(data);
    renderProducts(Array.isArray(data.products) ? data.products : []);
    renderLikedProducts(Array.isArray(data.likedProducts) ? data.likedProducts : []);
    updateLoginUserStorage(data);
    refreshAuthArea();
    alert('닉네임이 수정되었습니다.');
  } catch (error) {
    alert(error.message || '닉네임 수정에 실패했습니다.');
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = '저장';
  }
}

async function updatePassword(event) {
  event.preventDefault();

  const email = getLoginEmail();
  const currentPassword = currentPasswordInput.value;
  const newPassword = newPasswordInput.value;
  const newPasswordConfirm = newPasswordConfirmInput.value;
  const submitButton = passwordEditForm.querySelector('button[type="submit"]');

  if (!email) {
    alert('로그인 정보가 없습니다. 로그인 후 이용해 주세요.');
    window.location.href = LOGIN_URL;
    return;
  }

  if (!currentPassword) {
    alert('현재 비밀번호를 입력해 주세요.');
    currentPasswordInput.focus();
    return;
  }

  if (!newPassword) {
    alert('새 비밀번호를 입력해 주세요.');
    newPasswordInput.focus();
    return;
  }

  if (newPassword !== newPasswordConfirm) {
    alert('새 비밀번호와 확인값이 일치하지 않습니다.');
    newPasswordConfirmInput.focus();
    return;
  }

  try {
    submitButton.disabled = true;
    submitButton.textContent = '수정 중';

    const response = await fetch(`${API_BASE_URL}/users/me/password`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        currentPassword,
        newPassword,
      }),
    });

    if (!response.ok) {
      throw new Error(await readErrorMessage(response));
    }

    currentPasswordInput.value = '';
    newPasswordInput.value = '';
    newPasswordConfirmInput.value = '';
    alert('비밀번호가 수정되었습니다. 다음 로그인부터 새 비밀번호를 사용해 주세요.');
  } catch (error) {
    alert(error.message || '비밀번호 수정에 실패했습니다.');
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = '비밀번호 수정';
  }
}

async function loadMyPage() {
  const email = getLoginEmail();

  if (!email) {
    alert('로그인 정보가 없습니다. 로그인 후 이용해 주세요.');
    window.location.href = LOGIN_URL;
    return;
  }

  try {
    showMessage('마이페이지 정보를 불러오는 중입니다.');

    const response = await fetch(`${API_BASE_URL}/users/me?email=${encodeURIComponent(email)}`);
    if (!response.ok) {
      throw new Error(await response.text());
    }

    const data = await response.json();
    const products = Array.isArray(data.products) ? data.products : [];
    const likedProducts = Array.isArray(data.likedProducts) ? data.likedProducts : [];

    renderUserInfo(data);
    renderProducts(products);
    renderLikedProducts(likedProducts);
  } catch (error) {
    console.error(error);
    renderUserInfo({ email });
    document.getElementById('statProducts').textContent = '0';
    document.getElementById('statWishlist').textContent = '0';
    showMessage(error.message || '마이페이지 정보를 불러오지 못했습니다.');
  }
}

toggleEditProfileBtn.addEventListener('click', function () {
  const isHidden = profileEditPanel.hidden;
  profileEditPanel.hidden = !isHidden;
  toggleEditProfileBtn.innerHTML = isHidden
    ? '<i class="ti ti-x"></i> 닫기'
    : '<i class="ti ti-pencil"></i> 정보 수정';

  if (isHidden && currentUser) {
    nicknameInput.value = currentUser.nickname || '';
  }
});

nicknameEditForm.addEventListener('submit', updateNickname);
passwordEditForm.addEventListener('submit', updatePassword);

loadMyPage();
