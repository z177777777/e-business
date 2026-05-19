<template>
  <div class="app-shell">
    <router-view />
    <aside class="side-toolbar">
      <button v-if="showHomeItem" class="side-item" type="button" @click="goHome">首页</button>
      <button v-if="showProfileItem" class="side-item" type="button" @click="goProfile">我的</button>
      <button class="side-item" type="button" @click="goCart">购物车</button>
      <button class="side-item" type="button" @click="goOrders">订单</button>
      <button class="side-item" type="button" @click="handleAction('service')">客服</button>
      <button class="side-item" type="button" @click="handleAction('feedback')">反馈</button>
      <button v-if="showBackTop" class="side-item" type="button" @click="backToTop">回顶部</button>
    </aside>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/store/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const showBackTop = ref(false);

const authRoutes = ["/login", "/register", "/forgot"];
const isMainPage = computed(() => !authRoutes.includes(route.path));
const showProfileItem = computed(() => isMainPage.value && !!auth.token);
const showHomeItem = computed(() => route.path !== "/home");

const updateBackTop = () => {
  showBackTop.value = window.scrollY > 240;
};

const goHome = () => {
  router.push("/home");
};

const goCart = () => {
  router.push("/cart");
};

const goOrders = () => {
  router.push("/orders");
};

const goProfile = () => {
  router.push("/profile");
};

const backToTop = () => {
  window.scrollTo({ top: 0, behavior: "smooth" });
};

const handleAction = (type) => {
  const messages = {
    service: "客服功能开发中",
    feedback: "反馈功能开发中"
  };
  ElMessage.info(messages[type] || "功能开发中");
};

onMounted(() => {
  updateBackTop();
  window.addEventListener("scroll", updateBackTop, { passive: true });
});

onBeforeUnmount(() => {
  window.removeEventListener("scroll", updateBackTop);
});
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  padding-right: 96px;
}

.side-toolbar {
  position: fixed;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  flex-direction: column;
  gap: 0;
  z-index: 50;
  padding: 6px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(12px);
  border: 0.5px solid rgba(255, 255, 255, 0.35);
  box-shadow: var(--shadow-soft);
}

.side-item {
  width: 78px;
  padding: 10px 8px;
  border: none;
  border-radius: 0;
  background: transparent;
  color: var(--text-primary);
  font-size: 12px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.side-item:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 720px) {
  .app-shell {
    padding-right: 68px;
  }

  .side-toolbar {
    right: 4px;
  }
  .side-item {
    width: 64px;
    padding: 8px 6px;
    font-size: 11px;
  }
}
</style>
