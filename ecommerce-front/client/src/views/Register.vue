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
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <div class="form-grid">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="验证码" prop="code">
              <el-input v-model="form.code" placeholder="输入6位验证码">
                <template #append>
                  <el-button :disabled="countdown > 0" @click="handleSendCode">
                    {{ countdown > 0 ? `${countdown}s` : "Send" }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password placeholder="设置密码" />
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
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { register, sendCode } from "@/api/auth";
import { useAuthStore } from "@/store/auth";

const router = useRouter();
const authStore = useAuthStore();
const formRef = ref();
const loading = ref(false);
const countdown = ref(0);
let timer = null;

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
  await sendCode(form.email, "REGISTER");
  ElMessage.success("验证码已发送");
  startCountdown();
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
    ElMessage.success("注册成功");
    router.push("/home");
  } finally {
    loading.value = false;
  }
};
</script>
