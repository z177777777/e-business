<template>
  <div class="product-page">
    <!-- 顶部导航已移除：不显示返回与首页按钮 -->

    <section v-if="product" class="product-shell">
      <div class="product-hero">
        <div class="product-cover" :style="{ background: product.bg }">
          <div v-if="product.tag" class="cover-badge">{{ product.tag }}</div>
        </div>
        <div class="product-info">
          <div class="product-category">{{ product.category }}</div>
          <h1 class="product-title">{{ product.name }}</h1>
          <div class="product-sub">{{ product.subtitle }}</div>
          <div class="price-row">￥{{ product.price }}</div>
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
          <div class="action-row">
            <el-button type="primary" color="#ff6a3d" :loading="addingCart" @click="handleAddCart">加入购物车</el-button>
            <el-button plain>收藏</el-button>
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

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isAuthed = computed(() => !!authStore.token);

const productId = String(route.params.id || "");
const product = ref(null);
const loading = ref(true);
const addingCart = ref(false);

onMounted(async () => {
  loading.value = true;
  try {
    product.value = await fetchProductById(productId);
  } catch (e) {
    product.value = null;
  } finally {
    loading.value = false;
  }
});

const handleBack = () => {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  router.push("/home");
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
