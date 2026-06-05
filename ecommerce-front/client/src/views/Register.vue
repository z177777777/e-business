<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-hero">
        <h1>鍔犲叆 ShopPulse</h1>
        <p>鍒涘缓璐﹀彿锛岃В閿佹敹钘忎笌涓€у寲浼樻儬銆?/p>
        <p>楠岃瘉鐮佸皢鍙戦€佸埌浣犵殑閭銆?/p>
      </div>
      <div class="auth-form">
        <h2 class="auth-title">鍒涘缓璐﹀彿</h2>
        <!-- CSR 瀹℃壒閫氳繃妯箙 -->
        <div v-if="isCsrMode" class="csr-banner">
          绠＄悊鍛樺凡閫氳繃浣犵殑瀹㈡湇鐢宠锛屾敞鍐屽悗灏嗚嚜鍔ㄦ垚涓哄鏈嶃€?
        </div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="閭" prop="email">
              <el-input v-model="form.email" placeholder="璇疯緭鍏ラ偖绠? />
            </el-form-item>

            <!-- 鐢宠鎴愪负瀹㈡湇鎸夐挳锛堣緭鍏ラ偖绠卞悗鏄剧ず锛?-->
            <div v-if="form.email && !isCsrMode" class="csr-apply-row">
              <el-button
                type="warning"
                :loading="applyingCsr"
                :disabled="csrApplied"
                @click="handleApplyCsr"
              >
                {{ csrApplied ? '宸叉彁浜ょ敵璇? : '鐢宠鎴愪负瀹㈡湇' }}
              </el-button>
              <span v-if="csrApplied" class="csr-applied-tip">
                宸茶仈绯荤鐞嗗憳锛岃绛夊緟閭閫氱煡
              </span>
              <span v-else class="csr-apply-hint">
                闇€绠＄悊鍛樺鏍?
              </span>
            </div>

            <el-form-item label="楠岃瘉鐮? prop="code">
              <el-input v-model="form.code" placeholder="杈撳叆6浣嶉獙璇佺爜">
                <template #append>
                  <el-button :disabled="countdown > 0" @click="handleSendCode">
                    {{ countdown > 0 ? `${countdown}s` : "鍙戦€? }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="瀵嗙爜" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="璁剧疆瀵嗙爜" />
              <div class="password-strength" v-if="form.password">
                <span class="strength-label">瀵嗙爜寮哄害锛?/span>
                <span :class="['strength-value', `strength-${passwordStrength.level}`]">{{ passwordStrength.label }}</span>
                <span class="strength-tip">寤鸿鑷冲皯8浣嶏紝鍖呭惈澶у皬鍐欏瓧姣嶃€佹暟瀛楀拰绗﹀彿</span>
              </div>
            </el-form-item>
            <el-form-item label="鏄电О" prop="nickname">
              <el-input v-model="form.nickname" placeholder="閫夊～" />
            </el-form-item>
          </div>
          <el-button type="primary" color="#ff6a3d" :loading="loading" @click="handleRegister" style="width: 100%; margin-top: 18px;">
            娉ㄥ唽
          </el-button>
        </el-form>
        <div class="auth-footer">
          宸叉湁璐﹀彿锛?router-link to="/login">鍘荤櫥褰?/router-link>
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
  email: [{ required: true, message: "璇疯緭鍏ラ偖绠?, trigger: "blur" }],
  code: [{ required: true, message: "璇疯緭鍏ラ獙璇佺爜", trigger: "blur" }],
  password: [{ required: true, message: "璇疯緭鍏ュ瘑鐮?, trigger: "blur" }]
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
    ElMessage.warning("璇峰厛杈撳叆閭");
    return;
  }
  applyingCsr.value = true;
  try {
    await applyCsr(form.email);
    csrApplied.value = true;
    ElNotification.success({
      title: "鐢宠宸叉彁浜?,
      message: "宸茶仈绯荤鐞嗗憳锛岃绛夊緟閭閫氱煡",
      duration: 5000
    });
  } catch (e) {
    // 閿欒娑堟伅鐢辨嫤鎴櫒澶勭悊
    console.warn("apply csr failed", e);
  } finally {
    applyingCsr.value = false;
  }
};

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning("璇峰厛杈撳叆閭");
    return;
  }
  try {
    await sendCode(form.email, "REGISTER");
    ElMessage.success("楠岃瘉鐮佸凡鍙戦€?);
    startCountdown();
  } catch (e) {
    // 閿欒娑堟伅鐢辨嫤鎴櫒澶勭悊锛堝"璇ラ偖绠卞凡鐢宠鎴愪负瀹㈡湇锛屾棤娉曟敞鍐屼负鏅€氳处鎴?锛?
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
    ElMessage.success("娉ㄥ唽鎴愬姛");
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
