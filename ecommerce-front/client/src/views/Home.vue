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
          <el-tag v-for="item in hotKeywords" :key="item" size="small" effect="plain" class="hot-tag">
            {{ item }}
          </el-tag>
        </div>
      </div>
      <div class="header-actions">
        <template v-if="!isAuthed">
          <router-link to="/login" class="text-link">登录</router-link>
          <router-link to="/register" class="text-link">注册</router-link>
        </template>
      </div>
    </header>

    <section class="hero">
      <div class="hero-carousel">
        <el-carousel height="320px" indicator-position="outside">
          <el-carousel-item v-for="banner in banners" :key="banner.title">
            <div class="banner" :style="{ background: banner.bg }">
              <div class="banner-info">
                <span class="banner-tag">{{ banner.tag }}</span>
                <h2>{{ banner.title }}</h2>
                <p>{{ banner.subtitle }}</p>
                <el-button type="primary" color="#1c1c1e">马上看看</el-button>
              </div>
              <div class="banner-art">
                <div class="art-ring"></div>
                <div class="art-card">限时折扣</div>
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="hero-side">
        <div v-for="card in quickCards" :key="card.title" class="side-card" :style="{ background: card.bg }">
          <div class="side-title">{{ card.title }}</div>
          <div class="side-desc">{{ card.desc }}</div>
          <el-button type="primary" color="#ffffff" plain class="side-btn">{{ card.action }}</el-button>
        </div>
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
          v-for="item in hotProducts"
          :key="item.id"
          class="product-card is-clickable"
          @click="goProduct(item)"
        >
          <div class="product-cover" :style="{ background: item.bg }"></div>
          <div class="product-info">
            <div class="product-title">{{ item.name }}</div>
            <div class="product-meta">
              <span>销量 {{ item.sold }}</span>
              <el-tag size="small" effect="dark" type="danger">{{ item.tag }}</el-tag>
            </div>
            <div class="product-price">￥{{ item.price }}</div>
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
          v-for="item in newProducts"
          :key="item.id"
          class="product-card compact is-clickable"
          @click="goProduct(item)"
        >
          <div class="product-cover" :style="{ background: item.bg }"></div>
          <div class="product-info">
            <div class="product-title">{{ item.name }}</div>
            <div class="product-meta">
              <span>{{ item.subtitle }}</span>
            </div>
            <div class="product-price">￥{{ item.price }}</div>
          </div>
        </div>
      </div>
      
    </section>
    
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "@/store/auth";
import { getProductsByIds, hotProductIds, newProductIds } from "@/data/products";

const search = ref("");
const router = useRouter();
const authStore = useAuthStore();
const isAuthed = computed(() => !!authStore.token);

const banners = [
  {
    title: "橙意上新",
    subtitle: "轻量潮品 3 折起，爆款配色随心搭",
    tag: "新季限定",
    bg: "linear-gradient(135deg, #ff6a3d 0%, #ff3d55 45%, #ffb347 100%)"
  },
  {
    title: "城市通勤专场",
    subtitle: "效率穿搭与随身装备，省心组合推荐",
    tag: "通勤精选",
    bg: "linear-gradient(135deg, #2f3640 0%, #4b6584 55%, #f7b731 100%)"
  },
  {
    title: "热卖清单",
    subtitle: "本周榜单上线，口碑爆品一站直达",
    tag: "热销榜",
    bg: "linear-gradient(135deg, #1abc9c 0%, #16a085 45%, #f6e58d 100%)"
  }
];

const quickCards = [
  {
    title: "新人礼包",
    desc: "注册即领 50 元券包",
    action: "立即领取",
    bg: "linear-gradient(140deg, #ffe5d9 0%, #ffd5e5 100%)"
  },
  {
    title: "会员日",
    desc: "精选大牌满减叠加",
    action: "进入专场",
    bg: "linear-gradient(140deg, #e0f7fa 0%, #fce4ec 100%)"
  }
];

const hotKeywords = ["耳机", "运动鞋", "便携咖啡", "香氛蜡烛", "露营装备", "小家电"];

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

const hotProducts = getProductsByIds(hotProductIds);
const newProducts = getProductsByIds(newProductIds);

const handleSearch = () => {
  if (!search.value) {
    return;
  }
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
  border-radius: 12px;
  box-shadow: var(--shadow-soft);
  height: 44px;
  font-size: 14px;
  overflow: hidden; /* ensure append button is clipped into the wrapper */
  display: flex; /* ensure append lives inside and aligns */
  align-items: center;
  background: #fff; /* make wrapper the white search box */
  padding: 0;
}
/* 调试用：临时红色边框，用于确认样式是否生效，确认后会移除 */
.search-input.debug-border :deep(.el-input__wrapper) { border: 2px solid red !important }
.search-input :deep(.el-input__prefix) {
  color: var(--text-secondary);
  padding-left: 12px;
}
.search-input .search-icon { display:inline-flex; width:18px; text-align:center; align-items:center; justify-content:center }
.search-input .search-icon svg { width:16px; height:16px }

.search-input :deep(.el-input__append),
.search-input :deep(.el-input__append .el-button),
.search-input :deep(.el-input__suffix .el-button) {
  border-top-left-radius: 0 !important;
  border-bottom-left-radius: 0 !important;
  border-radius: 0 8px 8px 0 !important;
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
}
.search-input :deep(.el-input__append) { height: 100%; display: flex; align-items: center }
.jd-search-btn { border-radius: 0 8px 8px 0; padding: 8px 16px }
.search-input :deep(.el-input__append .el-button) svg,
.search-input :deep(.el-input__suffix .el-button) svg { width:18px; height:18px }
.search-input :deep(.el-input__inner) { height:44px; padding-left:12px; flex: 1; background: transparent; border: none; box-shadow: none }
.jd-search-btn { font-weight:600 }
.search-input :deep(.jd-search-btn) { background: #e60012 !important; color: #fff !important; border-color: #e60012 !important }

/* 相机按钮已移除，相关样式清理保留注释以便恢复 */
.camera-btn { display: none !important }
.camera-btn svg { display: none !important }

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
  grid-template-columns: 2.4fr 1fr;
  gap: 20px;
  align-items: stretch;
}

.banner {
  height: 320px;
  border-radius: 26px;
  padding: 26px 30px;
  color: #fff;
  display: grid;
  grid-template-columns: 1.3fr 1fr;
  align-items: center;
  gap: 24px;
  position: relative;
  overflow: hidden;
}

.banner-info h2 {
  margin: 12px 0 8px;
  font-size: 30px;
}

.banner-info p {
  margin: 0 0 18px;
  color: rgba(255, 255, 255, 0.9);
}

.banner-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.2);
  font-size: 12px;
}

.banner-art {
  position: relative;
  display: grid;
  place-items: center;
}

.art-ring {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.4);
}

.art-card {
  position: absolute;
  background: rgba(255, 255, 255, 0.2);
  padding: 10px 16px;
  border-radius: 14px;
  backdrop-filter: blur(10px);
}

.hero-side {
  display: grid;
  gap: 16px;
}

.side-card {
  padding: 22px;
  border-radius: 22px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #1c1c1e;
}

.side-title {
  font-size: 18px;
  font-weight: 700;
}

.side-desc {
  color: var(--text-secondary);
}

.side-btn {
  align-self: flex-start;
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
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
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

@media (max-width: 1200px) {
  .home-header {
    grid-template-columns: 1fr;
  }
  .hero {
    grid-template-columns: 1fr;
  }
  .category-grid,
  .product-row,
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 720px) {
  .home-page {
    padding: 20px 16px 60px;
  }
  .category-grid,
  .product-row,
  .product-grid {
    grid-template-columns: 1fr;
  }
  .banner {
    grid-template-columns: 1fr;
  }
  .header-actions {
    justify-content: flex-start;
  }
}
</style>
