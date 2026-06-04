<template>
  <div class="category-page">
    <section class="category-hero">
      <div>
        <h2>{{ categoryName }}精选</h2>
        <p>挑选该分类下的口碑商品，持续更新中</p>
      </div>
    </section>

    <div class="sort-bar">
      <span :class="['sort-item', { active: sortBy === '' }]" @click="sortBy = ''">综合</span>
      <span :class="['sort-item sort-price', { active: sortBy.startsWith('sold') }]" @click="toggleSoldSort">
        销量
        <span class="price-arrows">
          <i :class="['arrow up', { on: sortBy === 'sold-asc' }]">▲</i>
          <i :class="['arrow down', { on: sortBy === 'sold-desc' }]">▼</i>
        </span>
      </span>
      <span :class="['sort-item sort-price', { active: sortBy.startsWith('price') }]" @click="togglePriceSort">
        价格
        <span class="price-arrows">
          <i :class="['arrow up', { on: sortBy === 'price-asc' }]">▲</i>
          <i :class="['arrow down', { on: sortBy === 'price-desc' }]">▼</i>
        </span>
      </span>
    </div>

    <section v-if="products.length" class="product-grid">
      <div
        v-for="item in displayItems"
        :key="item.id"
        class="product-card is-clickable"
        @click="goProduct(item)"
      >
        <div class="product-cover" :style="getCoverStyle(item)"></div>
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
import { fetchProducts } from "@/data/products";
import { ref, onMounted } from "vue";

const route = useRoute();
const router = useRouter();

const safeDecode = (value) => {
  try {
    return decodeURIComponent(value || "");
  } catch (error) {
    return value || "";
  }
};

const categoryName = computed(() => safeDecode(route.params.name));

const products = ref([]);
const sortBy = ref("");

const displayItems = computed(() => {
  const list = [...products.value];
  if (!sortBy.value) return products.value;
  if (sortBy.value === "price-asc") return list.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0));
  if (sortBy.value === "price-desc") return list.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0));
  if (sortBy.value === "sold-desc") return list.sort((a, b) => (Number(b.sold) || 0) - (Number(a.sold) || 0));
  if (sortBy.value === "sold-asc") return list.sort((a, b) => (Number(a.sold) || 0) - (Number(b.sold) || 0));
  return products.value;
});

const getCoverStyle = (item) => {
  const img = item?.image || item?.coverUrl;
  if (img) {
    return { background: `url(${img}) center/cover no-repeat, #fff` };
  }
  return { background: "#fff" };
};

const togglePriceSort = () => {
  sortBy.value = sortBy.value === "price-asc" ? "price-desc" : "price-asc";
};

const toggleSoldSort = () => {
  sortBy.value = sortBy.value === "sold-desc" ? "sold-asc" : "sold-desc";
};

const loadCategory = async () => {
  if (!categoryName.value) {
    products.value = [];
    return;
  }
  const all = await fetchProducts({ page: 0, size: 200 }).catch(() => []);
  products.value = Array.isArray(all) ? all.filter((item) => item.category === categoryName.value) : [];
};

onMounted(loadCategory);

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

.sort-bar { display:flex; align-items:center; gap:4px; background:#f5f5f5; border-radius:8px; padding:3px; width:fit-content; }
.sort-item { padding:6px 14px; font-size:13px; color:#666; cursor:pointer; border-radius:6px; transition:all .15s; user-select:none; display:flex; align-items:center; gap:4px; }
.sort-item:hover { color:#333; }
.sort-item.active { background:#fff; color:#e4393c; font-weight:600; box-shadow:0 1px 4px rgba(0,0,0,0.08); }
.sort-price .price-arrows { display:inline-flex; flex-direction:column; line-height:1; font-size:8px; margin-left:2px; }
.sort-price .arrow { font-style:normal; color:#ccc; }
.sort-price .arrow.on { color:#e4393c; }
.sort-price.active .arrow { color:#999; }
.sort-price.active .arrow.on { color:#e4393c; }

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
