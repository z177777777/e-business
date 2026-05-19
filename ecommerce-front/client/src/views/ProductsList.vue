<template>
  <div class="products-page">
    <!-- 顶部导航已移除：不显示返回与首页按钮 -->

    <section class="products-list-shell">
      <h2 class="list-title">{{ title }}</h2>
      <div class="product-grid">
        <div v-for="item in items" :key="item.id" class="product-card is-clickable" @click="goProduct(item)">
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
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { fetchProducts } from "@/data/products";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isAuthed = computed(() => !!authStore.token);

const type = computed(() => (route.query.type || "").toString());
const items = ref([]);
const title = computed(() => {
  if (type.value === "hot") return "更多热销";
  if (type.value === "new") return "更多上新";
  return "商品列表";
});

const load = async () => {
  items.value = await fetchProducts({ type: type.value, page: 0, size: 50 });
};

onMounted(load);
watch(type, load);

const handleBack = () => {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  router.push("/home");
};

const goProduct = (item) => {
  router.push(`/product/${item.id}`);
};
</script>

<style scoped>
.products-page { padding: 28px 24px 80px; min-height: 100vh; }
.products-header { /* header removed; keep minimal layout in case used elsewhere */ display:flex; align-items:center; justify-content:space-between; gap:12px; }
/* 已删除 .back-btn 定义（顶部按钮已移除） */
.list-title { margin: 18px 0; font-size: 20px; }
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
