<template>
  <div class="page-card">
    <h2>商品收藏</h2>

    <div v-if="items.length === 0" class="empty-hint">
      <p>你还没有收藏任何商品。</p>
      <p class="sub">浏览商品时点击"收藏"即可添加到此处。</p>
    </div>

    <div v-else class="fav-grid">
      <div
        v-for="item in items"
        :key="item.id"
        class="fav-card"
        @click="goProduct(item)"
      >
        <div class="fav-cover" :style="coverStyle(item)"></div>
        <div class="fav-info">
          <div class="fav-name">{{ item.name }}</div>
          <div class="fav-sub">{{ item.subtitle }}</div>
          <div class="fav-price">￥{{ formatPrice(item.price) }}</div>
        </div>
        <el-button
          class="fav-remove-btn"
          size="small"
          type="danger"
          circle
          :icon="Delete"
          @click.stop="handleRemove(item)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { Delete } from "@element-plus/icons-vue";
import { getFavorites, removeFavorite } from "@/api/favorites";

const router = useRouter();
const items = ref([]);

const load = () => {
  items.value = getFavorites();
};

onMounted(load);

const coverStyle = (item) => {
  if (item.image) {
    return { background: `url(${item.image}) center/cover no-repeat, #fff` };
  }
  return { background: "#f5f7fb" };
};

const formatPrice = (p) => {
  if (p == null) return "0";
  const n = Number(p);
  return Number.isInteger(n) ? n.toString() : n.toFixed(2);
};

const goProduct = (item) => {
  router.push(`/product/${item.id}`);
};

const handleRemove = async (item) => {
  try {
    await ElMessageBox.confirm(`确认将「${item.name}」从收藏中移除？`, "取消收藏", {
      confirmButtonText: "确认移除",
      cancelButtonText: "取消",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  removeFavorite(item.id);
  ElMessage.success("已取消收藏");
  load();
};
</script>

<style scoped>
.page-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.page-card h2 { margin:0 0 20px; font-size:20px; }
.empty-hint { padding:60px 0; text-align:center; color:var(--text-secondary); }
.empty-hint p { margin:0; line-height:1.8; }
.empty-hint .sub { font-size:14px; color:#b0b0b0; }

.fav-grid { display:grid; grid-template-columns:repeat(3, minmax(0, 1fr)); gap:18px; }
.fav-card { position:relative; background:#fff; border-radius:14px; overflow:hidden; box-shadow:0 2px 12px rgba(0,0,0,0.06); cursor:pointer; transition:transform .2s ease, box-shadow .2s ease; }
.fav-card:hover { transform:translateY(-4px); box-shadow:0 8px 24px rgba(0,0,0,0.12); }
.fav-cover { height:140px; }
.fav-info { padding:14px 16px 18px; }
.fav-name { font-weight:600; }
.fav-sub { font-size:12px; color:var(--text-secondary); margin-top:4px; }
.fav-price { font-weight:700; font-size:16px; margin-top:8px; color:#e4393c; }
.fav-remove-btn { position:absolute; top:8px; right:8px; }

@media (max-width: 768px) {
  .page-card { padding: 12px !important; border-radius: 10px; }
  .page-card h2 { font-size: 18px; margin-bottom: 14px; }
  .fav-grid { grid-template-columns: 1fr !important; gap: 10px; }
  .fav-card { border-radius: 10px; }
  .fav-cover { height: 120px; }
  .fav-info { padding: 10px 12px 14px; }
  .fav-name { font-size: 14px; }
  .fav-price { font-size: 15px; }
  .fav-remove-btn { top: 6px; right: 6px; }
}
</style>
