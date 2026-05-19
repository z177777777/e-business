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
                    {{ countdown > 0 ? `${countdown}s` : "Send" }}
                  </el-button>
                </template>
              </el-input>
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
          </div>
          <el-button type="primary" color="#ff6a3d" :loading="loading" @click="handleReset" style="width: 100%; margin-top: 18px;">
            更新密码
          </el-button>
        </el-form>
        <div class="auth-footer">
          <router-link to="/login">← 登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { resetPassword, sendCode } from "@/api/auth";

const router = useRouter();
const formRef = ref();
const loading = ref(false);
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
</script>
