<template>
  <div class="orders-page">
    <!-- 顶部导航已移除：不显示返回与首页按钮 -->

    <section class="orders-shell">
      <h2 class="page-title">我的订单</h2>
      <div v-if="loading" class="loading-block short"></div>
      <div v-else-if="orders.length === 0" class="orders-empty">暂无订单</div>
      <div v-else class="orders-list">
        <article v-for="order in orders" :key="order.id" class="order-card" @click="goDetail(order)" role="button">
          <div class="order-head">
            <div class="meta">
              <div class="order-no">订单号：<span>{{ order.orderNo }}</span></div>
              <div class="order-time">{{ formatTime(order.createdAt) }}</div>
            </div>
            <div class="status-wrap">
              <el-tag :type="statusTagType(order.status)">{{ statusText(order.status) }}</el-tag>
            </div>
          </div>

          <div class="order-body">
                    <ul class="items-list">
                      <li v-for="item in order.items" :key="item.id" class="items-list-item">
                        <div class="item-name">{{ item.productName }}</div>
                        <div class="item-qty">×{{ item.quantity }}</div>
                        <div class="item-price">￥{{ formatMoney(item.productPrice != null ? item.productPrice : (item.subtotal || 0) / (item.quantity || 1)) }}</div>
                      </li>
                    </ul>
                  </div>

          <div class="order-footer">
            <div class="order-total">总金额：<strong>￥{{ formatMoney(order.totalAmount) }}</strong></div>
            <div class="order-actions">
              <el-button v-if="order.status === 'PENDING_PAYMENT'" type="primary" color="#ff6a3d" :loading="payingOrderId === order.id" @click.stop.prevent="handlePay(order)">模拟支付</el-button>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { listOrders, payOrder } from "@/api/order";

const router = useRouter();
const orders = ref([]);
const loading = ref(true);
const payingOrderId = ref(null);

const loadOrders = async () => {
  loading.value = true;
  try {
    const res = await listOrders();
    orders.value = res.data || [];
  } finally {
    loading.value = false;
  }
};

onMounted(loadOrders);

const handleBack = () => {
  if (window.history.length > 1) return router.back();
  router.push('/home');
};

const formatMoney = (v) => Number(v || 0).toFixed(2);
const formatTime = (v) => (v ? new Date(v).toLocaleString() : "-");

const handlePay = async (order) => {
  payingOrderId.value = order.id;
  try {
    const res = await payOrder(order.id);
    ElMessage.success(`订单 ${res.data.orderNo} 支付成功`);
    await loadOrders();
  } finally {
    payingOrderId.value = null;
  }
};

const goDetail = (order) => {
  router.push(`/orders/${order.id}`);
};

const statusText = (status) => {
  const map = {
    PENDING_PAYMENT: "待支付",
    PAID: "已支付",
    CANCELLED: "已取消"
  };
  return map[status] || status;
};

const statusTagType = (status) => {
  if (status === "PAID") return "success";
  if (status === "PENDING_PAYMENT") return "warning";
  return "info";
};
</script>

<style scoped>
.orders-page { padding: 28px 24px 80px; min-height: 100vh }
.orders-header { display:flex; align-items:center; justify-content:space-between; gap:12px }
.page-title { margin: 8px 0 18px; font-size:20px }
.orders-list { display:grid; gap:18px }
.order-card { background: #fff; padding: 18px; border-radius: 14px; box-shadow: var(--shadow-soft); display: grid; gap: 12px }
.order-head { display:flex; justify-content:space-between; align-items:center }
.order-head .meta { display:flex; flex-direction:column; gap:6px }
.order-no { font-weight:700; color:var(--text-primary) }
.order-no span { font-weight:500; color:var(--text-secondary); margin-left:6px }
.order-time { color:var(--text-secondary); font-size:12px }
.status-wrap { min-width:110px; display:flex; justify-content:flex-end }
.order-body { border-top:1px solid rgba(0,0,0,0.04); border-bottom:1px solid rgba(0,0,0,0.04); padding:10px 0 }
.items-list { list-style:none; margin:0; padding:0; display:grid; gap:8px }
.items-list-item { display:grid; grid-template-columns: 1fr auto auto; align-items:center; gap:12px }
.item-name { color:var(--text-primary); font-weight:600 }
.item-qty { color:var(--text-secondary); text-align:center }
.item-price { font-weight:700; text-align:right }
.order-footer { display:flex; justify-content:space-between; align-items:center; gap:12px }
.order-total { font-size:16px; color:var(--text-primary) }
.order-actions { display:flex; gap:8px }
.orders-empty { padding:40px; text-align:center; color:var(--text-secondary) }

@media (max-width: 720px) {
  .items-list-item { flex-direction: column; align-items: flex-start }
  .status-wrap { justify-content: flex-start }
}
</style>
