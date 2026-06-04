<template>
  <div class="product-page">
    <section v-if="product" class="product-shell">
      <div class="product-hero">
        <div class="product-cover" :style="getCoverStyle(product)">
          <div v-if="product.tag" class="cover-badge">{{ product.tag }}</div>
        </div>
        <div class="product-info">
          <div class="product-category">{{ product.category }}</div>
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-sub">{{ product.subtitle }}</div>
          <div class="price-row">￥{{ formatPrice(product.price) }}</div>
          <div class="meta-row">
            <span v-if="product.sold">销量 {{ product.sold }}</span>
            <span>48小时内发货</span>
          </div>
          <p class="product-desc">{{ product.description }}</p>
          <div class="highlight-grid">
            <div v-for="(item, index) in product.highlights" :key="index" class="highlight-item">
              {{ item }}
            </div>
          </div>
          <div v-if="product.stock != null && product.stock <= 0" style="margin-top:8px;color:#e03131;font-size:13px">暂时缺货</div>
          <div class="action-row">
            <el-button v-if="!isOutOfStock" type="primary" color="#ff6a3d" :loading="addingCart" @click="handleAddCart">加入购物车</el-button>
            <el-button v-else disabled>已售罄</el-button>
            <el-button v-if="isOutOfStock" type="warning" plain :loading="requestingRestock" @click="handleRequestRestock">催上货</el-button>
            <el-button :type="favorited ? 'warning' : ''" :icon="favorited ? StarFilled : Star" :loading="togglingFav" @click="handleToggleFav" plain>
              {{ favorited ? '取消收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="detail-panels">
        <div class="detail-card">
          <h3>推荐理由</h3>
          <p>围绕日常使用与质感体验挑选，兼顾颜值、实用与口碑。</p>
        </div>
        <div class="detail-card">
          <h3>发货与售后</h3>
          <p>官方仓库发货，支持7天无理由退换，售后无忧。</p>
        </div>
      </div>

      <div class="reviews-card">
        <div class="reviews-head">
          <div>
            <h3>商品评价</h3>
            <p>普通用户可查看评价，已收货用户可发布评价。</p>
          </div>
          <el-tag v-if="canReview" type="success" effect="dark">已解锁评价</el-tag>
          <el-tag v-else type="info" effect="plain">收货后可评价</el-tag>
        </div>

        <div v-if="canReview" class="review-form">
          <div class="review-form-title">写评价</div>
          <el-rate v-model="reviewForm.rating" :max="5" />
          <el-input
            v-model="reviewForm.content"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="分享你的真实使用感受"
          />
          <div style="margin-top:8px">
            <el-upload :http-request="handleImageUpload" :show-file-list="false" multiple>
              <el-button size="small">上传图片</el-button>
            </el-upload>
            <div style="display:flex; gap:8px; margin-top:8px">
              <div v-for="(img, idx) in reviewForm.images" :key="idx" style="position:relative">
                <img :src="img" style="width:96px;height:96px;object-fit:cover;border:1px solid #eee;border-radius:4px" />
                <el-button size="mini" type="danger" style="position:absolute;right:4px;top:4px" @click="removeReviewImage(idx)">删除</el-button>
              </div>
            </div>
          </div>
          <div class="review-form-actions">
            <el-button type="primary" color="#ff6a3d" :loading="submittingReview" @click="handleSubmitReview">发布评价</el-button>
          </div>
        </div>

        <el-empty v-if="!reviewLoading && !reviews.length" description="暂无评价" />
        <div v-else class="review-list">
          <div v-for="item in reviews" :key="item.id" class="review-item">
            <div class="review-user">
              <div class="review-avatar">{{ item.nickname ? item.nickname.slice(0, 1) : '匿' }}</div>
              <div class="review-user-info">
                <div class="review-nickname">{{ item.nickname || '匿名用户' }}</div>
                <div class="review-time">{{ formatTime(item.createdAt) }}</div>
              </div>
              <el-rate :model-value="item.rating" disabled show-score text-color="#ff9900" score-template="{value}分" />
            </div>
            <div class="review-content">{{ item.content }}</div>
            <div v-if="item.imageUrls && item.imageUrls.length" style="display:flex; gap:8px; flex-wrap:wrap">
              <img
                v-for="(img, idx) in item.imageUrls"
                :key="idx"
                :src="img"
                style="width:80px;height:80px;object-fit:cover;border:1px solid #eee;border-radius:4px;cursor:pointer"
                @click="previewReviewImage(img)"
              />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section v-else-if="!loading" class="empty-box">
      <div class="empty-title">未找到该商品</div>
      <el-button type="primary" color="#ff6a3d" plain @click="router.push('/home')">← 首页</el-button>
    </section>
    <!-- 加载中保持空白（不渲染未找到提示） -->
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/store/auth";
import { fetchProductById } from "@/data/products";
import { addCartItem } from "@/api/cart";
import { toggleFavorite, isFavorited as checkFavorited } from "@/api/favorites";
import { Star, StarFilled } from "@element-plus/icons-vue";
import { uploadImage } from "@/api/files";
import { listProductReviews, submitProductReview } from "@/api/reviews";
import http from "@/api/http";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const productId = String(route.params.id || "");
const product = ref(null);
const loading = ref(true);
const addingCart = ref(false);
const requestingRestock = ref(false);
const favorited = ref(false);
const togglingFav = ref(false);
const reviews = ref([]);
const reviewLoading = ref(false);
const canReview = ref(false);
const submittingReview = ref(false);
const reviewForm = ref({ rating: 5, content: "", images: [] });

const isOutOfStock = computed(() => {
  const p = product.value;
  return p && p.stock != null && p.stock <= 0;
});

const getCoverStyle = (item) => {
  const img = item?.image || item?.coverUrl;
  if (img) {
    return { background: `url(${img}) center/cover no-repeat, #fff` };
  }
  return { background: "#fff" };
};

const formatPrice = (p) => {
  if (p == null) return "0";
  const n = Number(p);
  if (Number.isInteger(n)) return n.toString();
  return n.toFixed(2);
};

onMounted(async () => {
  loading.value = true;
  try {
    product.value = await fetchProductById(productId);
    favorited.value = checkFavorited(product.value?.id);
  } catch (e) {
    product.value = null;
  } finally {
    loading.value = false;
  }

  await loadReviews();
});

const loadReviews = async () => {
  reviewLoading.value = true;
  try {
    const resp = await listProductReviews(productId);
    const data = resp.data || {};
    reviews.value = Array.isArray(data.reviews) ? data.reviews : [];
    canReview.value = !!data.canReview;
  } catch (e) {
    reviews.value = [];
    canReview.value = false;
  } finally {
    reviewLoading.value = false;
  }
};

const handleAddCart = async () => {
  if (!authStore.token) {
    router.push("/login");
    return;
  }
  addingCart.value = true;
  try {
    await addCartItem({
      productIdentifier: productId,
      quantity: 1
    });
    ElMessage.success("已加入购物车");
  } finally {
    addingCart.value = false;
  }
};

const handleRequestRestock = async () => {
  if (!authStore.token) {
    router.push("/login");
    return;
  }
  requestingRestock.value = true;
  try {
    await http.post(`/api/products/${productId}/request-restock`);
    ElMessage.success("已提交催上货请求");
  } catch (e) {
    ElMessage.error("操作失败");
  } finally {
    requestingRestock.value = false;
  }
};

const handleToggleFav = async () => {
  if (!product.value) return;
  togglingFav.value = true;
  try {
    const added = toggleFavorite({
      id: product.value.id,
      name: product.value.name,
      price: product.value.price,
      subtitle: product.value.subtitle,
      image: product.value.image || product.value.coverUrl,
      category: product.value.category
    });
    favorited.value = added;
    ElMessage.success(added ? "已加入收藏" : "已取消收藏");
  } finally {
    togglingFav.value = false;
  }
};

const formatTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString();
};

const handleImageUpload = async (options) => {
  const file = options.file;
  try {
    const res = await uploadImage(file);
    const url = res.data;
    reviewForm.value.images.push(url);
    ElMessage.success("图片上传成功");
  } catch (e) {
    ElMessage.error("图片上传失败");
  }
};

const removeReviewImage = (index) => {
  reviewForm.value.images.splice(index, 1);
};

const previewReviewImage = (url) => {
  window.open(url, "_blank");
};

const handleSubmitReview = async () => {
  if (!canReview.value) {
    ElMessage.warning("只有已收货用户才能发布评价");
    return;
  }
  const content = String(reviewForm.value.content || "").trim();
  if (!content) {
    ElMessage.warning("请输入评价内容");
    return;
  }
  submittingReview.value = true;
  try {
    await submitProductReview(productId, {
      rating: reviewForm.value.rating,
      content,
      imageUrls: reviewForm.value.images
    });
    ElMessage.success("评价已发布");
    reviewForm.value.content = "";
    reviewForm.value.rating = 5;
    reviewForm.value.images = [];
    await loadReviews();
  } finally {
    submittingReview.value = false;
  }
};
</script>

<style scoped>
.product-page {
  min-height: 100vh;
  padding: 28px 24px 80px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.text-link {
  color: var(--text-secondary);
  font-weight: 500;
}

.primary-link {
  padding: 8px 16px;
  border-radius: 999px;
  background: #1c1c1e;
  color: #fff;
  font-weight: 600;
}

.product-shell {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-hero {
  display: grid;
  grid-template-columns: minmax(280px, 420px) 1fr;
  gap: 28px;
  align-items: stretch;
}

.product-cover {
  min-height: 320px;
  border-radius: 28px;
  position: relative;
  overflow: hidden;
  box-shadow: var(--shadow-soft);
}

.cover-badge {
  position: absolute;
  top: 18px;
  left: 18px;
  padding: 6px 14px;
  border-radius: 999px;
  background: rgba(28, 28, 30, 0.65);
  color: #fff;
  font-size: 12px;
  letter-spacing: 0.6px;
}

.product-info {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 28px;
  padding: 28px 30px;
  box-shadow: var(--shadow-soft);
  display: grid;
  gap: 12px;
}

.product-category {
  color: var(--text-secondary);
  font-size: 12px;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.product-title {
  margin: 0;
  font-size: 30px;
}

.product-sub {
  color: var(--text-secondary);
  font-size: 14px;
}

.price-row {
  font-size: 28px;
  font-weight: 700;
  color: #ff6a3d;
}

.meta-row {
  display: flex;
  gap: 16px;
  color: var(--text-secondary);
  font-size: 12px;
}

.product-desc {
  margin: 0;
  line-height: 1.7;
}

.highlight-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
}

.highlight-item {
  background: #f5f7fb;
  border-radius: 12px;
  padding: 8px 12px;
  font-size: 12px;
}

.action-row {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.detail-panels {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.reviews-card {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 22px;
  padding: 22px 24px;
  box-shadow: var(--shadow-soft);
  display: grid;
  gap: 18px;
}

.reviews-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
}

.reviews-head h3 {
  margin: 0;
  font-size: 18px;
}

.reviews-head p {
  margin: 6px 0 0;
  color: var(--text-secondary);
  font-size: 13px;
}

.review-form {
  display: grid;
  gap: 12px;
  padding: 16px;
  border-radius: 18px;
  background: #f8fafc;
}

.review-form-title {
  font-weight: 600;
}

.review-form-actions {
  display: flex;
  justify-content: flex-end;
}

.review-list {
  display: grid;
  gap: 14px;
}

.review-item {
  padding: 14px 16px;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  display: grid;
  gap: 10px;
  background: #fff;
}

.review-user {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.review-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff9f6e, #ff6a3d);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
}

.review-user-info {
  min-width: 0;
  flex: 1;
}

.review-nickname {
  font-weight: 600;
}

.review-time {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.review-content {
  line-height: 1.7;
  color: #374151;
}

.detail-card {
  background: #fff;
  border-radius: 22px;
  padding: 20px 24px;
  box-shadow: var(--shadow-soft);
}

.detail-card h3 {
  margin: 0 0 10px;
  font-size: 18px;
}

.detail-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

.empty-box {
  background: #fff;
  border-radius: 18px;
  padding: 32px;
  text-align: center;
  display: grid;
  gap: 12px;
  box-shadow: var(--shadow-soft);
}

.empty-title {
  font-weight: 600;
}

@media (max-width: 1100px) {
  .product-hero {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .detail-panels {
    grid-template-columns: 1fr;
  }

  .reviews-head {
    flex-direction: column;
  }
}

@media (max-width: 640px) {
  .product-page {
    padding: 20px 16px 60px;
  }
  .product-info {
    padding: 22px;
  }
}
</style>
