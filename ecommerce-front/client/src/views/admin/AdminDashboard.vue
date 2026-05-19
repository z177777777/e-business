<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in cards" :key="card.title">
        <el-card class="metric-card" shadow="hover">
          <div class="metric-title">{{ card.title }}</div>
          <div class="metric-value">{{ card.value }}</div>
          <div class="metric-desc">{{ card.desc }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :xs="24" :lg="14">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">最近运营动态</div>
          </template>
          <el-timeline>
            <el-timeline-item v-for="item in activities" :key="item.time" :timestamp="item.time">
              {{ item.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="10">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">后台快捷操作</div>
          </template>
          <div class="quick-links">
            <router-link to="/admin/users">进入用户管理</router-link>
            <router-link to="/admin/products">进入商品管理</router-link>
            <router-link to="/admin/orders">进入订单管理</router-link>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const cards = [
  { title: "今日访问", value: "1,284", desc: "较昨日 +12%" },
  { title: "新增用户", value: "36", desc: "较昨日 +8" },
  { title: "在售商品", value: "218", desc: "已上架商品数量" },
  { title: "待处理订单", value: "19", desc: "需要尽快处理" }
];

const activities = [
  { time: "09:30", text: "新增 3 位新用户完成注册" },
  { time: "11:10", text: "商品管理中新增 2 个上架商品" },
  { time: "14:20", text: "有 5 笔订单进入待发货状态" },
  { time: "17:00", text: "今日后台访问量达到峰值" }
];
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.metric-card { height: 100%; }
.metric-title { font-size: 14px; color: #6b7280; }
.metric-value { margin-top: 10px; font-size: 30px; font-weight: 700; color: #111827; }
.metric-desc { margin-top: 8px; font-size: 13px; color: #9ca3af; }
.card-header { font-weight: 600; }
.quick-links { display: flex; flex-direction: column; gap: 12px; }
.quick-links a { color: #2563eb; text-decoration: none; }
.mt-16 { margin-top: 16px; }
</style>
