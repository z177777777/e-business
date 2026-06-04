<template>
  <div class="products-page">
    <section class="products-list-shell">
      <div class="list-head">
        <h2 class="list-title">{{ title }}</h2>
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
      </div>
      <div class="product-grid">
        <div v-for="item in displayItems" :key="item.id" class="product-card is-clickable" @click="goProduct(item)">
          <div class="product-cover" :style="getCoverStyle(item)"></div>
          <div class="product-info">
            <div class="product-title">{{ item.name }}</div>
            <div class="product-meta">
              <span>{{ item.subtitle }}</span>
              <span>销量 {{ item.sold }}</span>
            </div>
            <div class="product-price">￥{{ formatPrice(item.price) }}</div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { fetchProducts, categoryKeywordMap } from "@/data/products";

const route = useRoute();
const router = useRouter();

const type = computed(() => (route.query.type || "").toString());
const q = computed(() => (route.query.q || "").toString());
const items = ref([]);
const sortBy = ref("");

const displayItems = computed(() => {
  const list = [...items.value];
  if (!sortBy.value) return items.value;
  if (sortBy.value === "price-asc") return list.sort((a, b) => (Number(a.price) || 0) - (Number(b.price) || 0));
  if (sortBy.value === "price-desc") return list.sort((a, b) => (Number(b.price) || 0) - (Number(a.price) || 0));
  if (sortBy.value === "sold-desc") return list.sort((a, b) => (Number(b.sold) || 0) - (Number(a.sold) || 0));
  if (sortBy.value === "sold-asc") return list.sort((a, b) => (Number(a.sold) || 0) - (Number(b.sold) || 0));
  return items.value;
});

const togglePriceSort = () => {
  sortBy.value = sortBy.value === "price-asc" ? "price-desc" : "price-asc";
};

const toggleSoldSort = () => {
  sortBy.value = sortBy.value === "sold-desc" ? "sold-asc" : "sold-desc";
};
const getCoverStyle = (product) => {
  const img = product?.image || product?.coverUrl;
  if (img) {
    return { background: `url(${img}) center/cover no-repeat, #fff` };
  }
  return { background: "#fff" };
};
const title = computed(() => {
  if (q.value) return `搜索：${q.value}`;
  if (type.value === "hot") return "更多热销";
  if (type.value === "new") return "更多上新";
  return "商品列表";
});

const formatPrice = (p) => {
  if (p == null) return "0";
  const n = Number(p);
  if (Number.isInteger(n)) return n.toString();
  return n.toFixed(2);
};

const load = async () => {
  // 当带 q 搜索时，优先从后端获取商品列表（较大分页），再在客户端筛选
  if (q.value) {
    const kw = q.value.trim().toLowerCase();
    if (!kw) {
      items.value = [];
      return;
    }
    const all = await fetchProducts({ page: 0, size: 200 }).catch(() => []);
    const matchedKey = Object.keys(categoryKeywordMap).find((k) => {
      const key = k.toString().toLowerCase();
      return kw === key || kw.includes(key) || key.includes(kw);
    });
    if (matchedKey) {
      const mappedCategory = categoryKeywordMap[matchedKey];
      items.value = all.filter((p) => {
        const inCategory = (String(p.category || "").toLowerCase() === mappedCategory.toLowerCase());
        const text = `${p.name} ${p.subtitle || ""} ${p.description || ""}`.toLowerCase();
        return inCategory || text.includes(kw);
      });
      return;
    }
    items.value = all.filter((p) => {
      const text = `${p.name} ${p.subtitle || ""} ${p.description || ""}`.toLowerCase();
      return text.includes(kw);
    });
    return;
  }

  // 非搜索，根据 type 向后端请求（hot/new 或全部）
  const data = await fetchProducts({ type: type.value, page: 0, size: 50 }).catch(() => []);
  items.value = Array.isArray(data) ? data : [];
};

onMounted(load);
watch(() => route.query, load);

const goProduct = (item) => {
  router.push(`/product/${item.id}`);
};
</script>

<style scoped>
.products-page { padding: 28px 24px 80px; min-height: 100vh; }
.list-head { display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:12px; margin-bottom:8px; }
.list-title { margin: 0; font-size: 20px; }
.sort-bar { display:flex; align-items:center; gap:4px; background:#f5f5f5; border-radius:8px; padding:3px; }
.sort-item { padding:6px 14px; font-size:13px; color:#666; cursor:pointer; border-radius:6px; transition:all .15s; user-select:none; display:flex; align-items:center; gap:4px; }
.sort-item:hover { color:#333; }
.sort-item.active { background:#fff; color:#e4393c; font-weight:600; box-shadow:0 1px 4px rgba(0,0,0,0.08); }

.sort-price .price-arrows { display:inline-flex; flex-direction:column; line-height:1; font-size:8px; margin-left:2px; }
.sort-price .arrow { font-style:normal; color:#ccc; }
.sort-price .arrow.on { color:#e4393c; }
.sort-price.active .arrow { color:#999; }
.sort-price.active .arrow.on { color:#e4393c; }
.product-grid { display:grid; grid-template-columns: repeat(3, 1fr); gap: 18px; }
.product-card { background:#fff; border-radius:18px; overflow:hidden; box-shadow:0 12px 30px rgba(17,24,39,0.08); display:grid; gap:12px; cursor:pointer }
.product-cover { height:160px }
.product-info { padding:16px }
.product-title { font-weight:600 }
.product-meta { color:var(--text-secondary); font-size:12px; display:flex; justify-content:space-between }
.product-price { font-weight:700; font-size:18px }
@media (max-width:1024px){ .product-grid{ grid-template-columns:repeat(2,1fr);} }
@media (max-width:640px){ .product-grid{ grid-template-columns:1fr; } }
</style>
