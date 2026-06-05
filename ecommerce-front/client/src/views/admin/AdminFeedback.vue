<template>
  <div class="page">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">用户反馈</div>
      </template>
      <el-empty v-if="!messages.length" description="暂无反馈信息" />
      <div v-else class="request-list">
        <!-- REVIEW type -->
        <div v-for="item in messages" :key="item.type + '_' + item.id" class="request-item is-clickable" @click="handleMessageClick(item)">
          <div class="request-main">
            <div class="request-info-row">
              <span class="request-title">{{ item.title }}</span>
              <el-tag :type="tagType(item.type)" size="small">{{ tagText(item.type) }}</el-tag>
            </div>
            <template v-if="item.type === 'REVIEW'">
              <div class="request-rating-row">
                <el-rate :model-value="item.rating" disabled size="small" />
              </div>
              <div class="request-review-content">{{ item.nickname }} 评价：{{ item.note }}</div>
            </template>
            <template v-else-if="isFeedbackType(item.type)">
              <div class="request-review-content">{{ item.nickname }}：{{ item.note }}</div>
            </template>
            <template v-else>
              <div class="request-note">{{ item.note }}</div>
            </template>
            <div class="request-time">{{ formatTime(item.createdAt) }}</div>
          </div>
          <div class="request-actions">
            <el-button v-if="item.type === 'PASSWORD_RESET'" size="small" type="primary" plain @click.stop="markHandled(item)">已处理</el-button>
            <el-button v-if="item.type === 'RESTOCK_REQUEST'" size="small" type="primary" plain @click.stop="goProduct(item)">去管理</el-button>
            <el-button v-if="item.type === 'REVIEW'" size="small" type="primary" plain @click.stop="goProduct(item)">查看商品</el-button>
            <el-button v-if="isFeedbackType(item.type)" size="small" type="primary" plain @click.stop="markFeedbackRead(item)">已读</el-button>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import http from "@/api/http";

const router = useRouter();
const messages = ref([]);

const loadMessages = async () => {
  try {
    const resp = await http.get("/api/admin/user-messages");
    messages.value = resp.data || [];
  } catch (e) {
    console.warn("load user messages failed", e);
    messages.value = [];
  }
};

const formatTime = (value) => {
  if (!value) return "";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString();
};

const markHandled = async (item) => {
  try {
    await http.post(`/api/admin/password-reset-requests/${item.id}/handled`);
    ElMessage.success("已标记为已处理");
    await loadMessages();
  } catch (e) {
    console.warn("mark password reset handled failed", e);
  }
};

const goProduct = (item) => {
  router.push({ path: "/admin/products", query: { reviewProductId: item.productId } });
};

const isFeedbackType = (type) => {
  return !["PASSWORD_RESET", "RESTOCK_REQUEST", "REVIEW"].includes(type);
};

const tagType = (type) => {
  if (type === "PASSWORD_RESET") return "warning";
  if (type === "RESTOCK_REQUEST") return "danger";
  if (type === "FEEDBACK") return "success";
  if (type === "SUGGESTION") return "warning";
  return "info";
};
const tagText = (type) => {
  if (type === "PASSWORD_RESET") return "密码重置";
  if (type === "RESTOCK_REQUEST") return "催上货";
  if (type === "FEEDBACK") return "用户反馈";
  if (type === "SUGGESTION") return "功能建议";
  return type;
};

const markFeedbackRead = async (item) => {
  try {
    await http.post(`/api/admin/feedback/${item.id}/read`);
    ElMessage.success("已标记为已读");
    await loadMessages();
  } catch (e) {
    console.warn("mark feedback read failed", e);
  }
};

const handleMessageClick = (item) => {
  if (item.type === "PASSWORD_RESET" && item.target) {
    router.push({ path: "/admin/users", query: { focusEmail: item.target } });
  }
};

onMounted(loadMessages);
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.card-header { font-weight: 600; }

.request-list {
  display: grid;
  gap: 12px;
}

.request-item {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.request-item.is-clickable {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}

.request-item.is-clickable:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px rgba(17, 24, 39, 0.08);
}

.request-main {
  min-width: 0;
}

.request-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.request-title {
  font-weight: 700;
  color: #111827;
  word-break: break-word;
}

.request-note,
.request-time {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.request-rating-row {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.request-review-content {
  margin-top: 4px;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}

.request-actions {
  flex-shrink: 0;
  display: flex;
  gap: 8px;
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
  .request-item {
    flex-direction: column;
    align-items: stretch;
  }

  .request-actions {
    justify-content: flex-start;
  }

  .request-main {
    min-width: 100%;
  }

}

</style>
