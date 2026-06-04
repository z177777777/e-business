<template>
  <div class="page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">订单管理</div>
      </template>

      <el-tabs v-model="activeStatus" class="admin-order-tabs">
        <el-tab-pane v-for="tab in STATUS_TABS" :key="tab.value" :label="tab.label" :name="tab.value" />
      </el-tabs>

      <el-table :data="filteredOrders" border style="width: 100%" @selection-change="onSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="productName" label="名称" min-width="160" />
        <el-table-column prop="userName" label="用户" min-width="120" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" link @click="() => handleView(scope.row)">查看</el-button>
            <el-button v-if="scope.row.status === 'REFUND_REQUESTED'" type="warning" link @click="() => handleApproveRefund(scope.row)">确认退款</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="admin-actions" style="margin-top:12px; display:flex; gap:8px;">
        <el-button type="warning" :disabled="!selected.length" @click="handleBatchShip">批量发货</el-button>
        <el-button type="success" :disabled="!selected.length" @click="handleBatchRefund">批量确认退款</el-button>
        <el-button type="danger" :disabled="!selected.length" @click="handleBatchDelete">批量删除</el-button>
        <div style="flex:1"></div>
        <div style="color:var(--text-secondary); align-self:center">已选：{{ selected.length }} 项</div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import http from "@/api/http";

const orders = ref([]);
const selected = ref([]);
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

const statusText = (status) => {
  const map = { PENDING_PAYMENT: "待支付", PAID: "已支付", SHIPPED: "已发货", RECEIVED: "已收货", CANCELLED: "已取消", REFUND_REQUESTED: "退款中", REFUNDED: "已退款" };
  return map[status] || status;
};

const statusTagType = (status) => {
  if (status === "PENDING_PAYMENT") return "warning";
  if (status === "PAID") return "";
  if (status === "SHIPPED") return "info";
  if (status === "RECEIVED") return "success";
  return "info";
};

const loadOrders = async () => {
  try {
    const resp = await http.get("/api/admin/orders");
    const data = resp.data || [];
    orders.value = data.map(o => ({
      id: o.id,
      orderNo: o.orderNo || o.id,
      productName: o.items && o.items.length ? o.items.map(i => i.productName).join('、') : '-',
      userName: o.userName || '-',
      userId: o.userId,
      totalAmount: o.totalAmount,
      amount: `￥${o.totalAmount}`,
      status: o.status,
      createdAt: o.createdAt
    }));
  } catch (e) {
    console.warn("load admin orders failed", e);
  }
};

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`订单「${row.orderNo}」将从数据库中永久删除，删除后无法恢复。`, "确认删除订单", {
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
  try {
    await http.delete(`/api/admin/orders/${row.id}`);
    ElMessage.success("订单已删除");
    await loadOrders();
  } catch (e) {
    const message = e?.response?.data?.message || e?.message || "删除失败";
    ElMessage.error(message);
    console.warn("delete admin order failed", e);
  }
};

onMounted(loadOrders);

const onSelectionChange = (rows) => {
  selected.value = Array.isArray(rows) ? rows : [];
};

const handleView = (row) => {
  // 跳转到管理员订单详情页（使用 admin 路径）
  try { window.location.href = `/admin/orders/${row.id}`; } catch (e) { /* ignore */ }
};

const handleBatchDelete = async () => {
  if (!selected.value.length) return;
  try {
    await ElMessageBox.confirm(`所选 ${selected.value.length} 个订单将被永久删除，删除后无法恢复。`, "确认批量删除", {
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
  try {
    // 并行删除
    await Promise.all(selected.value.map(s => http.delete(`/api/admin/orders/${s.id}`)));
    ElMessage.success("已删除所选订单");
    selected.value = [];
    await loadOrders();
  } catch (e) {
    const message = e?.response?.data?.message || e?.message || "批量删除失败";
    ElMessage.error(message);
    console.warn("batch delete admin orders failed", e);
  }
};

const handleBatchShip = async () => {
  if (!selected.value.length) return;
  try {
    await ElMessageBox.confirm(`确认要对所选 ${selected.value.length} 个订单执行发货操作吗？`, "确认发货", {
      confirmButtonText: "确认发货",
      cancelButtonText: "取消",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }

  // 处理已发货 / 已支付 / 未支付的组合：优先对 `PAID` 发货，已发货项跳过
  const paid = selected.value.filter(s => String(s.status).toUpperCase() === 'PAID');
  const shipped = selected.value.filter(s => String(s.status).toUpperCase() === 'SHIPPED');
  const paidCount = paid.length;
  const shippedCount = shipped.length;
  const unpaidCount = selected.value.length - paidCount - shippedCount;

  if (paidCount === 0) {
    // 如果全部已发货，告诉管理员“所选商品已发货”
    if (shippedCount === selected.value.length) {
      ElMessage.info('所选商品已发货');
      return;
    }
    // 含已发货与未支付的混合，提示已发货/未支付被跳过
    if (shippedCount > 0 && unpaidCount > 0) {
      ElMessage.warning(`${shippedCount} 个已发货订单已被跳过，${unpaidCount} 个未支付订单无法发货`);
      return;
    }
    // 全部未支付
    ElMessage.error('所选订单中没有已支付的订单，未支付订单无法发货');
    return;
  }

  // 确认对已支付的订单发货
  let success = 0;
  for (const s of paid) {
    try {
      await http.post(`/api/admin/orders/${s.id}/ship`);
      success++;
    } catch (e) {
      console.warn(`ship order ${s.id} failed`, e);
    }
  }

  if (success > 0) {
    ElMessage.success(`已对 ${success} 个已支付订单标记为已发货`);
    if (unpaidCount > 0) ElMessage.warning(`${unpaidCount} 个未支付订单已被跳过，未支付订单无法发货`);
  } else {
    ElMessage.error('发货操作未成功，请确认后端是否实现发货接口 /api/admin/orders/{id}/ship');
  }

  selected.value = [];
  await loadOrders();
};

const handleApproveRefund = async (row) => {
  try {
    await ElMessageBox.confirm(`确认对订单「${row.orderNo}」执行退款操作？退款后库存和销量将恢复。`, "确认退款", {
      confirmButtonText: "确认退款",
      cancelButtonText: "取消",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  try {
    await http.post(`/api/admin/orders/${row.id}/refund-approve`);
    ElMessage.success("退款已确认，金额已退回用户钱包");
    await loadOrders();
  } catch (e) {
    const message = e?.response?.data?.msg || e?.message || "退款确认失败";
    ElMessage.error(message);
  }
};

const handleBatchRefund = async () => {
  if (!selected.value.length) return;
  const refunding = selected.value.filter(s => s.status === 'REFUND_REQUESTED');
  if (refunding.length === 0) {
    ElMessage.warning("所选订单中没有待退款的订单");
    return;
  }
  try {
    await ElMessageBox.confirm(`确认对所选 ${refunding.length} 个订单执行退款操作？退款后库存和销量将恢复。`, "确认批量退款", {
      confirmButtonText: "确认退款",
      cancelButtonText: "取消",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  let success = 0;
  for (const s of refunding) {
    try {
      await http.post(`/api/admin/orders/${s.id}/refund-approve`);
      success++;
    } catch (e) {
      console.warn(`refund order ${s.id} failed`, e);
    }
  }
  if (success > 0) {
    ElMessage.success(`已确认 ${success} 个订单的退款`);
  } else {
    ElMessage.error("退款确认失败");
  }
  selected.value = [];
  await loadOrders();
};
</script>

<style scoped>
.card-header { font-weight: 600; }
.admin-order-tabs {
  margin-bottom: 8px;
}
</style>
