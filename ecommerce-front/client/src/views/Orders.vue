<template>
  <div class="orders-page">
    <!-- 顶部导航已移除：不显示返回与首页按钮 -->

    <section class="orders-shell">
      <h2 class="page-title">我的订单</h2>

      <el-tabs v-model="activeStatus" @tab-change="onStatusChange" class="order-tabs">
        <el-tab-pane v-for="tab in STATUS_TABS" :key="tab.value" :label="tab.label" :name="tab.value" />
      </el-tabs>

      <div v-if="loading" class="loading-block short"></div>
      <div v-else-if="filteredOrders.length === 0" class="orders-empty">{{ activeStatus === 'ALL' ? '暂无订单' : '该状态下暂无订单' }}</div>
      <div v-else class="orders-list">
        <article v-for="order in filteredOrders" :key="order.id" class="order-card" @click="goDetail(order)" role="button">
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
              <el-button v-if="order.status === 'PENDING_PAYMENT'" plain type="danger" :loading="cancellingOrderId === order.id" @click.stop.prevent="handleCancel(order)">取消订单</el-button>
              <el-button v-if="order.status === 'PAID'" type="warning" :loading="refundingOrderId === order.id" @click.stop.prevent="handleRefund(order)">申请退款</el-button>
              <el-button v-if="order.status === 'SHIPPED'" type="primary" plain @click.stop.prevent="handleReceive(order)">确认收货</el-button>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { listOrders, payOrder, receiveOrder, cancelOrder, requestRefund } from "@/api/order";
import { getBalance, pay as walletPay } from "@/api/wallet";

const router = useRouter();
const orders = ref([]);
const loading = ref(true);
const payingOrderId = ref(null);
const cancellingOrderId = ref(null);
const refundingOrderId = ref(null);
const activeStatus = ref("ALL");

const STATUS_TABS = [
  { label: "全部", value: "ALL" },
  { label: "待支付", value: "PENDING_PAYMENT" },
  { label: "已支付", value: "PAID" },
  { label: "已发货", value: "SHIPPED" },
  { label: "已收货", value: "RECEIVED" },
  { label: "退款中", value: "REFUND_REQUESTED" },
  { label: "已退款", value: "REFUNDED" },
  { label: "已取消", value: "CANCELLED" }
];

const filteredOrders = computed(() => {
  if (activeStatus.value === "ALL") return orders.value;
  return orders.value.filter(o => o.status === activeStatus.value);
});

const onStatusChange = () => {
  // tab change triggers recompute via computed
};

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
  const total = Number(order.totalAmount || 0);
  const bal = await getBalance();
  if (total > 0 && bal < total) {
    try {
      await ElMessageBox.confirm(`余额不足（当前 ￥${bal.toFixed(2)}），需支付 ￥${total.toFixed(2)}。是否前往钱包充值？`, "余额不足", {
        confirmButtonText: "去充值",
        cancelButtonText: "取消",
        type: "warning",
        customClass: "pretty-confirm-box",
        distinguishCancelAndClose: true,
        center: true
      });
      router.push({ path: "/profile/wallet", query: { shortfall: (total - bal).toFixed(2) } });
    } catch (e) {
      // 取消
    }
    return;
  }
  payingOrderId.value = order.id;
  try {
    const res = await payOrder(order.id);
    if (total > 0) {
      await walletPay(total, res.data.orderNo);
    }
    ElMessage.success(`订单 ${res.data.orderNo} 支付成功` + (total > 0 ? `，已从钱包扣除 ￥${total.toFixed(2)}` : ""));
    await loadOrders();
  } finally {
    payingOrderId.value = null;
  }
};

const handleReceive = async (order) => {
  try {
    const res = await receiveOrder(order.id);
    if (res && res.data) {
      ElMessage.success('确认收货成功');
      await loadOrders();
    } else {
      ElMessage.error('确认收货失败');
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '确认收货失败');
  }
};

const handleCancel = async (order) => {
  try {
    await ElMessageBox.confirm(`确认取消订单「${order.orderNo}」？取消后订单状态将变为已取消。`, "确认取消订单", {
      confirmButtonText: "确认取消",
      cancelButtonText: "暂不取消",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  cancellingOrderId.value = order.id;
  try {
    await cancelOrder(order.id);
    ElMessage.success("订单已取消");
    await loadOrders();
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || "取消订单失败");
  } finally {
    cancellingOrderId.value = null;
  }
};

const handleRefund = async (order) => {
  try {
    await ElMessageBox.confirm(`确认对订单「${order.orderNo}」申请退款？提交后将由管理员审核。`, "确认申请退款", {
      confirmButtonText: "确认申请",
      cancelButtonText: "暂不申请",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  refundingOrderId.value = order.id;
  try {
    await requestRefund(order.id);
    ElMessage.success("退款申请已提交，请等待管理员审核");
    await loadOrders();
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || "申请退款失败");
  } finally {
    refundingOrderId.value = null;
  }
};

const goDetail = (order) => {
  router.push(`/orders/${order.id}`);
};

const statusText = (status) => {
  const map = {
    PENDING_PAYMENT: "待支付",
    PAID: "已支付",
    SHIPPED: "已发货",
    RECEIVED: "已收货",
    CANCELLED: "已取消",
    REFUND_REQUESTED: "退款中",
    REFUNDED: "已退款"
  };
  return map[status] || status;
};

const statusTagType = (status) => {
  if (status === "PAID") return "success";
  if (status === "PENDING_PAYMENT") return "warning";
  if (status === "SHIPPED") return "info";
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

.order-tabs {
  margin-bottom: 8px;
}

@media (max-width: 768px) {
  .orders-page { padding: 12px !important; }
  .page-title { font-size: 18px; margin-bottom: 12px; }
  .order-card { padding: 12px !important; border-radius: 10px; }
  .order-head { flex-direction: column; align-items: flex-start; gap: 8px; }
  .order-head .meta { gap: 4px; }
  .order-no { font-size: 13px; }
  .status-wrap { justify-content: flex-start; min-width: auto; }
  .items-list-item { grid-template-columns: 1fr auto auto; gap: 8px; font-size: 13px; }
  .item-name { font-size: 13px; }
  .order-footer { flex-direction: column; align-items: flex-start; gap: 10px; }
  .order-total { font-size: 15px; }
  .order-actions { flex-wrap: wrap; width: 100%; }
  .order-actions .el-button { flex: 1 1 auto; min-width: 0; }
  .order-tabs .el-tabs__nav-wrap { overflow-x: auto; }
}
</style>
