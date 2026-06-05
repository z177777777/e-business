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
        <div v-if="order.shippedAt">发货时间：{{ formatTime(order.shippedAt) }}</div>
        <div v-if="order.receivedAt">收货时间：{{ formatTime(order.receivedAt) }}</div>
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
          <el-button v-if="order.status === 'PENDING_PAYMENT' && !isAdmin" type="primary" color="#ff6a3d" :loading="paying" @click="pay">模拟支付</el-button>
          <el-button v-if="order.status === 'PENDING_PAYMENT' && !isAdmin" plain type="danger" :loading="cancelling" @click="cancel">取消订单</el-button>
          <el-button v-if="order.status === 'PAID' && !isAdmin" type="warning" :loading="refunding" @click="refund">申请退款</el-button>
          <el-button v-if="order.status === 'SHIPPED' && !isAdmin" type="primary" plain @click="receive">确认收货</el-button>
          <template v-if="order.status === 'RECEIVED' && !isAdmin">
            <el-button v-for="item in order.items" :key="'r' + item.id" @click="openReview(item)">写评价</el-button>
          </template>
        </div>
      </div>
    </section>
      <el-dialog title="写评价" v-model="reviewVisible" width="640px">
        <div v-if="reviewProduct">
          <div style="margin-bottom:8px">商品：{{ reviewProduct.productName }}</div>
          <el-rate v-model="reviewForm.rating" :max="5"></el-rate>
          <el-input type="textarea" v-model="reviewForm.content" rows="4" placeholder="写下你的评价..." style="margin-top:8px"></el-input>
          <div style="margin-top:8px">
            <el-upload
              :http-request="handleImageUpload"
              :show-file-list="false"
              multiple
            >
              <el-button size="small">上传图片</el-button>
            </el-upload>
            <div style="display:flex; gap:8px; margin-top:8px">
              <div v-for="(img, idx) in reviewForm.images" :key="idx" style="position:relative">
                <img :src="img" style="width:96px;height:96px;object-fit:cover;border:1px solid #eee;border-radius:4px" />
                <el-button size="mini" type="danger" style="position:absolute;right:4px;top:4px" @click="removeReviewImage(idx)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
        <template #footer>
          <el-button @click="reviewVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReview">提交评价</el-button>
        </template>
      </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import http from '@/api/http';
import { getOrder, payOrder, receiveOrder, cancelOrder, requestRefund } from '@/api/order';
import { getBalance, pay as walletPay } from '@/api/wallet';
import { uploadImage } from '@/api/files';
import { submitProductReview } from '@/api/reviews';

const route = useRoute();
const router = useRouter();
const id = Number(route.params.id);
const order = ref(null);
const loading = ref(true);
const paying = ref(false);
const cancelling = ref(false);
const refunding = ref(false);
const isAdmin = route.path && route.path.startsWith('/admin');

// review dialog state
const reviewVisible = ref(false);
const reviewProduct = ref(null);
const reviewForm = ref({ rating: 5, content: '', images: [] });

const handleImageUpload = async (options) => {
  const file = options.file;
  try {
    const res = await uploadImage(file);
    const url = res.data;
    reviewForm.value.images.push(url);
    ElMessage.success('图片上传成功');
  } catch (e) {
    ElMessage.error('图片上传失败');
  }
};

const removeReviewImage = (index) => {
  reviewForm.value.images.splice(index, 1);
};

const openReview = (item) => {
  reviewProduct.value = item;
  reviewForm.value = { rating: 5, content: '', images: [] };
  reviewVisible.value = true;
};

const submitReview = async () => {
  if (!reviewProduct.value) return;
  if (!reviewForm.value.content || !reviewForm.value.rating) {
    ElMessage.warning('请输入评分和评价内容');
    return;
  }
  try {
    await submitProductReview(reviewProduct.value.productId || reviewProduct.value.productIdentifier || reviewProduct.value.id, {
      rating: Number(reviewForm.value.rating),
      content: reviewForm.value.content,
      imageUrls: reviewForm.value.images
    });
    ElMessage.success('评价提交成功');
    reviewVisible.value = false;
  } catch (e) {
    ElMessage.error('评价提交失败');
  }
};

const load = async () => {
  loading.value = true;
  try {
    // For admin routes call admin API explicitly to avoid pathname detection edge cases
    const res = isAdmin ? await http.get(`/api/admin/orders/${id}`) : await getOrder(id);
    order.value = res.data || null;
  } finally {
    loading.value = false;
  }
};

onMounted(load);

const formatMoney = (v) => Number(v || 0).toFixed(2);
const formatTime = (v) => (v ? new Date(v).toLocaleString() : '-');

const statusText = (s) => ({ PENDING_PAYMENT: '待支付', PAID: '已支付', SHIPPED: '已发货', RECEIVED: '已收货', CANCELLED: '已取消', REFUND_REQUESTED: '退款中', REFUNDED: '已退款' }[s] || s);

const pay = async () => {
  if (!order.value) return;
  const total = Number(order.value.totalAmount || 0);
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
  paying.value = true;
  try {
    const res = await payOrder(order.value.id);
    if (total > 0) {
      await walletPay(total, res.data.orderNo);
    }
    order.value = res.data;
    ElMessage.success(`订单 ${res.data.orderNo} 支付成功` + (total > 0 ? `，已从钱包扣除 ￥${total.toFixed(2)}` : ""));
  } finally { paying.value = false; }
};

const receive = async () => {
  if (!order.value) return;
  try {
    const res = await receiveOrder(order.value.id);
    if (res && res.data) {
      ElMessage.success('确认收货成功');
      order.value = res.data;
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || '确认收货失败');
  }
};

const cancel = async () => {
  if (!order.value) return;
  try {
    await ElMessageBox.confirm(`确认取消订单「${order.value.orderNo}」？取消后订单状态将变为已取消。`, "确认取消订单", {
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
  cancelling.value = true;
  try {
    await cancelOrder(order.value.id);
    ElMessage.success("订单已取消");
    router.push("/orders");
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || "取消订单失败");
  } finally {
    cancelling.value = false;
  }
};

const refund = async () => {
  if (!order.value) return;
  try {
    await ElMessageBox.confirm(`确认对订单「${order.value.orderNo}」申请退款？提交后将由管理员审核。`, "确认申请退款", {
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
  refunding.value = true;
  try {
    await requestRefund(order.value.id);
    ElMessage.success("退款申请已提交，请等待管理员审核");
    await load();
  } catch (e) {
    ElMessage.error(e?.response?.data?.msg || "申请退款失败");
  } finally {
    refunding.value = false;
  }
};

// review dialog state is defined above
</script>

<style scoped>
.order-detail-page { padding: 28px 24px 80px; min-height: 100vh }
.meta-row { display:flex; gap:20px; margin:12px 0; color:var(--text-secondary) }
.items-table { width:100%; border-collapse:collapse; margin-top:12px }
.items-table th, .items-table td { padding:12px; border-bottom:1px solid rgba(0,0,0,0.06); text-align:left }
.detail-footer { display:flex; justify-content:space-between; align-items:center; margin-top:16px }
.total { font-weight:700; font-size:18px }

@media (max-width: 768px) {
  .order-detail-page { padding: 12px !important; }
  .detail-shell h2 { font-size: 16px; }
  .meta-row { flex-direction: column; gap: 8px; font-size: 13px; }
  .items-table { font-size: 12px; }
  .items-table th, .items-table td { padding: 8px 6px; }
  .items-table thead { display: none; }
  .items-table tbody tr { display: flex; flex-direction: column; border-bottom: 1px solid rgba(0,0,0,0.08); padding: 8px 0; }
  .items-table tbody td { display: flex; justify-content: space-between; border-bottom: none; padding: 4px 6px; }
  .items-table tbody td::before { content: attr(data-label); font-weight: 600; color: var(--text-secondary); }
  .detail-footer { flex-direction: column; align-items: flex-start; gap: 12px; }
  .total { font-size: 16px; }
  .detail-footer .actions { display: flex; flex-wrap: wrap; gap: 8px; width: 100%; }
  .detail-footer .actions .el-button { flex: 1 1 auto; min-width: 0; }
}
</style>
