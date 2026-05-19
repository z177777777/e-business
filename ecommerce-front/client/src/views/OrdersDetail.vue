<template>
  <div class="order-detail-page">
    <!-- 顶部导航已移除：不显示返回与刷新按钮 -->

    <section v-if="loading" class="loading-block short"></section>
    <section v-else-if="!order" class="orders-empty">订单不存在</section>
    <section v-else class="detail-shell">
      <h2>订单详情 - {{ order.orderNo }}</h2>
      <div class="meta-row">
        <div>状态：<strong>{{ statusText(order.status) }}</strong></div>
        <div>创建时间：{{ formatTime(order.createdAt) }}</div>
        <div v-if="order.paidAt">支付时间：{{ formatTime(order.paidAt) }}</div>
      </div>

      <table class="items-table">
        <thead>
          <tr><th>商品</th><th>单价</th><th>数量</th><th>小计</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in order.items" :key="item.id">
            <td>{{ item.productName }}</td>
            <td>￥{{ formatMoney(item.productPrice) }}</td>
            <td>×{{ item.quantity }}</td>
            <td>￥{{ formatMoney(item.subtotal) }}</td>
          </tr>
        </tbody>
      </table>

      <div class="detail-footer">
        <div class="total">总金额：￥{{ formatMoney(order.totalAmount) }}</div>
        <div class="actions">
          <el-button v-if="order.status === 'PENDING_PAYMENT'" type="primary" color="#ff6a3d" :loading="paying" @click="pay">模拟支付</el-button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getOrder, payOrder } from '@/api/order';

const route = useRoute();
const router = useRouter();
const id = Number(route.params.id);
const order = ref(null);
const loading = ref(true);
const paying = ref(false);

const load = async () => {
  loading.value = true;
  try {
    const res = await getOrder(id);
    order.value = res.data || null;
  } finally {
    loading.value = false;
  }
};

onMounted(load);

const formatMoney = (v) => Number(v || 0).toFixed(2);
const formatTime = (v) => (v ? new Date(v).toLocaleString() : '-');

const statusText = (s) => ({ PENDING_PAYMENT: '待支付', PAID: '已支付' }[s] || s);

const pay = async () => {
  if (!order.value) return;
  paying.value = true;
  try {
    const res = await payOrder(order.value.id);
    ElMessage.success(`订单 ${res.data.orderNo} 支付成功`);
    // 不自动刷新详情页面，用户可手动返回列表查看状态
  } finally { paying.value = false; }
};
</script>

<style scoped>
.order-detail-page { padding: 28px 24px 80px; min-height: 100vh }
.meta-row { display:flex; gap:20px; margin:12px 0; color:var(--text-secondary) }
.items-table { width:100%; border-collapse:collapse; margin-top:12px }
.items-table th, .items-table td { padding:12px; border-bottom:1px solid rgba(0,0,0,0.06); text-align:left }
.detail-footer { display:flex; justify-content:space-between; align-items:center; margin-top:16px }
.total { font-weight:700; font-size:18px }
</style>
