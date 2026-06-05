<template>
  <div class="home-page">
    <header class="home-header">
      <div class="brand">
        <div class="brand-mark">SP</div>
        <div>
          <div class="brand-name">ShopPulse</div>
          <div class="brand-sub">轻量商城 · 精选好物</div>
        </div>
      </div>
      <div class="search-box red-frame">
        <el-input v-model="search" placeholder="搜索商品、品牌、分类" class="search-input">
          <template #prefix>
            <span class="search-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M21 21l-4.35-4.35" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
                <path d="M10.5 18a7.5 7.5 0 1 1 0-15 7.5 7.5 0 0 1 0 15z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
              </svg>
            </span>
          </template>
          <!-- 相机按钮已移除 -->
          <template #append>
            <el-button type="primary" color="#e60012" @click="handleSearch" class="jd-search-btn" aria-label="搜索">搜索</el-button>
          </template>
        </el-input>
        <div class="hot-keys">
          <span class="hot-title">热搜</span>
          <el-tag
            v-for="item in hotKeywords"
            :key="item"
            size="small"
            effect="plain"
            class="hot-tag"
            @click="onHotClick(item)"
            tabindex="0"
            @keyup.enter="onHotClick(item)"
            style="cursor:pointer"
          >
            {{ item }}
          </el-tag>
        </div>
      </div>
      <div class="header-actions">
        <template v-if="!isAuthed">
          <router-link to="/login" class="text-link">登录</router-link>
          <router-link to="/register" class="text-link">注册</router-link>
        </template>
        <router-link v-else to="/profile" class="avatar-link" title="我的资料">
          <img
            v-if="authStore.user?.avatarUrl"
            :src="authStore.user.avatarUrl"
            alt="avatar"
            class="header-avatar-img"
          />
          <span v-else class="header-avatar-placeholder">{{ (authStore.user?.nickname || '用').slice(0, 1) }}</span>
        </router-link>
      </div>
    </header>

    <section class="hero">
      <div class="hero-carousel">
        <el-carousel height="320px" indicator-position="outside">
          <el-carousel-item v-for="slide in heroSlides" :key="slide.title">
            <div
              class="banner is-clickable"
              :style="{ background: slide.bg }"
              role="button"
              tabindex="0"
              @click="goCategory(slide.category)"
              @keyup.enter="goCategory(slide.category)"
              @keyup.space.prevent="goCategory(slide.category)"
            >
              <div class="banner-info">
                <span class="banner-tag">{{ slide.tag }}</span>
                <h2>{{ slide.title }}</h2>
                <p>{{ slide.subtitle }}</p>
                <div v-if="slide.highlight" class="banner-meta">
                  <span>{{ slide.highlight }}</span>
                </div>
              </div>
              <div class="banner-showcase">
                <div
                  v-for="(product, index) in slide.products"
                  :key="product.id"
                  class="hero-product-card"
                  :style="{ ...getCoverStyle(product), animationDelay: `${index * 0.16}s` }"
                >
                  <div class="hero-product-glow"></div>
                  <div class="hero-badge" v-if="product.tag">{{ product.tag }}</div>
                  <div class="hero-product-media">
                    <div class="hero-product-image" :style="getHeroImageStyle(product)"></div>
                    <div class="hero-product-pedestal"></div>
                  </div>
                  <div class="hero-product-name">{{ product.name }}</div>
                  <div class="hero-product-price">￥{{ formatPrice(product.price) }}</div>
                </div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
    </section>

    <section class="category-grid">
      <div
        v-for="category in categories"
        :key="category.name"
        class="category-card is-clickable"
        @click="goCategory(category.name)"
      >
        <div class="category-icon" :style="{ background: category.bg }">{{ category.short }}</div>
        <div class="category-name">{{ category.name }}</div>
        <div class="category-desc">{{ category.desc }}</div>
      </div>
    </section>

    <section class="section-block">
      <div class="section-head">
        <div>
          <h3>热门商品</h3>
          <p>本周热销排行，口碑爆款一网打尽</p>
        </div>
          <el-button type="primary" color="#ff6a3d" plain @click="goMore('hot')">更多热销</el-button>
      </div>
      <div class="product-row">
        <div
          v-for="item in hotProductsDisplay"
          :key="item.id"
          class="product-card is-clickable"
          @click="goProduct(item)"
        >
          <div class="product-cover" :style="getCoverStyle(item)"></div>
          <div class="product-info">
            <div class="product-title">{{ item.name }}</div>
            <div class="product-meta">
              <span>销量 {{ item.sold }}</span>
              <el-tag v-if="item.tag" size="small" effect="dark" type="danger">{{ item.tag }}</el-tag>
            </div>
            <div class="product-price">￥{{ formatPrice(item.price) }}</div>
          </div>
        </div>
      </div>
    </section>

    <section class="section-block">
      <div class="section-head">
        <div>
          <h3>新品推荐</h3>
          <p>精选上新，第一时间锁定潮流单品</p>
        </div>
          <el-button type="primary" color="#ff6a3d" plain @click="goMore('new')">更多上新</el-button>
      </div>
      <div class="product-grid">
        <div
          v-for="item in newProductsDisplay"
          :key="item.id"
          class="product-card compact is-clickable"
          @click="goProduct(item)"
        >
          <div class="product-cover" :style="getCoverStyle(item)"></div>
          <div class="product-info">
            <div class="product-title">{{ item.name }}</div>
            <div class="product-meta">
              <span>{{ item.subtitle }}</span>
            </div>
            <div class="product-price">￥{{ formatPrice(item.price) }}</div>
          </div>
        </div>
      </div>
      
    </section>
    
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { fetchProducts } from "@/data/products";

const search = ref("");
const router = useRouter();
const authStore = useAuthStore();
const isAuthed = computed(() => !!authStore.token);

const heroSlides = ref([]);

const slideMetaMap = {
  '数码潮玩':  { tag: '数码新品', title: '数码潮玩精选',   subtitle: '耳机、相机、潮玩数码一站配齐。',           bg: 'linear-gradient(135deg, #2d3436, #636e72)' },
  '美妆护肤':  { tag: '美妆好物', title: '美妆护肤精选',   subtitle: '精华、防晒、洁面，呵护每一寸肌肤。',         bg: 'linear-gradient(135deg, #d4a5a5, #9b7b7b)' },
  '日用家居':  { tag: '居家焕新', title: '日用家居精选',   subtitle: '收纳、香氛，打造更顺手的日常空间。',         bg: 'linear-gradient(135deg, #a3b1c6, #7b8fa1)' },
  '服饰鞋包':  { tag: '出行穿搭', title: '服饰鞋包精选',   subtitle: '通勤背包、休闲鞋，轻松应对每一天。',         bg: 'linear-gradient(135deg, #8d7b7b, #6b5b5b)' },
  '运动户外':  { tag: '户外热卖', title: '运动户外精选',   subtitle: '登山、运动、露营装备一次选齐。',             bg: 'linear-gradient(135deg, #60a3bc, #3c6382)' },
  '食品饮品':  { tag: '轻食补给', title: '食品饮品精选',   subtitle: '咖啡、燕麦、轻食饮品零负担。',               bg: 'linear-gradient(135deg, #c9a96e, #8b6914)' },
  '智能家电':  { tag: '智能生活', title: '智能家电精选',   subtitle: '空气炸锅、循环风扇，提升居家效率。',         bg: 'linear-gradient(135deg, #5b7b8a, #3b5b6a)' },
  '办公文具':  { tag: '效率办公', title: '办公文具精选',   subtitle: '笔记本、鼠标、收纳，高效每一天。',           bg: 'linear-gradient(135deg, #7b8b9a, #5b6b7a)' },
};

const loadHeroSlides = (allProducts) => {
  const grouped = {};
  for (const p of allProducts) {
    const cat = p.category;
    if (!grouped[cat]) grouped[cat] = [];
    grouped[cat].push(p);
  }
  const slides = [];
  for (const [category, products] of Object.entries(grouped)) {
    const meta = slideMetaMap[category] || { tag: category, title: category, subtitle: '', bg: '#555' };
    slides.push({
      category,
      tag: meta.tag,
      title: meta.title,
      subtitle: meta.subtitle,
      bg: meta.bg,
      products: products.slice(0, 3),
    });
  }
  heroSlides.value = slides;
};

const hotKeywords = ["耳机", "运动鞋", "便携饮品", "香氛蜡烛", "露营装备", "日用家居"];

const categories = [
  { name: "数码潮玩", short: "数码", desc: "潮流数码与配件", bg: "#ffe7e0" },
  { name: "美妆护肤", short: "美妆", desc: "轻薄底妆与护理", bg: "#fff1d6" },
  { name: "日用家居", short: "家居", desc: "高颜值生活好物", bg: "#e7f7ff" },
  { name: "服饰鞋包", short: "服饰", desc: "通勤与休闲穿搭", bg: "#f4e9ff" },
  { name: "运动户外", short: "运动", desc: "专业装备与户外", bg: "#e6f7e6" },
  { name: "食品饮品", short: "食品", desc: "轻食饮品与零食", bg: "#fff7e0" },
  { name: "智能家电", short: "家电", desc: "提升效率的家电", bg: "#e6efff" },
  { name: "办公文具", short: "办公", desc: "效率装备与文具", bg: "#ffeef2" }
];

const hotProducts = ref([]);
const newProducts = ref([]);
const hotProductsDisplay = computed(() => hotProducts.value.slice(0, 4));
const newProductsDisplay = computed(() => newProducts.value.slice(0, 4));

// 优先通过后端获取热门/新品；热门商品不再回退到本地静态数据
const loadHomeProducts = async () => {
  try {
    const hot = await fetchProducts({ type: "hot", size: 4 });
    hotProducts.value = Array.isArray(hot) ? hot.slice(0, 4) : [];
  } catch (e) {
    hotProducts.value = [];
  }
  try {
    const neu = await fetchProducts({ type: "new", size: 4 });
    newProducts.value = Array.isArray(neu) ? neu.slice(0, 4) : [];
  } catch (e) {
    newProducts.value = [];
  }
  // 加载轮播图商品（按品类分组，每页最多3个）
  try {
    const all = await fetchProducts({ page: 0, size: 100 });
    loadHeroSlides(Array.isArray(all) ? all : []);
  } catch (e) {
    heroSlides.value = [];
  }
};

onMounted(() => {
  loadHomeProducts();
});

const getCoverStyle = (product) => {
  const img = product?.image || product?.coverUrl;
  if (img) {
    return { background: `url(${img}) center/cover no-repeat, #fff` };
  }
  return { background: "#fff" };
};

const getHeroImageStyle = (product) => {
  const img = product?.image || product?.coverUrl;
  return {
    backgroundImage: img ? `url(${img})` : "none",
    backgroundColor: "#fff"
  };
};

const formatPrice = (p) => {
  if (p == null) return "0";
  const n = Number(p);
  if (Number.isInteger(n)) return n.toString();
  return n.toFixed(2);
};

const handleSearch = () => {
  if (!search.value) {
    return;
  }
  router.push({ path: "/products", query: { q: search.value } });
};

const onHotClick = (kw) => {
  if (!kw) return;
  search.value = kw;
  router.push({ path: "/products", query: { q: kw } });
};

const goProduct = (item) => {
  router.push(`/product/${item.id}`);
};

const goCategory = (name) => {
  router.push(`/category/${encodeURIComponent(name)}`);
};

const goMore = (type) => {
  router.push({ path: '/products', query: { type } });
};


</script>

<style scoped>
.home-page {
  padding: 28px 24px 80px;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.home-header {
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  align-items: center;
  gap: 20px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: linear-gradient(135deg, #ff6a3d, #ff3d55);
  color: #fff;
  display: grid;
  place-items: center;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-name {
  font-weight: 700;
  font-size: 20px;
}

.brand-sub {
  color: var(--text-secondary);
  font-size: 12px;
}

.search-box {
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: transparent;
}

/* 外层白色容器，包裹输入框 */
.search-box.red-frame {
  background: #fff;
  padding: 0; /* 按钮放入 input wrapper，不需要外层内边距 */
  border-radius: 12px;
}

/* 仿京东搜索框 */
.search-input :deep(.el-input__wrapper) {
  /* left side rounded; right side will be rounded by the button */
  border-radius: 12px 0 0 12px;
  box-shadow: var(--shadow-soft);
  height: 44px;
  font-size: 14px;
  overflow: hidden; /* ensure append button is clipped into the wrapper */
  display: flex; /* ensure append lives inside and aligns */
  align-items: center;
  background: #fff; /* make wrapper the white search box */
  padding: 0;
}
.search-input :deep(.el-input__prefix) {
  color: var(--text-secondary);
  padding-left: 12px;
}
.search-input .search-icon { display:inline-flex; width:18px; text-align:center; align-items:center; justify-content:center }
.search-input .search-icon svg { width:16px; height:16px }

.search-input :deep(.el-input__append),
.search-input :deep(.el-input__append .el-button),
.search-input :deep(.el-input__suffix .el-button) {
  /* allow the append button to have rounded left corners so both top corners are rounded visually */
  border-top-left-radius: 8px !important;
  border-bottom-left-radius: 8px !important;
  border-radius: 8px !important;
  background: #e60012 !important;
  color: #fff !important;
  border: none !important;
  padding: 0 12px !important;
  height: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  margin-left: 0 !important;
}
.search-input :deep(.el-input__append .el-button),
.search-input :deep(.el-input__append .el-button):focus,
.search-input :deep(.el-input__append .el-button):hover,
.search-input :deep(.el-input__append .el-button):active {
  border: none !important;
  box-shadow: none !important;
  outline: none !important;
  -webkit-box-shadow: none !important;
}
.search-input :deep(.el-input__append .el-button) {
  /* remove any inner stroke that creates a visible boundary */
  background-clip: padding-box !important;
  height: 100% !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  padding: 0 16px !important;
}
.search-input :deep(.el-input__append) { height: 100%; display: flex; align-items: center }
.jd-search-btn { border-radius: 8px; height: 100%; padding: 0 16px; display: flex; align-items: center; justify-content: center }
.search-input :deep(.el-input__append .el-button) svg,
.search-input :deep(.el-input__suffix .el-button) svg { width:18px; height:18px }
.search-input :deep(.el-input__inner) { height:44px; padding-left:12px; flex: 1; background: transparent; border: none; box-shadow: none }
.jd-search-btn { font-weight:600 }
.search-input :deep(.jd-search-btn) { background: #e60012 !important; color: #fff !important; border-color: #e60012 !important }


.hot-keys {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.hot-title {
  font-size: 12px;
  color: var(--text-secondary);
}

.hot-tag {
  border-radius: 999px;
}

.header-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  align-items: center;
}

.avatar-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  text-decoration: none;
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  border: 2px solid transparent;
}

.avatar-link:hover {
  box-shadow: 0 0 0 3px rgba(255, 106, 61, 0.25);
  transform: scale(1.05);
  border-color: #ff6a3d;
}

.header-avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #ff6a3d, #ff3d55);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 15px;
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


.hero {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
  align-items: stretch;
}

.banner {
  height: 320px;
  border-radius: 26px;
  padding: 28px 30px;
  color: #fff;
  display: grid;
  grid-template-columns: 1.15fr 1fr;
  align-items: center;
  gap: 24px;
  position: relative;
  overflow: hidden;
}

.banner.is-clickable {
  cursor: pointer;
}

.banner.is-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 22px 44px rgba(15, 23, 42, 0.18);
}

.banner-info h2 {
  margin: 12px 0 8px;
  font-size: 30px;
}

.banner-info p {
  margin: 0 0 18px;
  color: rgba(255, 255, 255, 0.9);
}

.banner-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
  color: rgba(255, 255, 255, 0.92);
  font-size: 13px;
}

.banner-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
}

.banner-showcase {
  position: relative;
  height: 100%;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  align-items: center;
  gap: 14px;
  padding-left: 12px;
}

.hero-product-card {
  min-height: 168px;
  border-radius: 22px;
  padding: 18px 16px;
  position: relative;
  overflow: hidden;
  color: #fff;
  box-shadow: 0 18px 30px rgba(0, 0, 0, 0.16);
  backdrop-filter: blur(8px);
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  gap: 6px;
  animation: heroFloat 2.8s ease-in-out infinite;
  cursor: default;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  transform: perspective(900px) rotateX(8deg) rotateY(-10deg);
}

.hero-product-card:nth-child(2) {
  margin-top: 18px;
  animation-duration: 3.1s;
}

.hero-product-card:nth-child(3) {
  margin-top: -8px;
  animation-duration: 2.9s;
}

.hero-product-card:hover {
  transform: perspective(900px) rotateX(4deg) rotateY(-6deg) translateY(-6px) scale(1.02);
  box-shadow: 0 24px 36px rgba(0, 0, 0, 0.22);
}

.hero-product-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 25% 20%, rgba(255, 255, 255, 0.35), transparent 46%);
  pointer-events: none;
}

.hero-product-media {
  min-height: 104px;
  display: grid;
  place-items: center;
  margin-bottom: 4px;
  perspective: 1000px;
}

.hero-product-image {
  position: relative;
  width: 120%;
  height: 140px;
  border-radius: 18px;
  background-position: center;
  background-repeat: no-repeat;
  background-size: contain;
  filter: drop-shadow(0 20px 30px rgba(12, 18, 28, 0.32));
  transform: rotateY(-18deg) rotateX(8deg) translateY(6px) translateX(6px);
  box-shadow: 0 24px 40px rgba(12, 18, 28, 0.28), inset 0 0 0 1px rgba(255, 255, 255, 0.14);
}

.hero-product-image::after {
  content: "";
  position: absolute;
  inset: 0;
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.16), transparent 40%);
  pointer-events: none;
}

.hero-badge {
  position: absolute;
  left: 12px;
  top: 12px;
  background: #ff4d6d;
  color: #fff;
  padding: 6px 8px;
  font-size: 12px;
  font-weight: 700;
  border-radius: 6px;
  transform: rotate(-6deg);
  box-shadow: 0 6px 14px rgba(255, 77, 109, 0.18);
  z-index: 6;
}

.hero-product-pedestal {
  position: absolute;
  left: 50%;
  transform: translateX(-50%) translateY(12px) rotateX(80deg) scaleX(1.05);
  bottom: 12px;
  width: 72%;
  height: 18px;
  background: radial-gradient(ellipse at center, rgba(0,0,0,0.28), rgba(0,0,0,0.08));
  filter: blur(6px);
  border-radius: 50%;
  z-index: 1;
}

.hero-product-name {
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}

.hero-product-price {
  font-size: 13px;
  opacity: 0.95;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.category-card {
  background: var(--card);
  border-radius: 18px;
  padding: 18px;
  box-shadow: var(--shadow-soft);
  display: grid;
  gap: 8px;
  animation: floatUp 0.6s ease both;
}

.category-card.is-clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.category-card.is-clickable:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(16, 24, 40, 0.18);
}

.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  display: grid;
  place-items: center;
  font-weight: 600;
}

.category-name {
  font-weight: 600;
}

.category-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

.section-block {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 28px;
  padding: 28px;
  box-shadow: var(--shadow-soft);
  display: grid;
  gap: 22px;
  animation: fadeIn 0.6s ease both;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 22px;
}

.section-head p {
  margin: 6px 0 0;
  color: var(--text-secondary);
}

.product-row {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  overflow-x: auto;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  overflow-x: auto;
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

.product-card.compact {
  grid-template-rows: auto 1fr;
}

.product-card.tall {
  grid-template-rows: auto 1fr;
}

.product-cover {
  height: 140px;
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


@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes floatUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes heroFloat {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@media (max-width: 1200px) {
  .home-header {
    grid-template-columns: 1fr;
  }
  .hero {
    grid-template-columns: 1fr;
  }
  .category-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .product-row,
  .product-grid {
    grid-template-columns: repeat(4, minmax(220px, 1fr));
  }

  .banner {
    grid-template-columns: 1fr;
    height: auto;
  }

  .banner-showcase {
    padding-left: 0;
  }
}

@media (max-width: 720px) {
  .home-page {
    padding: 20px 16px 60px;
  }
  .category-grid {
    grid-template-columns: 1fr;
  }

  .product-row,
  .product-grid {
    grid-template-columns: repeat(4, minmax(220px, 1fr));
  }
  .banner {
    grid-template-columns: 1fr;
  }
  .banner-showcase {
    grid-template-columns: 1fr;
  }
  .hero-product-card:nth-child(2),
  .hero-product-card:nth-child(3) {
    margin-top: 0;
  }
  .header-actions {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .home-page { padding: 8px !important; gap: 18px; }
  .home-header { grid-template-columns: 1fr; gap: 12px; }
  .brand-name { font-size: 16px; }
  .search-box { gap: 4px; }
  .hero { gap: 12px; }
  .banner { height: auto !important; padding: 28px 16px !important; grid-template-columns: 1fr; }
  .banner-info h2 { font-size: 1.5rem !important; margin: 8px 0 6px; }
  .banner-info p { font-size: 0.9rem !important; }
  .banner-showcase { grid-template-columns: 1fr; padding-left: 0; gap: 8px; }
  .hero-product-card { min-height: auto; padding: 12px 14px; }
  .hero-product-card:nth-child(2), .hero-product-card:nth-child(3) { margin-top: 0; }
  .hero-product-image { width: 100%; height: 100px; }
  .hero-product-name { font-size: 15px; }
  .hero-product-price { font-size: 12px; }
  .section-block { padding: 16px; border-radius: 20px; gap: 14px; }
  .section-head { flex-direction: column; align-items: flex-start; gap: 10px; }
  .section-head h3 { font-size: 1.1rem !important; }
  .section-head .el-button { width: 100%; }
  .product-grid, .product-row { gap: 8px !important; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .product-card { border-radius: 14px; }
  .product-cover { height: 110px; }
  .product-info { padding: 10px 12px 14px; gap: 4px; }
  .product-title { font-size: 13px; }
  .product-price { font-size: 15px; }
  .category-grid { gap: 8px !important; grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .category-card { padding: 14px; border-radius: 14px; }
  .category-icon { width: 38px; height: 38px; border-radius: 12px; font-size: 13px; }
  .category-name { font-size: 14px; }
  .category-desc { font-size: 11px; }
  .hot-keys { gap: 6px; }
  .hot-tag { font-size: 11px; }
}
</style>
