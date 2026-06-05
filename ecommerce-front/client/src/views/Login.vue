<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-hero">
        <h1>ShopPulse</h1>
        <p>鍙戠幇鏂板搧銆佺簿閫夊ソ鐗╀笌闄愭椂浼樻儬锛屼竴绔欏紡杞绘澗璐墿銆?/p>
        <p>鐧诲綍鍚庡彲鏌ョ湅涓汉淇℃伅銆佽鍗曚笌鏀惰棌銆?/p>
      </div>
      <div class="auth-form">
        <h2 class="auth-title">娆㈣繋鍥炴潵</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
          <div class="form-grid">
            <el-form-item label="閭" prop="email">
              <el-autocomplete v-model="form.email" :fetch-suggestions="querySearch" placeholder="璇疯緭鍏ラ偖绠? ref="emailInput">
                <template #suffix>
                  <button
                    v-if="form.email"
                    type="button"
                    class="input-clear-btn"
                    aria-label="娓呴櫎閭"
                    @click.stop="() => { form.email = ''; $refs.emailInput.focus(); }"
                  >
                    脳
                  </button>
                </template>
              </el-autocomplete>
            </el-form-item>
            <el-form-item label="瀵嗙爜" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="璇疯緭鍏ュ瘑鐮? />
            </el-form-item>
          </div>
          <div class="auth-actions">
            <el-checkbox v-model="form.rememberMe">璁颁綇鎴?/el-checkbox>
            <router-link to="/forgot" class="forgot-link">蹇樿瀵嗙爜锛?/router-link>
          </div>
          <el-button type="primary" native-type="submit" color="#ff6a3d" :loading="loading" style="width: 100%; margin-top: 18px;">
            鐧诲綍
          </el-button>
        </el-form>
        <div class="auth-footer">
          娌℃湁璐﹀彿锛?router-link to="/register">鍘绘敞鍐?/router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/auth";
import { useAuthStore } from "@/store/auth";

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref();
const loading = ref(false);
const knownUsers = ref([]);

const isAdminEmail = (email) => String(email || "").trim().toLowerCase() === "admin@local";

const form = reactive({
  email: authStore.rememberedEmail || "",
  password: "",
  rememberMe: !!authStore.rememberedEmail
});

const rules = {
  email: [{ required: true, message: "璇疯緭鍏ラ偖绠?, trigger: "blur" }],
  password: [{ required: true, message: "璇疯緭鍏ュ瘑鐮?, trigger: "blur" }]
};

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  loading.value = true;
  try {
    const res = await login({
      email: form.email,
      password: form.password,
      rememberMe: form.rememberMe
    });
    authStore.setSession(res.data.token, res.data.user, form.rememberMe);
    authStore.setRememberedEmail(form.rememberMe ? form.email.toLowerCase() : "");
    // 淇濆瓨閭鍒版湰鍦板凡鐭ョ敤鎴峰垪琛紙缁熶竴灏忓啓锛?
    try {
      const normalizedEmail = form.email.toLowerCase();
      if (!knownUsers.value.includes(normalizedEmail)) {
        knownUsers.value.unshift(normalizedEmail);
        localStorage.setItem("known_users", JSON.stringify(knownUsers.value));
      }
    } catch (e) {
      console.warn("save known users failed", e);
    }
    ElMessage.success("鐧诲綍鎴愬姛");
    router.push(isAdminEmail(form.email) ? "/admin/dashboard" : "/home");
  } catch (err) {
    // 閿欒鐢辨嫤鎴櫒璐熻矗灞曠ず锛屾澶勬崟鑾蜂互閬垮厤鏈崟鑾风殑 Promise 閿欒
    console.warn("Login failed:", err?.message || err);
  } finally {
    loading.value = false;
  }
};

const querySearch = (queryString, cb) => {
  const q = String(queryString || "").toLowerCase();
  const results = knownUsers.value.filter(u => u.toLowerCase().includes(q));
  cb(results.map(r => ({ value: r })));
};

onMounted(() => {
  try {
    const raw = localStorage.getItem("known_users");
    const arr = raw ? JSON.parse(raw) : [];
    const base = Array.isArray(arr) ? arr : [];
    // 鍘婚噸骞剁粺涓€杞皬鍐欙紝鍐欏叆 localStorage
    const seen = new Set();
    const deduped = [];
    for (const email of base) {
      const low = email.toLowerCase();
      if (!seen.has(low)) { seen.add(low); deduped.push(low); }
    }
    if (!deduped.includes("admin@local")) deduped.unshift("admin@local");
    if (authStore.rememberedEmail) {
      const remLow = authStore.rememberedEmail.toLowerCase();
      if (!deduped.includes(remLow)) deduped.unshift(remLow);
    }
    knownUsers.value = deduped;
    localStorage.setItem("known_users", JSON.stringify(deduped));
  } catch (e) {
    knownUsers.value = ["admin@local"];
  }
});
</script>

<style scoped>
.input-clear-btn {
  border: none;
  background: transparent;
  color: var(--text-secondary);
  padding: 0 6px;
  border-radius: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  outline: none;
}
.input-clear-btn:hover { background: rgba(0,0,0,0.04); border-radius: 2px; }

.auth-actions :deep(.el-checkbox__label),
.forgot-link {
  font-size: 14px;
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
