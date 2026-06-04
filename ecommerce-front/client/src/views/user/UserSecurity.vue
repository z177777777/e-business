<template>
  <div class="page-card">
    <h2>账户安全</h2>

    <div class="section">
      <h3>修改邮箱</h3>
      <el-form :model="emailForm" label-position="top" style="max-width:480px">
        <el-form-item label="新邮箱">
          <el-input v-model="emailForm.newEmail" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="emailForm.code" placeholder="6位验证码">
            <template #append>
              <el-button :disabled="emailCountdown > 0" @click="sendEmailCode">
                {{ emailCountdown > 0 ? `${emailCountdown}s` : "发送" }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-button type="primary" color="#ff6a3d" :loading="savingEmail" @click="saveEmail">更新邮箱</el-button>
      </el-form>
    </div>

    <el-divider />

    <div class="section">
      <h3>修改密码</h3>
      <el-form :model="passwordForm" label-position="top" style="max-width:480px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
          <div class="password-strength" v-if="passwordForm.newPassword">
            <span class="strength-label">密码强度：</span>
            <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
            <span class="strength-tip">建议至少8位，包含大小写字母、数字和符号</span>
          </div>
        </el-form-item>
        <el-button type="primary" color="#ff6a3d" :loading="savingPassword" @click="savePassword">更新密码</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { sendCode } from "@/api/auth";
import { updateEmail, updatePassword } from "@/api/user";
import { useAuthStore } from "@/store/auth";

const authStore = useAuthStore();

const emailForm = reactive({ newEmail: "", code: "" });
const savingEmail = ref(false);
const emailCountdown = ref(0);
let emailTimer = null;

const passwordForm = reactive({ oldPassword: "", newPassword: "" });
const savingPassword = ref(false);

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

const passwordStrength = computed(() => calcPasswordStrength(passwordForm.newPassword));

const startEmailCountdown = () => {
  emailCountdown.value = 60;
  emailTimer = setInterval(() => {
    emailCountdown.value -= 1;
    if (emailCountdown.value <= 0) clearInterval(emailTimer);
  }, 1000);
};

const sendEmailCode = async () => {
  if (!emailForm.newEmail) { ElMessage.warning("请先输入新邮箱"); return; }
  await sendCode(emailForm.newEmail, "CHANGE_EMAIL");
  ElMessage.success("验证码已发送");
  startEmailCountdown();
};

const saveEmail = async () => {
  savingEmail.value = true;
  try {
    const res = await updateEmail({ newEmail: emailForm.newEmail, code: emailForm.code });
    authStore.updateUser(res.data);
    emailForm.newEmail = ""; emailForm.code = "";
    ElMessage.success("邮箱已更新");
  } finally { savingEmail.value = false; }
};

const savePassword = async () => {
  savingPassword.value = true;
  try {
    await updatePassword({ oldPassword: passwordForm.oldPassword, newPassword: passwordForm.newPassword });
    passwordForm.oldPassword = ""; passwordForm.newPassword = "";
    ElMessage.success("密码已更新");
  } finally { savingPassword.value = false; }
};

onUnmounted(() => { if (emailTimer) clearInterval(emailTimer); });
</script>

<style scoped>
.page-card { background:#fff; border-radius:14px; padding:28px; box-shadow:0 1px 4px rgba(0,0,0,0.06); }
.page-card h2 { margin:0 0 20px; font-size:20px; }
.section { margin-bottom: 8px; }
.section h3 { font-size: 16px; margin: 0 0 12px; color: var(--text-primary); }

.password-strength { margin-top:8px; display:flex; align-items:center; flex-wrap:wrap; gap:8px; font-size:12px; }
.strength-label, .strength-tip { color: var(--text-secondary); }
.strength-value { font-weight:700; }
.strength-weak { color:#e03131; }
.strength-medium { color:#f08c00; }
.strength-strong { color:#2f9e44; }
</style>
