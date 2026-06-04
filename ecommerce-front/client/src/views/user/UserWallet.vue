<template>
  <div class="page-card">
    <h2>我的钱包</h2>

    <div class="balance-section">
      <div class="balance-label">账户余额</div>
      <div class="balance-amount">￥{{ balance.toFixed(2) }}</div>
      <div class="top-up-row">
        <el-input-number
          v-model="topUpAmount"
          :min="1"
          :max="99999"
          :step="100"
          controls-position="right"
          style="width:180px"
          placeholder="充值金额"
        />
        <el-button type="primary" color="#ff6a3d" :loading="toppingUp" @click="handleTopUp">充值</el-button>
      </div>
    </div>

    <el-divider />

    <div class="tx-section">
      <h3>交易记录</h3>
      <div v-if="transactions.length === 0" class="empty-hint">暂无交易记录</div>
      <el-table v-else :data="transactions" style="width:100%">
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === '充值' || row.type === '退款' ? 'success' : 'danger'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="130">
          <template #default="{ row }">
            <span :style="{ color: row.amount >= 0 ? '#2f9e44' : '#e03131', fontWeight: 600 }">
              {{ row.amount >= 0 ? '+' : '' }}{{ row.amount.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" width="130">
          <template #default="{ row }">￥{{ row.balance.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="orderNo" label="关联订单" min-width="200">
          <template #default="{ row }">{{ row.orderNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="time" label="时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.time) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { getTransactions, topUp, syncRefundTransactions } from "@/api/wallet";

const route = useRoute();
const balance = ref(0);
const transactions = ref([]);
const topUpAmount = ref(100);
const toppingUp = ref(false);

const load = async () => {
  const result = await syncRefundTransactions();
  balance.value = result.balance;
  transactions.value = getTransactions();
  const sf = route.query.shortfall;
  if (sf) {
    topUpAmount.value = Math.ceil(Number(sf));
  }
};

onMounted(load);

const handleTopUp = async () => {
  if (topUpAmount.value <= 0) { ElMessage.warning("请输入充值金额"); return; }
  toppingUp.value = true;
  try {
    const result = await topUp(topUpAmount.value);
    balance.value = result.balance;
    transactions.value = getTransactions();
    ElMessage.success(`成功充值 ￥${topUpAmount.value}`);
  } finally {
    toppingUp.value = false;
  }
};

const formatTime = (v) => {
  if (!v) return "-";
  return new Date(v).toLocaleString();
};
</script>

<style scoped>
.page-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.page-card h2 { margin:0 0 20px; font-size:20px; }
.balance-section { display:flex; flex-direction:column; align-items:center; gap:12px; padding:20px 0; }
.balance-label { font-size:14px; color:var(--text-secondary); }
.balance-amount { font-size:40px; font-weight:800; color:#e4393c; letter-spacing:2px; }
.top-up-row { display:flex; align-items:center; gap:12px; margin-top:8px; }
.tx-section h3 { font-size:16px; margin:0 0 12px; }
.empty-hint { padding:40px 0; text-align:center; color:var(--text-secondary); }
</style>
