<template>
  <div class="admin-shell">
    <aside class="admin-sidebar">
      <div class="brand">
        <div class="brand-title">e-business</div>
        <div class="brand-subtitle">Admin Console</div>
      </div>

      <el-menu
        :default-active="activePath"
        class="admin-menu"
        background-color="#111827"
        text-color="#d1d5db"
        active-text-color="#ffffff"
        router
      >
        <el-menu-item index="/admin/dashboard">首页数据看板</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/products">商品管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
      </el-menu>
    </aside>

    <section class="admin-content">
      <header class="admin-header">
        <div>
          <h2>管理员后台</h2>
          <p>当前账号：{{ adminLabel }}</p>
        </div>
        <el-button type="danger" plain @click="handleLogout">退出登录</el-button>
      </header>

      <main class="admin-main">
        <router-view />
      </main>
    </section>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessageBox } from "element-plus";
import { useAuthStore } from "@/store/auth";
import { logout as logoutApi } from "@/api/auth";

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();

const activePath = computed(() => route.path);
const adminLabel = computed(() => authStore.user?.email || "admin@local");

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm("确定要退出管理员后台吗？", "提示", {
      confirmButtonText: "退出",
      cancelButtonText: "取消",
      type: "warning"
    });
    await logoutApi();
  } catch (error) {
    if (error !== "cancel") {
      // ignore logout api errors; local session still cleared
    }
  } finally {
    authStore.clearSession();
    router.push("/admin/login");
  }
};
</script>

<style scoped>
.admin-shell {
  display: flex;
  min-height: 100vh;
  background: #f3f4f6;
}

.admin-sidebar {
  width: 240px;
  background: #111827;
  color: #fff;
  display: flex;
  flex-direction: column;
}

.brand {
  padding: 22px 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-title {
  font-size: 20px;
  font-weight: 700;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #9ca3af;
}

.admin-menu {
  border-right: none;
  flex: 1;
}

.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.admin-header h2 {
  margin: 0;
  font-size: 22px;
}

.admin-header p {
  margin: 6px 0 0;
  color: #6b7280;
}

.admin-main {
  padding: 24px;
}

@media (max-width: 960px) {
  .admin-shell {
    flex-direction: column;
  }

  .admin-sidebar {
    width: 100%;
  }
}
</style>
