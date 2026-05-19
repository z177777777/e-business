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
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
          </div>
          <div class="auth-actions">
            <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
            <router-link to="/forgot">忘记密码？</router-link>
          </div>
          <el-button type="primary" color="#ff6a3d" :loading="loading" @click="handleLogin" style="width: 100%; margin-top: 18px;">
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
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { login } from "@/api/auth";
import { useAuthStore } from "@/store/auth";

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref();
const loading = ref(false);

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
  await formRef.value.validate();
  loading.value = true;
  try {
    const res = await login({
      email: form.email,
      password: form.password,
      rememberMe: form.rememberMe
    });
    authStore.setSession(res.data.token, res.data.user, form.rememberMe);
    authStore.setRememberedEmail(form.rememberMe ? form.email : "");
    ElMessage.success("登录成功");
    router.push("/home");
  } finally {
    loading.value = false;
  }
};
</script>
