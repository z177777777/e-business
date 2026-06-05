<template>
  <div class="csr-applications-page">
    <h3>客服申请管理</h3>
    <el-card style="margin-top: 16px;">
      <el-table :data="applications" border stripe v-loading="loading" empty-text="暂无申请记录">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="note" label="备注" min-width="160" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审核</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已通过</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">未知</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 0"
              size="small"
              type="success"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 0"
              size="small"
              type="danger"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <span v-else style="color: #999; font-size: 13px;">已处理</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import http from "@/api/http";

const applications = ref([]);
const loading = ref(false);

const formatDate = (dateStr) => {
  if (!dateStr) return "";
  const d = new Date(dateStr);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
};

const loadApplications = async () => {
  loading.value = true;
  try {
    const res = await http.get("/api/admin/csr-applications");
    applications.value = res.data || [];
  } catch (e) {
    console.warn("load csr applications failed", e);
  } finally {
    loading.value = false;
  }
};

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(`确定通过 ${row.nickname || row.email} 的客服申请吗？`, "确认通过", {
      confirmButtonText: "确定通过",
      cancelButtonText: "取消",
      type: "info"
    });
  } catch {
    return;
  }
  try {
    await http.post(`/api/admin/csr-applications/${row.id}/approve`);
    ElMessage.success("已通过申请");
    loadApplications();
  } catch (e) {
    console.warn("approve failed", e);
  }
};

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(`确定拒绝 ${row.nickname || row.email} 的客服申请吗？`, "确认拒绝", {
      confirmButtonText: "确定拒绝",
      cancelButtonText: "取消",
      type: "warning"
    });
  } catch {
    return;
  }
  try {
    await http.post(`/api/admin/csr-applications/${row.id}/reject`);
    ElMessage.success("已拒绝申请");
    loadApplications();
  } catch (e) {
    console.warn("reject failed", e);
  }
};

onMounted(() => {
  loadApplications();
});
</script>

<style scoped>
.csr-applications-page {
  max-width: 100%;
}

h3 {
  margin: 0;
  font-size: 18px;
}
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
  .csr-applications-page {
    padding: 8px;
  }

  h3 {
    font-size: 16px;
  }

}

</style>
