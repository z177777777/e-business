<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-hero">
        <h1>加入 ShopPulse</h1>
        <p>创建账号，解锁收藏与个性化优惠。</p>
        <p>验证码将发送到你的邮箱。</p>
      </div>
      <div class="auth-form">
        <h2 class="auth-title">创建账号</h2>
        <!-- CSR 审批通过横幅 -->
        <div v-if="isCsrMode" class="csr-banner">
          管理员已通过你的客服申请，注册后将自动成为客服。
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>

            <!-- 申请成为客服按钮（输入邮箱后显示） -->
            <div v-if="form.email && !isCsrMode" class="csr-apply-row">
              <el-button
                type="warning"
                :loading="applyingCsr"
                :disabled="csrApplied"
                @click="handleApplyCsr"
              >
                {{ csrApplied ? '已提交申请' : '申请成为客服' }}
              </el-button>
              <span v-if="csrApplied" class="csr-applied-tip">
                已联系管理员，请等待邮箱通知
              </span>
              <span v-else class="csr-apply-hint">
                需管理员审核
              </span>
            </div>

            <el-form-item label="验证码" prop="code">
              <el-input v-model="form.code" placeholder="输入6位验证码">
                <template #append>
                  <el-button :disabled="countdown > 0" @click="handleSendCode">
                    {{ countdown > 0 ? `${countdown}s` : "发送" }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="设置密码" />
              <div class="password-strength" v-if="form.password">
                <span class="strength-label">密码强度：</span>
                <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
                <span class="strength-tip">建议至少8位，包含大小写字母、数字和符号</span>
              </div>
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="form.nickname" placeholder="选填" />
            </el-form-item>
          </div>
          <el-button type="primary" color="#ff6a3d" :loading="loading" @click="handleRegister" style="width: 100%; margin-top: 18px;">
            注册
          </el-button>
        </el-form>
        <div class="auth-footer">
          已有账号？<router-link to="/login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage, ElNotification } from "element-plus";
import { register, sendCode, applyCsr } from "@/api/auth";
import { useAuthStore } from "@/store/auth";

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const formRef = ref();
const loading = ref(false);
const countdown = ref(0);
const applyingCsr = ref(false);
const csrApplied = ref(false);
let timer = null;

const isCsrMode = computed(() => route.query.csr === "true");

const form = reactive({
  email: "",
  code: "",
  password: "",
  nickname: ""
});

const rules = {
  email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  code: [{ required: true, message: "请输入验证码", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
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

const passwordStrength = computed(() => calcPasswordStrength(form.password));

const startCountdown = () => {
  countdown.value = 60;
  timer = setInterval(() => {
    countdown.value -= 1;
    if (countdown.value <= 0) {
      clearInterval(timer);
    }
  }, 1000);
};

const handleApplyCsr = async () => {
  if (!form.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  applyingCsr.value = true;
  try {
    await applyCsr(form.email);
    csrApplied.value = true;
    ElNotification.success({
      title: "申请已提交",
      message: "已联系管理员，请等待邮箱通知",
      duration: 5000
    });
  } catch (e) {
    // 错误消息由拦截器处理
    console.warn("apply csr failed", e);
  } finally {
    applyingCsr.value = false;
  }
};

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }
  try {
    await sendCode(form.email, "REGISTER");
    ElMessage.success("验证码已发送");
    startCountdown();
  } catch (e) {
    // 错误消息由拦截器处理（如"该邮箱已申请成为客服，无法注册为普通账户"）
    console.warn("send code failed", e);
  }
};

const handleRegister = async () => {
  await formRef.value.validate();
  loading.value = true;
  try {
    const res = await register({
      email: form.email,
      code: form.code,
      password: form.password,
      nickname: form.nickname
    });
    authStore.setSession(res.data.token, res.data.user, true);
    authStore.setRememberedEmail(form.email);
    const role = res.data.user?.role;
    ElMessage.success("注册成功");
    if (role === "CSR") {
      router.push("/chat");
    } else {
      router.push("/home");
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.csr-banner {
  background: #e8f5e9;
  color: #2e7d32;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 14px;
  font-weight: 500;
}

.csr-apply-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.csr-applied-tip {
  font-size: 13px;
  color: #2e7d32;
  font-weight: 500;
}

.csr-apply-hint {
  font-size: 12px;
  color: #999;
}

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
