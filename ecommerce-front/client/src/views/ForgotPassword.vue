<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-hero">
        <h1>鎵惧洖瀵嗙爜</h1>
        <p>楠岃瘉鐮佸皢鍙戦€佸埌浣犵殑閭锛岀敤浜庨噸缃瘑鐮併€?/p>
      </div>
      <div class="auth-form">
        <h2 class="auth-title">閲嶇疆瀵嗙爜</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="閭" prop="email">
              <el-input v-model="form.email" placeholder="璇疯緭鍏ラ偖绠? />
            </el-form-item>
            <el-form-item label="楠岃瘉鐮? prop="code">
              <el-input v-model="form.code" placeholder="杈撳叆6浣嶉獙璇佺爜">
                <template #append>
                  <el-button :disabled="countdown > 0" @click="handleSendCode">
                    {{ countdown > 0 ? `${countdown}s` : "鍙戦€? }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="鏂板瘑鐮? prop="newPassword">
              <el-input v-model="form.newPassword" type="password" show-password placeholder="璇疯緭鍏ユ柊瀵嗙爜" />
              <div class="password-strength" v-if="form.newPassword">
                <span class="strength-label">瀵嗙爜寮哄害锛?/span>
                <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
                <span class="strength-tip">寤鸿鑷冲皯8浣嶏紝鍖呭惈澶у皬鍐欏瓧姣嶃€佹暟瀛楀拰绗﹀彿</span>
              </div>
            </el-form-item>
          </div>
          <el-button type="primary" color="#ff6a3d" :loading="loading" @click="handleReset" style="width: 100%; margin-top: 18px;">
            鏇存柊瀵嗙爜
          </el-button>
        </el-form>
        <div class="auth-footer">
          <el-button text type="primary" :loading="contactLoading" class="contact-admin-btn" @click="handleContactAdmin">鑱旂郴绠＄悊鍛?/el-button>
          <router-link to="/login" class="back-login-link">鈫?鐧诲綍</router-link>
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
  email: [{ required: true, message: "璇疯緭鍏ラ偖绠?, trigger: "blur" }],
  code: [{ required: true, message: "璇疯緭鍏ラ獙璇佺爜", trigger: "blur" }],
  newPassword: [{ required: true, message: "璇疯緭鍏ユ柊瀵嗙爜", trigger: "blur" }]
};

const calcPasswordStrength = (pwd) => {
  const value = String(pwd || "");
  if (!value) return { level: "none", label: "" };
  let score = 0;
  if (value.length >= 8) score += 1;
  if (/[a-z]/.test(value) && /[A-Z]/.test(value)) score += 1;
  if (/\d/.test(value)) score += 1;
  if (/[^A-Za-z0-9]/.test(value)) score += 1;
  if (score <= 1) return { level: "weak", label: "寮? };
  if (score <= 3) return { level: "medium", label: "涓? };
  return { level: "strong", label: "寮? };
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
    ElMessage.warning("璇峰厛杈撳叆閭");
    return;
  }
  await sendCode(form.email, "RESET");
  ElMessage.success("楠岃瘉鐮佸凡鍙戦€?);
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
    ElMessage.success("瀵嗙爜宸叉洿鏂?);
    router.push("/login");
  } finally {
    loading.value = false;
  }
};

const handleContactAdmin = async () => {
  if (!form.email) {
    ElMessage.warning("璇峰厛杈撳叆閭");
    return;
  }
  contactLoading.value = true;
  try {
    await requestPasswordResetSupport({
      email: form.email,
      message: "鐢ㄦ埛鍦ㄦ壘鍥炲瘑鐮侀〉闈㈣姹傜鐞嗗憳鍗忓姪閲嶇疆瀵嗙爜"
    });
    ElMessage.success("宸插悜绠＄悊鍛樺彂閫侀噸缃姹?);
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
  .auth-card {
    width: 100% !important;
    max-width: 100% !important;
    padding: 16px !important;
    border-radius: 0 !important;
  }
  .el-input, .el-button {
    width: 100% !important;
  }
  .auth-page {
    padding: 12px !important;
    min-height: auto !important;
  }
  h1, h2, h3 { font-size: 1.2em !important; }
}
</style>
