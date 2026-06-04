<template>
  <div class="page">
    <el-alert
      v-if="focusEmail"
      :title="`已定位到：${focusEmail}`"
      type="success"
      :closable="false"
      style="margin-bottom: 12px"
    />
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">用户管理</div>
      </template>
      <el-alert
        title="提示：用户在找回密码页点击“联系管理员”后，会在首页看板出现待处理请求；也可以在这里直接重置密码。"
        type="info"
        :closable="false"
        style="margin-bottom: 12px"
      />
      <el-table :data="users" border style="width: 100%">
        <el-table-column type="index" label="ID" width="80" :index="indexMethod" />
        <el-table-column prop="email" label="邮箱" min-width="220" />
        <el-table-column prop="nickname" label="昵称" min-width="140" />
        <el-table-column prop="statusText" label="状态" width="120" />
        <el-table-column prop="lastLoginAt" label="最近登录" min-width="180" />
        <el-table-column label="操作" width="240">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button :type="row.status == 1 ? 'danger' : 'success'" size="small" @click="toggleStatus(row)">{{ row.status == 1 ? '禁用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :model-value="showDialog" title="编辑用户" width="560px" @close="closeDialog">
      <el-form :model="form" label-width="90px">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatarUrl" placeholder="可选" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :value="1" label="已启用" />
            <el-option :value="0" label="被禁用" />
          </el-select>
        </el-form-item>
        <el-form-item label="重置密码">
          <el-input v-model="form.newPassword" type="password" show-password placeholder="留空则不重置" />
          <div class="password-strength" v-if="form.newPassword">
            <span class="strength-label">密码强度：</span>
            <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
            <span class="strength-tip">建议至少8位，包含大小写字母、数字和符号</span>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import http from "@/api/http";

const route = useRoute();
const users = ref([]);
const showDialog = ref(false);
const editingId = ref(null);
const form = ref({ email: "", nickname: "", avatarUrl: "", status: 1, newPassword: "" });
const focusEmail = computed(() => String(route.query.focusEmail || "").trim().toLowerCase());

const calcPasswordStrength = (pwd) => {
  const value = String(pwd || "");
  if (!value) return { level: "none", label: "" };
  let score = 0;
  if (value.length >= 8) score += 1;
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score += 1;
  if (/\d/.test(value)) score += 1;
  if (/[^A-Za-z0-9]/.test(value)) score += 1;
  if (score <= 1) return { level: "weak", label: "弱" };
  if (score <= 3) return { level: "medium", label: "中" };
  return { level: "strong", label: "强" };
};

const passwordStrength = computed(() => calcPasswordStrength(form.value.newPassword));

const indexMethod = (index) => index + 1;

const loadUsers = async () => {
  try {
    const resp = await http.get("/api/admin/users");
    const data = resp.data || [];
    users.value = data.map(u => ({ id: u.id, email: u.email, nickname: u.nickname, avatarUrl: u.avatarUrl || "", status: u.status, statusText: u.status == 1 ? '已启用' : '被禁用', lastLoginAt: u.lastLoginAt }));
    if (focusEmail.value) {
      const matched = users.value.find(u => String(u.email || "").trim().toLowerCase() === focusEmail.value);
      if (matched) {
        openEdit(matched);
      }
    }
  } catch (e) {
    console.warn("load admin users failed", e);
  }
};

const toggleStatus = async (row) => {
  try {
    const target = row.status == 1 ? 0 : 1;
    await http.post(`/api/admin/users/${row.id}/status`, null, { params: { status: target } });
    await loadUsers();
  } catch (e) {
    console.warn('toggle user status failed', e);
  }
};

const openEdit = (row) => {
  editingId.value = row.id;
  form.value = {
    email: row.email || "",
    nickname: row.nickname || "",
    avatarUrl: row.avatarUrl || "",
    status: row.status == null ? 1 : Number(row.status),
    newPassword: ""
  };
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
};

const saveEdit = async () => {
  if (!editingId.value) return;
  try {
    await http.put(`/api/admin/users/${editingId.value}`, {
      email: form.value.email,
      nickname: form.value.nickname,
      avatarUrl: form.value.avatarUrl,
      status: form.value.status
    });

    const pwd = (form.value.newPassword || "").trim();
    if (pwd) {
      await http.post(`/api/admin/users/${editingId.value}/reset-password`, { newPassword: pwd });
      await http.post(`/api/admin/password-reset-requests/handled-by-email`, null, { params: { email: form.value.email } });
    }

    ElMessage.success(pwd ? "保存成功，密码已重置" : "保存成功");
    showDialog.value = false;
    await loadUsers();
  } catch (e) {
    console.warn("save user failed", e);
  }
};

onMounted(loadUsers);

watch(focusEmail, () => {
  if (users.value.length) {
    const matched = users.value.find(u => String(u.email || "").trim().toLowerCase() === focusEmail.value);
    if (matched) {
      openEdit(matched);
    }
  }
});
</script>

<style scoped>
.card-header { font-weight: 600; }

.password-strength {
  margin-top: 8px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
}

.strength-label,
.strength-tip {
  color: var(--text-secondary);
}

.strength-value {
  font-weight: 700;
}

.strength-weak {
  color: #e03131;
}

.strength-medium {
  color: #f08c00;
}

.strength-strong {
  color: #2f9e44;
}
</style>
