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
} = window.SejongMarketUtils;
const LOGIN_URL = pageUrl('login');

function renderUserInfo(user) {
  document.getElementById('profileNickname').textContent = user.nickname || '이름 없음';
  document.getElementById('profileEmail').textContent = user.email || '-';
  document.getElementById('profileStudentId').textContent = user.studentId || '-';
  document.getElementById('profileDept').textContent = '세종대학교 중고거래';
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

  window.location.href = pageUrl('productNew', {
    mode: 'edit',
    id: product.id,
  });
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

loadMyPage();
