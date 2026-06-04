<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-hero">
        <h1>ShopPulse</h1>
        <p>发现新品、精选好物与限时优惠，一站式轻松购物。</p>
        <p>登录后可查看个人信息、订单与收藏。</p>
      </div>
      <div class="auth-form">
        <h2 class="auth-title">欢迎回来</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
          <div class="form-grid">
            <el-form-item label="邮箱" prop="email">
              <el-autocomplete v-model="form.email" :fetch-suggestions="querySearch" placeholder="请输入邮箱" ref="emailInput">
                <template #suffix>
                  <button
                    v-if="form.email"
                    type="button"
                    class="input-clear-btn"
                    aria-label="清除邮箱"
                    @click.stop="() => { form.email = ''; $refs.emailInput.focus(); }"
                  >
                    ×
                  </button>
                </template>
              </el-autocomplete>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
          </div>
          <div class="auth-actions">
            <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
            <router-link to="/forgot" class="forgot-link">忘记密码？</router-link>
          </div>
          <el-button type="primary" native-type="submit" color="#ff6a3d" :loading="loading" style="width: 100%; margin-top: 18px;">
            登录
          </el-button>
        </el-form>
        <div class="auth-footer">
          没有账号？<router-link to="/register">去注册</router-link>
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
  email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }]
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
    // 保存邮箱到本地已知用户列表（统一小写）
    try {
      const normalizedEmail = form.email.toLowerCase();
      if (!knownUsers.value.includes(normalizedEmail)) {
        knownUsers.value.unshift(normalizedEmail);
        localStorage.setItem("known_users", JSON.stringify(knownUsers.value));
      }
    } catch (e) {
      console.warn("save known users failed", e);
    }
    ElMessage.success("登录成功");
    router.push(isAdminEmail(form.email) ? "/admin/dashboard" : "/home");
  } catch (err) {
    // 错误由拦截器负责展示，此处捕获以避免未捕获的 Promise 错误
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
    // 去重并统一转小写，写入 localStorage
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
</style>
