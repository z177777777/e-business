<template>
  <div class="user-shell">
    <aside class="user-sidebar">
      <div class="user-brand">
        <div class="brand-title">个人中心</div>
      </div>
      <el-menu
        :default-active="activePath"
        :default-openeds="defaultOpeneds"
        class="user-menu"
        router
      >
        <el-menu-item index="/orders">
          <el-icon><Document /></el-icon>
          <span>订单中心</span>
        </el-menu-item>

        <el-menu-item index="/profile/wallet">
          <el-icon><Wallet /></el-icon>
          <span>我的钱包</span>
        </el-menu-item>

        <el-sub-menu index="account-sub">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>账户设置</span>
          </template>
          <el-menu-item index="/profile/info">个人信息</el-menu-item>
          <el-menu-item index="/profile/security">账户安全</el-menu-item>
          <el-menu-item index="/profile/address">地址管理</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/profile/favorites">
          <el-icon><Star /></el-icon>
          <span>商品收藏</span>
        </el-menu-item>

        <el-menu-item index="/profile/service">
          <el-icon><Service /></el-icon>
          <span>客服服务</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <el-button type="danger" plain @click="handleLogout" style="width:100%">退出登录</el-button>
      </div>
    </aside>

    <section class="user-content">
      <router-view />
    </section>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { Document, Wallet, Setting, Star, Service } from "@element-plus/icons-vue";
import { useAuthStore } from "@/store/auth";
import { logout as logoutApi } from "@/api/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activePath = computed(() => route.path);
const defaultOpeneds = computed(() => {
  if (["/profile/info", "/profile/security", "/profile/address"].includes(route.path)) {
    return ["account-sub"];
  }
  return [];
});

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("退出后将回到登录页，你可以随时再次登录。", "退出登录", {
      confirmButtonText: "继续退出",
      cancelButtonText: "暂不退出",
      type: "warning",
      customClass: "pretty-confirm-box",
      distinguishCancelAndClose: true,
      center: true
    });
  } catch (e) {
    return;
  }
  try { await logoutApi(); } catch (e) { console.warn("logout api failed", e); }
  authStore.clearSession();
  router.push("/login");
};
</script>

<style scoped>
.user-shell {
  display: flex;
  min-height: 100vh;
  background: #f5f7fb;
}

.user-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.user-brand {
  padding: 20px 18px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.brand-title {
  font-size: 18px;
  font-weight: 700;
  color: #1c1c1e;
}

.user-menu {
  border-right: none;
  flex: 1;
  padding-top: 4px;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #f0f0f0;
}

.user-content {
  flex: 1;
  padding: 28px 32px 80px;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .user-shell { flex-direction: column; }
  .user-sidebar { width: 100% !important; flex-direction: row; flex-wrap: wrap; border-right: none !important; border-bottom: 1px solid #eee; }
  .user-brand { width: 100%; padding: 12px 16px 8px; }
  .brand-title { font-size: 16px; }
  .user-menu { display: flex; flex-wrap: wrap; border-right: none; }
  .user-menu .el-menu-item { flex: 0 1 auto; padding: 8px 12px !important; font-size: 13px; height: auto; line-height: 1.5; }
  .user-menu .el-sub-menu { flex: 0 1 auto; }
  .user-menu .el-sub-menu .el-sub-menu__title { padding: 8px 12px !important; font-size: 13px; height: auto; line-height: 1.5; }
  .sidebar-footer { width: 100%; padding: 10px 16px; }
  .sidebar-footer .el-button { width: 100% !important; }
  .user-content { padding: 12px !important; }
}
</style>
