// ===========================
//  세종마켓 — 마이페이지 스크립트 (mypage.js)
// ===========================

// ── 샘플 사용자 데이터 (백엔드 연동 전 임시) ──
// 실제 서비스: fetch('/api/user/me') 로 받아와요
const userData = {
  nickname: '홍길동',
  email: 'hong1234@sju.ac.kr',
  studentId: '23011234',
  college: '소프트웨어융합대학',
  dept: '컴퓨터공학과'
};

// ── 샘플 상품 데이터 (백엔드 연동 전 임시) ──
// 실제 서비스: fetch('/api/products/my') 로 받아와요
const myProducts = [
  {
    id: 1,
    title: '전공책 자료구조 팝니다',
    price: 15000,
    desc: '한 학기 사용했고 상태 양호합니다. 직거래 선호해요.',
    location: '광개토관',
    time: '1시간 전',
    status: 'on-sale',    // 'on-sale' | 'sold-out'
    image: null           // 이미지 URL (없으면 null)
  },
  {
    id: 2,
    title: '아이패드 거치대 팝니다',
    price: 8000,
    desc: '사용감 거의 없어요. 자취방 정리하면서 내놓습니다.',
    location: '학생회관',
    time: '2일 전',
    status: 'sold-out',
    image: null
  }
];

// ===========================
//  사용자 정보 렌더링
// ===========================

function renderUserInfo(user) {
  document.getElementById('profileNickname').textContent = user.nickname;
  document.getElementById('profileEmail').textContent    = user.email;
  document.getElementById('profileStudentId').textContent = user.studentId;
  document.getElementById('profileDept').textContent    = user.college + ' · ' + user.dept;
}

// ===========================
//  상품 목록 렌더링
// ===========================

function renderProducts(products) {
  const list       = document.getElementById('productList');
  const emptyState = document.getElementById('emptyState');
  const statEl     = document.getElementById('statProducts');

  // 통계 업데이트
  statEl.textContent = products.length;

  // 상품이 없으면 빈 상태 표시
  if (products.length === 0) {
    list.style.display       = 'none';
    emptyState.style.display = 'flex';
    return;
  }

  list.style.display       = 'flex';
  emptyState.style.display = 'none';
  list.innerHTML = '';  // 기존 내용 초기화

  products.forEach(function (product) {
    const card = document.createElement('div');
    card.className = 'product-card';

    // 이미지 또는 placeholder
    const imgContent = product.image
      ? '<img src="' + product.image + '" alt="상품 이미지">'
      : '<i class="ti ti-photo"></i>';

    // 상태 뱃지
    const statusLabel = product.status === 'on-sale' ? '판매중' : '판매완료';

    card.innerHTML = `
      <div class="product-img-wrap">
        <div class="product-img-placeholder">${imgContent}</div>
        <span class="product-status ${product.status}">${statusLabel}</span>
      </div>
      <div class="product-info">
        <p class="product-title">${product.title}</p>
        <p class="product-price">${product.price.toLocaleString()}원</p>
        <p class="product-desc">${product.desc}</p>
        <div class="product-meta">
          <span><i class="ti ti-map-pin"></i> ${product.location}</span>
          <span><i class="ti ti-clock"></i> ${product.time}</span>
        </div>
      </div>
      <div class="product-actions">
        <button class="action-icon-btn" title="수정" onclick="editProduct(${product.id})">
          <i class="ti ti-pencil"></i>
        </button>
        <button class="action-icon-btn danger" title="삭제" onclick="deleteProduct(${product.id})">
          <i class="ti ti-trash"></i>
        </button>
      </div>
    `;

    list.appendChild(card);
  });
}

// ===========================
//  수정 / 삭제 (백엔드 연동 전 임시)
// ===========================

function editProduct(id) {
  // 실제 서비스: 상품 수정 페이지로 이동
  // window.location.href = 'product-edit.html?id=' + id;
  alert('상품 수정 기능은 준비 중이에요. (상품 ID: ' + id + ')');
}

function deleteProduct(id) {
  if (!confirm('정말 삭제하시겠어요?')) return;

  // 실제 서비스: fetch('/api/products/' + id, { method: 'DELETE' })
  // 삭제 후 목록 새로고침

  // 데모: 목록에서 제거
  const idx = myProducts.findIndex(function (p) { return p.id === id; });
  if (idx !== -1) {
    myProducts.splice(idx, 1);
    renderProducts(myProducts);
  }
}

// ===========================
//  초기화
// ===========================

// 실제 서비스에서는 아래처럼 API 호출 후 렌더링해요
// fetch('/api/user/me')
//   .then(res => res.json())
//   .then(data => renderUserInfo(data));
//
// fetch('/api/products/my')
//   .then(res => res.json())
//   .then(data => renderProducts(data));

// 지금은 샘플 데이터로 렌더링
renderUserInfo(userData);
renderProducts(myProducts);
