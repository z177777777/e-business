<template>
  <div class="category-page">
    <!-- 顶部导航已移除：不显示返回与首页按钮 -->

    <section class="category-hero">
      <div>
        <h2>{{ categoryName }}精选</h2>
        <p>挑选该分类下的口碑商品，持续更新中</p>
      </div>
      <div class="hero-tags">
        <el-tag v-for="tag in quickTags" :key="tag" effect="plain" class="hero-tag">
          {{ tag }}
        </el-tag>
      </div>
    </section>

    <section v-if="products.length" class="product-grid">
      <div
        v-for="item in products"
        :key="item.id"
        class="product-card is-clickable"
        @click="goProduct(item)"
      >
        <div class="product-cover" :style="{ background: item.bg }"></div>
        <div class="product-info">
          <div class="product-title">{{ item.name }}</div>
          <div class="product-meta">
            <span>{{ item.subtitle }}</span>
            <span>销量 {{ item.sold }}</span>
          </div>
          <div class="product-price">￥{{ item.price }}</div>
        </div>
      </div>
    </section>

    <section v-else class="empty-box">
      <div class="empty-title">该分类暂时没有商品</div>
      <el-button type="primary" color="#ff6a3d" plain @click="router.push('/home')">← 首页</el-button>
    </section>

  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { productList } from "@/data/products";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isAuthed = computed(() => !!authStore.token);

const safeDecode = (value) => {
  try {
    return decodeURIComponent(value || "");
  } catch (error) {
    return value || "";
  }
};

const categoryName = computed(() => safeDecode(route.params.name));

const quickTags = ["新品", "口碑", "热卖", "好评"];

const products = computed(() => {
  if (!categoryName.value) {
    return [];
  }
  return productList.filter((item) => item.category === categoryName.value);
});

const goProduct = (item) => {
  router.push(`/product/${item.id}`);
};
</script>

<style scoped>
.category-page {
  padding: 28px 24px 80px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.category-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}


.category-title {
  font-size: 22px;
  font-weight: 700;
}

.category-sub {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 已删除 .back-btn 与 .header-actions 定义（顶部已移除） */

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

.category-hero {
  background: linear-gradient(135deg, #fff1e6, #ffeef5);
  border-radius: 22px;
  padding: 22px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.category-hero h2 {
  margin: 0 0 6px;
}

.category-hero p {
  margin: 0;
  color: var(--text-secondary);
}

.hero-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.hero-tag {
  border-radius: 999px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.product-card {
  background: #fff;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 12px 30px rgba(17, 24, 39, 0.08);
  display: grid;
  gap: 12px;
}

.product-card.is-clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card.is-clickable:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(16, 24, 40, 0.18);
}

.product-cover {
  height: 160px;
}

.product-info {
  padding: 16px 16px 18px;
  display: grid;
  gap: 8px;
}

.product-title {
  font-weight: 600;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  color: var(--text-secondary);
  font-size: 12px;
  align-items: center;
  gap: 10px;
}

.product-price {
  font-weight: 700;
  font-size: 18px;
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


@media (max-width: 1024px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .category-page {
    padding: 20px 16px 60px;
  }
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
