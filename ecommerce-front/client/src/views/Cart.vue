<template>
  <div class="cart-page">
    <header class="cart-header">
      <div>
        <div class="page-kicker">购物车</div>
        <h1>购物车</h1>
        <p class="page-subtitle">勾选商品后可直接模拟结算和支付。</p>
      </div>
      
    </header>

    <section v-if="loading" class="loading-panel">
      <div class="loading-block"></div>
      <div class="loading-block short"></div>
      <div class="loading-block"></div>
    </section>

    <template v-else>
      <section v-if="cartItems.length === 0" class="empty-box">
        <div class="empty-title">购物车还是空的</div>
        <div class="empty-subtitle">先去挑几件喜欢的商品吧。</div>
        <el-button type="primary" color="#ff6a3d" @click="router.push('/home')">去首页逛逛</el-button>
      </section>

      <section v-else class="cart-layout">
        <div class="cart-list-panel">
          <div class="cart-toolbar">
            <el-checkbox :model-value="allSelected" @change="toggleAll">全选</el-checkbox>
            <div class="cart-toolbar-meta">
              <span>已选 {{ selectedCount }} 件</span>
              <span>共 {{ totalCount }} 件</span>
            </div>
            <button class="text-link button-link danger" type="button" :disabled="selectedCount === 0" @click="clearSelected">
              删除已选
            </button>
          </div>

          <div class="cart-items">
            <article v-for="item in cartItems" :key="item.id" class="cart-item">
              <el-checkbox :model-value="item.selected" @change="(checked) => updateSelection(item, checked)" />
              <div class="item-cover" :style="coverStyle(item)"></div>
              <div class="item-main">
                <router-link :to="`/product/${item.productIdentifier}`" class="item-name">{{ item.productName }}</router-link>
                <div class="item-subtitle">{{ item.productSubtitle || item.category || '精选好物' }}</div>
                <div class="item-price">单价：￥{{ formatMoney(item.productPrice) }}</div>
              </div>
              <div class="item-actions">
                <el-input-number :model-value="item.quantity" :min="1" @change="(value) => updateQuantity(item, value)" />
                <div class="item-subtotal">￥{{ formatMoney(item.subtotal) }}</div>
                <button class="text-link button-link danger" type="button" @click="removeItem(item)">删除</button>
              </div>
            </article>
          </div>
        </div>

        <aside class="summary-panel">
          <h3>结算信息</h3>
          <div class="summary-row">
            <span>商品总数</span>
            <strong>{{ totalCount }}</strong>
          </div>
          <div class="summary-row">
            <span>已选商品</span>
            <strong>{{ selectedCount }}</strong>
          </div>
          <div class="summary-row amount-row">
            <span>应付金额</span>
            <strong>￥{{ formatMoney(selectedAmount) }}</strong>
          </div>
          <el-button type="primary" color="#ff6a3d" class="checkout-btn" :disabled="selectedCount === 0" :loading="checkoutLoading" @click="handleCheckout">
            去结算
          </el-button>
          <div class="summary-tip">结算后会生成待支付订单，支付为模拟流程。</div>
        </aside>
      </section>
    </template>
    
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { clearSelectedCartItems, deleteCartItem, getCartItems, updateCartItem } from "@/api/cart";

const router = useRouter();
const loading = ref(true);
const checkoutLoading = ref(false);
const cartItems = ref([]);
// orders removed: orders section eliminated from UI

const loadCart = async () => {
  const res = await getCartItems();
  const data = res.data || {};
  cartItems.value = data.items || [];
};

const reloadCart = async () => {
  loading.value = true;
  try {
    await loadCart();
  } finally {
    loading.value = false;
  }
};

onMounted(reloadCart);

const totalCount = computed(() => cartItems.value.reduce((sum, item) => sum + (item.quantity || 0), 0));
const selectedCount = computed(() => cartItems.value.filter((item) => item.selected).reduce((sum, item) => sum + (item.quantity || 0), 0));
const selectedAmount = computed(() => cartItems.value.filter((item) => item.selected).reduce((sum, item) => sum + Number(item.subtotal || 0), 0));
const allSelected = computed(() => cartItems.value.length > 0 && cartItems.value.every((item) => item.selected));

const formatMoney = (value) => Number(value || 0).toFixed(2);

const coverStyle = (item) => ({
  background: item.productCoverUrl ? `center / cover no-repeat url(${item.productCoverUrl})` : "#fff"
});

const syncCart = async () => {
  await loadCart();
};

const updateSelection = async (item, checked) => {
  const prev = item.selected;
  item.selected = checked;
  try {
    await updateCartItem(item.id, { selected: checked });
  } catch (e) {
    item.selected = prev;
    ElMessage.error("更新失败，请稍后重试");
  }
};

const updateQuantity = async (item, quantity) => {
  const prevQty = item.quantity;
  const prevSubtotal = item.subtotal;
  item.quantity = quantity;
  item.subtotal = Number(item.productPrice || 0) * Number(quantity || 0);
  try {
    await updateCartItem(item.id, { quantity });
  } catch (e) {
    item.quantity = prevQty;
    item.subtotal = prevSubtotal;
    ElMessage.error("更新数量失败，请稍后重试");
  }
};

const toggleAll = async (checked) => {
  const prevStates = cartItems.value.map((it) => ({ id: it.id, prev: it.selected }));
  cartItems.value.forEach((it) => (it.selected = checked));
  try {
    await Promise.all(cartItems.value.map((item) => updateCartItem(item.id, { selected: checked })));
  } catch (e) {
    // revert on error
    for (const p of prevStates) {
      const it = cartItems.value.find((x) => x.id === p.id);
      if (it) it.selected = p.prev;
    }
    ElMessage.error("批量更新失败，请稍后重试");
  }
};

const removeItem = async (item) => {
  try {
    await ElMessageBox.confirm(`商品「${item.productName}」将从购物车中移除，删除后可在商品页重新添加。`, "确认删除商品", {
      confirmButtonText: "确认删除",
      cancelButtonText: "暂不删除",
      type: "warning",
      customClass: "pretty-confirm-box pretty-confirm-box--danger",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  await deleteCartItem(item.id);
  ElMessage.success("已删除");
  await reloadCart();
};

const clearSelected = async () => {
  try {
    await ElMessageBox.confirm("已勾选商品将从购物车中移除，删除后可在商品页重新添加。", "确认删除已选商品", {
      confirmButtonText: "确认删除",
      cancelButtonText: "暂不删除",
      type: "warning",
      customClass: "pretty-confirm-box pretty-confirm-box--danger",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  await clearSelectedCartItems();
  ElMessage.success("已删除已选商品");
  await reloadCart();
};

const handleCheckout = async () => {
  // 跳转到结算页面，并通过 query 传递已选商品 id 列表（逗号分隔）
  checkoutLoading.value = true;
  try {
    const selectedIds = cartItems.value.filter(i => i.selected).map(i => i.id).join(",");
    router.push({ path: "/checkout", query: { items: selectedIds } });
  } finally {
    checkoutLoading.value = false;
  }
};

// payment & orders logic removed as orders UI was removed
</script>

<style scoped>
.cart-page {
  min-height: 100vh;
  padding: 28px 24px 80px;
  display: grid;
  gap: 24px;
}

.cart-header,
.section-head,
.order-head,
.order-footer,
.cart-toolbar,
.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.page-kicker {
  font-size: 12px;
  letter-spacing: 2px;
  color: var(--text-secondary);
}

.cart-header h1,
.section-head h2 {
  margin: 6px 0 8px;
  font-size: 30px;
}

.page-subtitle,
.section-head p,
.summary-tip,
.order-time,
.empty-subtitle {
  color: var(--text-secondary);
}

.header-links,
.cart-toolbar-meta,
.order-items-preview {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.text-link,
.button-link {
  border: none;
  background: none;
  padding: 0;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 14px;
  text-decoration: none;
}

.button-link.danger {
  color: #d14343;
}

.cart-layout {
  display: grid;
  grid-template-columns: 1fr;
  gap: 20px;
}

.cart-list-panel,
.summary-panel,
.order-card,
.empty-box,
.loading-panel,
.order-loading,
.orders-empty {
  background: rgba(255, 255, 255, 0.92);
  border-radius: 24px;
  box-shadow: var(--shadow-soft);
}

.cart-list-panel,
.summary-panel {
  padding: 22px;
}

.cart-toolbar {
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  margin-bottom: 16px;
}

.cart-items {
  display: grid;
  gap: 14px;
}

.cart-item {
  display: grid;
  grid-template-columns: auto 84px minmax(0, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 16px;
  border-radius: 18px;
  background: #fff;
}

.item-cover {
  width: 84px;
  height: 84px;
  border-radius: 18px;
}

.item-main {
  display: grid;
  gap: 6px;
}

.item-name {
  color: var(--text-primary);
  font-weight: 700;
  text-decoration: none;
  font-size: 16px;
}

.item-subtitle,
.item-price,
.item-subtotal,
.summary-tip {
  font-size: 13px;
}

.item-actions {
  display: grid;
  justify-items: end;
  gap: 10px;
}

.summary-panel h3,
.order-card,
.empty-title,
.orders-empty {
  margin: 0;
}

.summary-panel {
  display: grid;
  gap: 14px;
  align-content: start;
  height: fit-content;
}

.amount-row strong {
  color: #ff6a3d;
  font-size: 22px;
}

.checkout-btn {
  width: 100%;
  margin-top: 6px;
}

.loading-panel,
.order-loading {
  padding: 28px;
  display: grid;
  gap: 16px;
}

.loading-block {
  height: 140px;
  border-radius: 18px;
  background: linear-gradient(90deg, #eef1f5 25%, #f7f9fc 37%, #eef1f5 63%);
  background-size: 400% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

.loading-block.short {
  height: 72px;
}

.orders-section {
  display: grid;
  gap: 16px;
}

.orders-list {
  display: grid;
  gap: 14px;
}

.order-card,
.orders-empty {
  padding: 20px 22px;
}

.order-no {
  font-weight: 700;
}

.order-item-chip {
  padding: 6px 10px;
  border-radius: 999px;
  background: #f4f6f8;
  font-size: 12px;
}

.more-chip {
  background: #ffe9df;
  color: #ff6a3d;
}

.order-total {
  font-weight: 700;
}

.empty-box {
  padding: 42px 28px;
  display: grid;
  gap: 12px;
  justify-items: center;
  text-align: center;
}

.empty-title {
  font-size: 20px;
  font-weight: 700;
}

.loading-panel {
  min-height: 240px;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}

@media (max-width: 960px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }


@media (min-width: 901px) {
  .summary-panel {
    max-width: 420px;
    width: 100%;
    justify-self: start;
  }
}
  .summary-panel {
    position: static;
  }

  .cart-item {
    grid-template-columns: auto 72px minmax(0, 1fr);
  }

  .item-actions {
    grid-column: 1 / -1;
    justify-items: start;
  }
}

@media (max-width: 640px) {
  .cart-page {
    padding: 20px 14px 64px;
  }

  .cart-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .cart-item {
    padding: 14px;
  }
}
</style>
