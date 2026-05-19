<template>
  <div class="page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">订单管理</div>
      </template>
      <el-table :data="orders" border style="width: 100%">
        <el-table-column prop="id" label="订单号" min-width="180" />
        <el-table-column prop="user" label="用户" min-width="160" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button type="primary" link>查看</el-button>
            <el-button type="success" link>发货</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
const orders = [
  { id: "OD20260519001", user: "user1@test.com", amount: "￥199", status: "待发货", createdAt: "2026-05-19 10:12" },
  { id: "OD20260519002", user: "user2@test.com", amount: "￥358", status: "待付款", createdAt: "2026-05-19 11:26" },
  { id: "OD20260518033", user: "user3@test.com", amount: "￥89", status: "已完成", createdAt: "2026-05-18 16:40" }
];
</script>

<style scoped>
.card-header { font-weight: 600; }
</style>
