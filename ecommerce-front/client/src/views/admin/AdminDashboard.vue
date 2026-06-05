<template>
  <div class="page">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6" v-for="card in cards" :key="card.title">
        <el-card
          class="metric-card"
          :class="{ 'is-clickable': !!card.route }"
          shadow="hover"
          @click="handleCardClick(card)"
        >
          <div class="metric-title">{{ card.title }}</div>
          <div v-if="card.title === '订单数量'" class="metric-order">
            <div class="metric-value">{{ card.value }}</div>
            <div class="metric-subvalue">待处理 {{ card.pending }}</div>
          </div>
          <div v-else class="metric-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 饼图行 -->
    <el-row :gutter="16" class="mt-16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><div class="card-header">订单状态分布</div></template>
          <div ref="chartOrderStatusRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><div class="card-header">商品分类分布</div></template>
          <div ref="chartCategoryRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-16">
      <el-col :span="24">
        <div class="time-toggle">
          <el-radio-group v-model="chartDays" @change="onDaysChange">
            <el-radio-button :value="7">近7天</el-radio-button>
            <el-radio-button :value="30">近30天</el-radio-button>
          </el-radio-group>
        </div>
      </el-col>
    </el-row>

    <!-- 折线图 + 柱状图行 -->
    <el-row :gutter="16" class="mt-16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><div class="card-header">订单</div></template>
          <div ref="chartOrderTrendRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover">
          <template #header><div class="card-header">销售额</div></template>
          <div ref="chartSalesTrendRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 日历热力图行 -->
    <el-row :gutter="16" class="mt-16">
      <el-col :xs="24" :lg="24">
        <el-card shadow="hover">
          <template #header><div class="card-header">近90天订单热度</div></template>
          <div ref="chartCalendarRef" class="chart-box chart-box-calendar"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 最近动态 -->
    <el-row :gutter="16" class="mt-16">
        <el-col :xs="24" :lg="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">最近动态</div>
          </template>
          <el-timeline>
            <el-timeline-item v-for="(item, idx) in activities" :key="`${item.time}-${idx}`" :timestamp="formatTime(item.time)">
              {{ item.text }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, nextTick, ref, shallowRef } from "vue";
import { useRouter } from "vue-router";
import * as echarts from "echarts";
import http from "@/api/http";

const router = useRouter();
const activities = ref([]);
const chartDays = ref(7);
const cards = ref([
  { title: "今日访问", value: "1,284" },
  { title: "用户数量", value: "--", route: "/admin/users" },
  { title: "在售商品", value: "--", route: "/admin/products" },
  { title: "订单数量", value: "--", pending: "--", route: "/admin/orders" }
]);

const chartOrderStatusRef = ref(null);
const chartCategoryRef = ref(null);
const chartOrderTrendRef = ref(null);
const chartSalesTrendRef = ref(null);
const chartCalendarRef = ref(null);

const chartInstances = {
  orderStatus: shallowRef(null),
  category: shallowRef(null),
  orderTrend: shallowRef(null),
  salesTrend: shallowRef(null),
  calendar: shallowRef(null)
};

const loadStats = async () => {
  try {
    const resp = await http.get("/api/admin/dashboard-stats");
    const data = resp.data || {};
    cards.value = [
          { title: "今日访问", value: String(data.pvToday ?? 0) },
          { title: "用户数量", value: String(data.userCount ?? 0), route: "/admin/users" },
          { title: "在售商品", value: String(data.productCount ?? 0), route: "/admin/products" },
          { title: "订单数量", value: String(data.orderCount ?? 0), pending: String(data.pendingOrderCount ?? 0), route: "/admin/orders" }
    ];
    await loadActivities();
  } catch (e) {
    console.warn("load dashboard stats failed", e);
  }
};

const loadActivities = async () => {
  try {
    const resp = await http.get("/api/admin/recent-activities", { params: { limit: 8 } });
    const data = resp.data || [];
    activities.value = Array.isArray(data) ? data : [];
  } catch (e) {
    console.warn("load recent activities failed", e);
    activities.value = [];
  }
};

const formatTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString();
};

const handleCardClick = (card) => {
  if (!card?.route) return;
  router.push(card.route);
};

const loadChartData = async () => {
  try {
    const resp = await http.get("/api/admin/chart-data", { params: { days: chartDays.value } });
    const d = resp.data || {};

    // 订单状态分布饼图
    if (chartInstances.orderStatus.value) {
      const statusMap = d.orderStatusDistribution || {};
      chartInstances.orderStatus.value.setOption({
        tooltip: { trigger: "item" },
        legend: { orient: "vertical", right: 10, top: "center" },
        series: [{
          type: "pie",
          radius: ["40%", "70%"],
          avoidLabelOverlap: false,
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 16, fontWeight: "bold" } },
          data: Object.entries(statusMap).map(([name, value]) => ({ name, value }))
        }]
      });
    }

    // 商品分类分布饼图
    if (chartInstances.category.value) {
      const catMap = d.productCategoryDistribution || {};
      chartInstances.category.value.setOption({
        tooltip: { trigger: "item" },
        legend: { orient: "vertical", right: 10, top: "center" },
        series: [{
          type: "pie",
          radius: ["40%", "70%"],
          avoidLabelOverlap: false,
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 16, fontWeight: "bold" } },
          data: Object.entries(catMap).map(([name, value]) => ({ name, value }))
        }]
      });
    }

    // 订单趋势折线图
    if (chartInstances.orderTrend.value) {
      const ot = d.orderTrend || {};
      chartInstances.orderTrend.value.setOption({
        tooltip: { trigger: "axis" },
        xAxis: { type: "category", data: ot.labels || [] },
        yAxis: { type: "value", minInterval: 1 },
        series: [{
          type: "line",
          data: ot.values || [],
          smooth: true,
          areaStyle: { opacity: 0.15 }
        }]
      });
    }

    // 销售额趋势柱状图
    if (chartInstances.salesTrend.value) {
      const st = d.salesTrend || {};
      chartInstances.salesTrend.value.setOption({
        tooltip: { trigger: "axis", valueFormatter: (v) => "¥" + (Number(v) || 0).toFixed(2) },
        xAxis: { type: "category", data: st.labels || [] },
        yAxis: { type: "value" },
        series: [{
          type: "bar",
          data: st.values || [],
          itemStyle: { borderRadius: [4, 4, 0, 0] }
        }]
      });
    }

  } catch (e) {
    console.warn("load chart data failed", e);
  }
};

const loadCalendarHeatmap = async () => {
  try {
    const resp = await http.get("/api/admin/chart-data", { params: { days: 7 } });
    const d = resp.data || {};
    const heatmapData = d.calendarHeatmap || [];

    if (!chartInstances.calendar.value) return;

    const endDate = new Date();
    endDate.setHours(23, 59, 59, 999);
    const startDate = new Date();
    startDate.setDate(startDate.getDate() - 89);
    startDate.setHours(0, 0, 0, 0);

    const fmt = (d) => {
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const day = String(d.getDate()).padStart(2, "0");
      return `${y}-${m}-${day}`;
    };

    const values = heatmapData.map(([dateStr, count]) => [dateStr, Number(count)]);
    const maxVal = values.length > 0 ? Math.max(...values.map(v => v[1]), 1) : 1;

    chartInstances.calendar.value.setOption({
      tooltip: { formatter: (p) => `${p.value[0]}<br/>订单数: ${p.value[1]}` },
      visualMap: {
        min: 0,
        max: maxVal,
        calculable: true,
        orient: "horizontal",
        left: "center",
        bottom: 0,
        inRange: { color: ["#ebedf0", "#c6e48b", "#7bc96f", "#239a3b", "#196127"] }
      },
      calendar: {
        top: 20,
        left: 30,
        right: 20,
        bottom: 60,
        range: [fmt(startDate), fmt(endDate)],
        cellSize: ["auto", 16],
        yearLabel: { show: true },
        dayLabel: { firstDay: 1 },
        monthLabel: { show: true }
      },
      series: [{
        type: "heatmap",
        coordinateSystem: "calendar",
        data: values
      }]
    });
  } catch (e) {
    console.warn("load calendar heatmap failed", e);
  }
};

const initCharts = () => {
  if (chartOrderStatusRef.value) {
    chartInstances.orderStatus.value = echarts.init(chartOrderStatusRef.value);
  }
  if (chartCategoryRef.value) {
    chartInstances.category.value = echarts.init(chartCategoryRef.value);
  }
  if (chartOrderTrendRef.value) {
    chartInstances.orderTrend.value = echarts.init(chartOrderTrendRef.value);
  }
  if (chartSalesTrendRef.value) {
    chartInstances.salesTrend.value = echarts.init(chartSalesTrendRef.value);
  }
  if (chartCalendarRef.value) {
    chartInstances.calendar.value = echarts.init(chartCalendarRef.value);
  }
};

const disposeCharts = () => {
  Object.values(chartInstances).forEach(inst => {
    if (inst.value) {
      inst.value.dispose();
      inst.value = null;
    }
  });
};

const onResize = () => {
  Object.values(chartInstances).forEach(inst => {
    inst.value?.resize();
  });
};

const onDaysChange = () => {
  loadChartData();
};

onMounted(async () => {
  await loadStats();
  await nextTick();
  initCharts();
  await nextTick();
  await loadChartData();
  await loadCalendarHeatmap();
  window.addEventListener("resize", onResize);
});

onUnmounted(() => {
  window.removeEventListener("resize", onResize);
  disposeCharts();
});
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.metric-card { height: 100%; }
.metric-card.is-clickable {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.metric-card.is-clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.08);
}
.metric-title { font-size: 14px; color: #6b7280; }
.metric-value { margin-top: 10px; font-size: 30px; font-weight: 700; color: #111827; }
.metric-order { margin-top: 10px; display: flex; flex-direction: column; gap: 4px; }
.metric-subvalue { font-size: 14px; color: #dc2626; font-weight: 600; }
.card-header { font-weight: 600; }
.quick-links { display: flex; flex-direction: column; gap: 12px; }
.quick-links a { color: #2563eb; text-decoration: none; }
.mt-16 { margin-top: 16px; }
.chart-box { width: 100%; height: 320px; }
.chart-box-calendar { height: 200px; }
.time-toggle { display: flex; justify-content: flex-end; }
@media (max-width: 768px) {
  .el-table__body-wrapper {
    overflow-x: auto;
  }

  .el-form-item {
    display: block;
    margin-bottom: 12px;
  }

  .el-form-item__label {
    width: 100% !important;
    text-align: left !important;
  }

  .el-form-item__content {
    width: 100% !important;
  }

  .page-header {
    flex-direction: column;
    gap: 8px;
  }

  .filter-bar {
    flex-direction: column;
    gap: 8px;
  }

  .el-button + .el-button {
    margin-left: 0 !important;
    margin-top: 8px;
  }

  .el-pagination {
    justify-content: center;
    flex-wrap: wrap;
  }
}

</style>
