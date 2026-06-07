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
    list.appendChild(createProductCard(product));
  });
}

function getProductTitle(product) {
  return product.title || product.productName || product.name || '';
}

function createProductCard(product) {
  const card = document.createElement('div');
  const productId = product.id;
  const status = getProductStatus(product);
  const statusText = getProductStatusText(product);

  card.className = 'product-card';
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
    <div class="product-actions">
      <button class="action-icon-btn" type="button" title="수정" data-action="edit">
        <i class="ti ti-pencil"></i>
      </button>
      <button class="action-icon-btn danger" type="button" title="삭제" data-action="delete">
        <i class="ti ti-trash"></i>
      </button>
    </div>
  `;

  card.querySelector('[data-action="edit"]').addEventListener('click', function (event) {
    event.stopPropagation();
    editProduct(product);
  });

  card.querySelector('[data-action="delete"]').addEventListener('click', function (event) {
    event.stopPropagation();
    deleteProduct(productId);
  });

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

    renderUserInfo(data);
    renderProducts(products);
  } catch (error) {
    console.error(error);
    renderUserInfo({ email });
    document.getElementById('statProducts').textContent = '0';
    showMessage(error.message || '마이페이지 정보를 불러오지 못했습니다.');
  }
}

loadMyPage();
