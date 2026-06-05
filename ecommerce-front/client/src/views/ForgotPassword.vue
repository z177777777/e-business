<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-hero">
        <h1>找回密码</h1>
        <p>验证码将发送到你的邮箱，用于重置密码。</p>
      </div>
      <div class="auth-form">
        <h2 class="auth-title">重置密码</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <el-input v-model="form.code" placeholder="输入6位验证码">
                <template #append>
                  <el-button :disabled="countdown > 0" @click="handleSendCode">
                    {{ countdown > 0 ? `${countdown}s` : "发送" }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
              <div class="password-strength" v-if="form.newPassword">
                <span class="strength-label">密码强度：</span>
                <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
                <span class="strength-tip">建议至少8位，包含大小写字母、数字和符号</span>
              </div>
            </el-form-item>
          </div>
          <el-button type="primary" color="#ff6a3d" :loading="loading" @click="handleReset" style="width: 100%; margin-top: 18px;">
            更新密码
          </el-button>
        </el-form>
        <div class="auth-footer">
          <el-button text type="primary" :loading="contactLoading" class="contact-admin-btn" @click="handleContactAdmin">联系管理员</el-button>
          <router-link to="/login" class="back-login-link">← 登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { resetPassword, requestPasswordResetSupport, sendCode } from "@/api/auth";

const router = useRouter();
const formRef = ref();
const loading = ref(false);
const contactLoading = ref(false);
const countdown = ref(0);
let timer = null;

const form = reactive({
  email: "",
  code: "",
  newPassword: ""
});

const rules = {
  email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
  newPassword: [{ required: true, message: "请输入新密码", trigger: "blur" }]
};

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

const passwordStrength = computed(() => calcPasswordStrength(form.newPassword));

const startCountdown = () => {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  await sendCode(form.email, "RESET");
  ElMessage.success("验证码已发送");
  startCountdown();
};

const handleReset = async () => {
  await formRef.value.validate();
  loading.value = true;
  try {
    await resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword
    });
    ElMessage.success("密码已更新");
    router.push("/login");
  } finally {
    loading.value = false;
  }
};

const handleContactAdmin = async () => {
  if (!form.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  contactLoading.value = true;
  try {
    await requestPasswordResetSupport({
      email: form.email,
      message: "用户在找回密码页面请求管理员协助重置密码"
    });
    ElMessage.success("已向管理员发送重置请求");
  } catch (e) {
    console.warn("request password reset support failed", e);
  } finally {
    contactLoading.value = false;
  }
};
</script>

<style scoped>
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

.auth-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.contact-admin-btn {
  padding: 0;
}

.back-login-link {
  margin-left: auto;
}

@media (max-width: 768px) {
  .auth-card { width: 100% !important; max-width: 100% !important; padding: 16px !important; border-radius: 0 !important; }
  .auth-page { padding: 12px !important; min-height: auto !important; }
  .el-input, .el-button { width: 100% !important; }
  h1, h2, h3 { font-size: 1.2em !important; }
}
</style>
